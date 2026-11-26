package app.marlboroadvance.mpvex.ui.browser.networkstreaming.proxy

import android.util.Log
import java.io.File
import java.io.InputStream
import java.io.RandomAccessFile

/**
 * Disk-backed 4 MiB block cache for disc images served through the local proxy.
 *
 * mpv's dvdnav/bluray demuxer performs many small random reads (IFO/VMG
 * metadata) when opening an ISO. Forwarding every Range request to the remote
 * WebDAV server is far too slow and mpv falls back to linear mpegts playback
 * (extras first).
 *
 * Blocks are stored in a sparse temp file instead of heap ByteArray objects:
 * a 32-block cache would otherwise pin ~128 MB of heap and repeatedly allocate
 * 4 MB contiguous arrays, which OOM-crashes on low-heap devices.
 */
class DiscBlockCache(
  private val cacheFile: File,
  private val tag: String = "DiscBlockCache",
  private val blockSize: Long = 4L * 1024L * 1024L,
  private val maxBlocks: Int = 32,
) : AutoCloseable {

  // blockIndex -> slot; access-order LinkedHashMap == LRU
  private val slots = object : LinkedHashMap<Long, Int>(maxBlocks, 0.75f, true) {
    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Long, Int>?): Boolean = size > maxBlocks
  }
  private var nextSlot = 0
  private val raf = RandomAccessFile(cacheFile, "rw")

  @Synchronized
  fun slotOf(index: Long): Int? = slots[index]

  @Synchronized
  fun ensureSlot(index: Long): Int {
    slots[index]?.let { return it }
    val slot = if (slots.size < maxBlocks) {
      nextSlot++.also { slots[index] = it }
    } else {
      val eldest = slots.entries.first().key
      val reusable = slots.remove(eldest)!!
      slots[index] = reusable
      reusable
    }
    return slot
  }

  @Synchronized
  fun blockOffset(index: Long): Long = ensureSlot(index) * blockSize

  /**
   * Fetch a missing block and write it directly to the cache file using a small
   * 64 KiB heap buffer (never a 4 MB allocation). [write] receives the target
   * file offset, the block length, and a buffer-filled callback.
   * Returns true on success.
   */
  fun fetchInto(index: Long, write: (fileOffset: Long, blockLen: Int, sink: (ByteArray, Int, Int) -> Boolean) -> Boolean): Boolean {
    val targetOffset = blockOffset(index)
    val blockLen = minOf(blockSize, Long.MAX_VALUE - targetOffset).toInt()
    return synchronized(this) {
      try {
        raf.setLength(maxOf(raf.length(), targetOffset + blockLen))
        raf.seek(targetOffset)
        val ok = write(targetOffset, blockLen) { b, off, len ->
          raf.write(b, off, len)
          true
        }
        ok
      } catch (e: Exception) {
        Log.e(tag, "write block $index failed: ${e.message}")
        false
      }
    }
  }

  @Synchronized
  fun clear() {
    slots.clear()
    nextSlot = 0
  }

  override fun close() {
    try { raf.close() } catch (_: Exception) {}
    try { cacheFile.delete() } catch (_: Exception) {}
  }

  /**
   * Returns an InputStream reading [start, start+length) from the cache file.
   * [fetchBlock] is called for missing slots with (blockIndex, targetFileOffset,
   * blockSize) and must write the block bytes directly into the file (no heap
   * ByteArray). Returns false if the fetch failed.
   */
  fun streamRange(
    start: Long,
    length: Long,
    fileSize: Long,
    fetchBlock: (index: Long, blockLen: Int, sink: (ByteArray, Int, Int) -> Boolean) -> Boolean,
  ): InputStream? {
    if (length <= 0) return null
    val endExclusive = (start + length).coerceAtMost(fileSize)

    var pos = start
    while (pos < endExclusive) {
      val blockIndex = pos / blockSize
      val slot = slotOf(blockIndex)
      if (slot == null) {
        val blockLen = minOf(blockSize, fileSize - blockIndex * blockSize).toInt()
        val ok = fetchInto(blockIndex) { _, _, sink ->
          fetchBlock(blockIndex, blockLen, sink)
        }
        if (!ok) return null
      }
      // Advance to the next block boundary (or the requested end). Never land
      // back inside the same block, otherwise the loop never terminates.
      val nextBoundary = blockIndex * blockSize + blockSize
      pos = minOf(nextBoundary, endExclusive)
    }

    // Single RAF-backed stream; positions are real file offsets in the cache file.
    return object : InputStream() {
      private var cur = start
      private val end = endExclusive
      private val buf = ByteArray(64 * 1024)
      private var bufPos = 0
      private var bufLen = 0

      override fun read(): Int {
        if (bufPos >= bufLen) {
          if (cur >= end) return -1
          val idx = cur / blockSize
          val slot = slotOf(idx) ?: return -1
          val offInBlock = (cur % blockSize).toInt()
          val toRead = minOf(buf.size.toLong(), end - cur).toInt()
          synchronized(this@DiscBlockCache) {
            raf.seek(slot * blockSize + offInBlock)
            bufLen = raf.read(buf, 0, toRead)
          }
          if (bufLen <= 0) return -1
          bufPos = 0
        }
        cur++
        return buf[bufPos++].toInt() and 0xFF
      }

      override fun read(b: ByteArray, off: Int, len: Int): Int {
        if (len <= 0) return 0
        // Fast path: copy from the current internal buffer first.
        var written = 0
        if (bufPos < bufLen) {
          val n = minOf(bufLen - bufPos, len)
          System.arraycopy(buf, bufPos, b, off, n)
          bufPos += n
          cur += n
          written += n
        }
        // Then read directly from the cache file in bulk for the remainder.
        while (written < len && cur < end) {
          val idx = cur / blockSize
          val slot = slotOf(idx) ?: break
          val offInBlock = (cur % blockSize).toInt()
          val want = minOf((len - written).toLong(), end - cur).toInt()
          val n = synchronized(this@DiscBlockCache) {
            raf.seek(slot * blockSize + offInBlock)
            raf.read(b, off + written, want)
          }
          if (n <= 0) break
          written += n
          cur += n
        }
        return if (written > 0) written else -1
      }

      override fun close() { /* raf owned by cache */ }
    }
  }
}
