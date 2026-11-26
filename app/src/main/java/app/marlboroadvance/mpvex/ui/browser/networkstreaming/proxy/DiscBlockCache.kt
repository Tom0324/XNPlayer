package app.marlboroadvance.mpvex.ui.browser.networkstreaming.proxy

import android.util.Log
import java.io.File
import java.io.InputStream
import java.io.RandomAccessFile

/**
 * Disk-backed, on-demand 4 MiB block cache for disc images served via the
 * local proxy.
 *
 * mpv's dvdnav/bluray demuxer performs many small random reads (IFO/VMG
 * metadata). Forwarding every Range request to the remote WebDAV server is far
 * too slow, so blocks are cached in a sparse temp file (not heap) with LRU.
 * Crucially, blocks are fetched LAZILY on read — never eagerly for the whole
 * requested range — otherwise an open-ended `bytes=0-` request (23 GB) would
 * try to download the entire disc before returning.
 */
class DiscBlockCache(
  private val cacheFile: File,
  private val tag: String = "DiscBlockCache",
  private val blockSize: Long = 4L * 1024L * 1024L,
  private val maxBlocks: Int = 32,
) : AutoCloseable {

  private val slots = object : LinkedHashMap<Long, Int>(maxBlocks, 0.75f, true) {
    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Long, Int>?): Boolean = size > maxBlocks
  }
  private var nextSlot = 0
  private val raf = RandomAccessFile(cacheFile, "rw")

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
      val eldest = slots.entries.first().key
      val reusable = slots.remove(eldest)!!
      slots[index] = reusable
      reusable
    }
  }

  /** Fetch a missing block into the cache file with a small 64 KiB buffer. */
  private fun fetchInto(index: Long, fileSize: Long, fetch: (index: Long, len: Int, sink: (ByteArray, Int, Int) -> Boolean) -> Boolean): Boolean {
    val targetOffset = ensureSlot(index) * blockSize
    val blockLen = minOf(blockSize, fileSize - index * blockSize).toInt()
    if (blockLen <= 0) return false
    return synchronized(this) {
      try {
        if (raf.length() < targetOffset + blockLen) raf.setLength(targetOffset + blockLen)
        raf.seek(targetOffset)
        fetch(index, blockLen) { b, off, len -> raf.write(b, off, len); true }
      } catch (e: Exception) {
        Log.e(tag, "fetch block $index failed: ${e.message}")
        false
      }
    }
  }

  override fun close() {
    try { raf.close() } catch (_: Exception) {}
    try { cacheFile.delete() } catch (_: Exception) {}
  }

  /**
   * Returns a lazy InputStream for [start, start+length). Blocks are downloaded
   * only when read() actually reaches them.
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
        }
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
