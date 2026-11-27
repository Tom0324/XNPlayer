package app.marlboroadvance.mpvex.ui.browser.networkstreaming.proxy

import android.util.Log
import java.io.File
import java.io.InputStream
import java.io.RandomAccessFile
import java.util.concurrent.Executors

/**
 * Disk-backed, on-demand block cache for disc images served via the local proxy.
 *
 * mpv's dvdnav/bluray demuxer performs many small random reads (IFO/VMG
 * metadata). Forwarding every Range request to the remote WebDAV server is far
 * too slow, so blocks are cached in a sparse temp file (not heap) with LRU.
 * Crucially, blocks are fetched LAZILY on read — never eagerly for the whole
 * requested range — otherwise an open-ended `bytes=0-` request (23 GB) would
 * try to download the entire disc before returning.
 *
 * Sequential background prefetch is enabled after a block miss: once mpv
 * starts playing a title, the next few 4 MiB blocks are downloaded ahead of
 * time. This prevents the demuxer cache from collapsing to one block (~1s)
 * when WebDAV latency is close to the video bitrate.
 */
class DiscBlockCache(
  private val cacheFile: File,
  private val tag: String = "DiscBlockCache",
  private val blockSize: Long = 4L * 1024L * 1024L,
  private val maxBlocks: Int = 64,
) : AutoCloseable {

  private val slots = object : LinkedHashMap<Long, Int>(maxBlocks, 0.75f, true) {
    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Long, Int>?): Boolean = size > maxBlocks
  }
  private var nextSlot = 0
  private val raf = RandomAccessFile(cacheFile, "rw")

  private val prefetchAheadBlocks = 16
  private val prefetchExecutor = Executors.newFixedThreadPool(4) { runnable ->
    Thread(runnable, "xnplayer-disc-cache-prefetch").apply { isDaemon = true }
  }
  private val fetching = HashSet<Long>()
  private var lastPrefetchTrigger = -1L

  @Synchronized fun cachedBlockCount(): Int = slots.size

  @Synchronized
  private fun slotOf(index: Long): Int? = slots[index]

  @Synchronized
  private fun ensureSlot(index: Long): Int {
    slots[index]?.let { return it }
    return if (slots.size < maxBlocks) {
      val slot = nextSlot++
      slots[index] = slot
      slot
    } else {
      val eldest = slots.entries.firstOrNull { !fetching.contains(it.key) }?.key
        ?: slots.entries.first().key
      val reusable = slots.remove(eldest)!!
      slots[index] = reusable
      reusable
    }
  }

  private fun waitIfFetching(index: Long) {
    synchronized(this) {
      while (fetching.contains(index)) {
        (this as java.lang.Object).wait(100)
      }
    }
  }

  /** Fetch a missing block into the cache file with a small 64 KiB buffer. */
  private fun fetchInto(
    index: Long,
    fileSize: Long,
    fetch: (index: Long, len: Int, sink: (ByteArray, Int, Int) -> Boolean) -> Boolean,
  ): Boolean {
    waitIfFetching(index)

    val blockLen = minOf(blockSize, fileSize - index * blockSize).toInt()
    if (blockLen <= 0) return false

    val reservedSlot = synchronized(this) {
      if (fetching.contains(index)) {
        null
      } else {
        val s = ensureSlot(index)
        fetching.add(index)
        val targetOffset = s * blockSize
        if (raf.length() < targetOffset + blockLen) raf.setLength(targetOffset + blockLen)
        s
      }
    }
    if (reservedSlot == null) {
      waitIfFetching(index)
      return synchronized(this) { slots.containsKey(index) }
    }
    val slot = reservedSlot

    var written = 0
    return try {
      val ok = fetch(index, blockLen) { b, off, len ->
        synchronized(this@DiscBlockCache) {
          raf.seek(slot * blockSize + written)
          raf.write(b, off, len)
        }
        written += len
        true
      }
      val complete = ok && written == blockLen
      if (!complete) {
        synchronized(this) { slots.remove(index) }
      }
      complete
    } catch (e: Exception) {
      Log.e(tag, "fetch block $index failed: ${e.message}")
      synchronized(this) { slots.remove(index) }
      false
    } finally {
      synchronized(this) {
        fetching.remove(index)
        (this as java.lang.Object).notifyAll()
      }
    }
  }

  /** Schedule sequential prefetch after a real block miss. */
  private fun prefetchAhead(
    currentIndex: Long,
    fileSize: Long,
    fetchBlock: (index: Long, blockLen: Int, sink: (ByteArray, Int, Int) -> Boolean) -> Boolean,
  ) {
    val shouldSchedule = synchronized(this) {
      if (currentIndex > lastPrefetchTrigger) {
        lastPrefetchTrigger = currentIndex
        true
      } else {
        false
      }
    }
    if (!shouldSchedule || prefetchExecutor.isShutdown) return

    prefetchExecutor.execute {
      Log.d(tag, "PREFETCH from block=$currentIndex ahead=$prefetchAheadBlocks")
      for (delta in 1..prefetchAheadBlocks) {
        if (prefetchExecutor.isShutdown) break
        val index = currentIndex + delta
        if (index * blockSize >= fileSize) break
        val needed = synchronized(this) {
          !slots.containsKey(index) && !fetching.contains(index)
        }
        if (needed) {
          runCatching { fetchInto(index, fileSize, fetchBlock) }
            .onFailure { Log.w(tag, "prefetch block $index failed: ${it.message}") }
        }
      }
    }
  }

  override fun close() {
    prefetchExecutor.shutdownNow()
    try { raf.close() } catch (_: Exception) {}
    try { cacheFile.delete() } catch (_: Exception) {}
  }

  /**
   * Returns a lazy InputStream for [start, start+length). Blocks are downloaded
   * only when read() actually reaches them; after a miss, sequential blocks are
   * prefetched in the background.
   */
  fun streamRange(
    start: Long,
    length: Long,
    fileSize: Long,
    fetchBlock: (index: Long, blockLen: Int, sink: (ByteArray, Int, Int) -> Boolean) -> Boolean,
  ): InputStream? {
    if (length <= 0) return null
    val endExclusive = (start + length).coerceAtMost(fileSize)
    if (start >= endExclusive) return null

    return object : InputStream() {
      private var cur = start
      private val buf = ByteArray(64 * 1024)
      private var bufPos = 0
      private var bufLen = 0

      private fun fill(): Boolean {
        if (cur >= endExclusive) return false
        val idx = cur / blockSize
        if (slotOf(idx) == null) {
          Log.d(tag, "FETCH block=$idx offset=${idx * blockSize}")
          if (!fetchInto(idx, fileSize, fetchBlock)) return false
          prefetchAhead(idx, fileSize, fetchBlock)
        }
        waitIfFetching(idx)
        val slot = slotOf(idx) ?: return false
        val offInBlock = (cur % blockSize).toInt()
        val inBlock = (blockSize - offInBlock).toInt()
        val want = minOf(minOf(buf.size.toLong(), endExclusive - cur), inBlock.toLong()).toInt()
        return synchronized(this@DiscBlockCache) {
          try {
            raf.seek(slot * blockSize + offInBlock)
            bufLen = raf.read(buf, 0, want)
            bufPos = 0
            bufLen > 0
          } catch (e: Exception) {
            Log.e(tag, "read block $idx failed: ${e.message}")
            false
          }
        }
      }

      override fun read(): Int {
        if (bufPos >= bufLen && !fill()) return -1
        cur++
        return buf[bufPos++].toInt() and 0xFF
      }

      override fun read(b: ByteArray, off: Int, len: Int): Int {
        if (len <= 0) return 0
        var written = 0
        while (written < len) {
          if (bufPos >= bufLen && !fill()) break
          val n = minOf(bufLen - bufPos, len - written)
          System.arraycopy(buf, bufPos, b, off + written, n)
          bufPos += n
          cur += n
          written += n
        }
        return if (written > 0) written else -1
      }

      override fun available(): Int = (endExclusive - cur).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
      override fun close() { /* raf owned by cache */ }
    }
  }
}
