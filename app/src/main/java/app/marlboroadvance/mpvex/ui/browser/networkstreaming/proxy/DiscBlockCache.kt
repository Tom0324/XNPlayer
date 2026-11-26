package app.marlboroadvance.mpvex.ui.browser.networkstreaming.proxy

import android.util.Log
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.io.SequenceInputStream
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap

/**
 * 4 MiB block cache for disc images served through the local proxy.
 *
 * mpv's dvdnav/bluray demuxer performs many small random reads (IFO/VMG
 * metadata) when opening an ISO. Forwarding every Range request to the remote
 * WebDAV server is far too slow and mpv falls back to linear mpegts playback
 * (extras first). Cache aligned 4 MiB blocks in memory with an LRU eviction
 * policy so repeated small seeks hit memory and the disc demuxer can open.
 */
class DiscBlockCache(
  private val tag: String = "DiscBlockCache",
  private val blockSize: Int = 4 * 1024 * 1024,
  private val maxBlocks: Int = 32,
) {
  private data class Block(val data: ByteArray)

  // LinkedHashMap with access-order == LRU; guarded by synchronized methods.
  private val blocks = object : LinkedHashMap<Long, Block>(maxBlocks, 0.75f, true) {
    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Long, Block>?): Boolean {
      return size > maxBlocks
    }
  }

  @Synchronized
  fun cachedBlockCount(): Int = blocks.size

  @Synchronized
  fun hasBlock(index: Long): Boolean = blocks.containsKey(index)

  @Synchronized
  fun getBlock(index: Long): ByteArray? = blocks[index]?.data

  @Synchronized
  fun putBlock(index: Long, data: ByteArray) {
    blocks[index] = Block(data)
  }

  @Synchronized
  fun clear() = blocks.clear()

  /**
   * Build an InputStream that serves [start, start+length) from cached blocks,
   * calling [fetchBlock] (which must return exactly blockSize bytes, or fewer
   * for the final block) for any missing block.
   */
  fun streamRange(
    start: Long,
    length: Long,
    fileSize: Long,
    fetchBlock: (index: Long) -> ByteArray?,
  ): InputStream? {
    if (length <= 0) return ByteArrayInputStream(ByteArray(0))

    val endExclusive = (start + length).coerceAtMost(fileSize)
    val streams = mutableListOf<InputStream>()
    var pos = start

    while (pos < endExclusive) {
      val blockIndex = pos / blockSize
      val offsetInBlock = (pos % blockSize).toInt()
      val block = getBlock(blockIndex) ?: run {
        val fetched = fetchBlock(blockIndex) ?: return null
        putBlock(blockIndex, fetched)
        fetched
      }
      val availableInBlock = block.size - offsetInBlock
      if (availableInBlock <= 0) return null
      val take = minOf(availableInBlock.toLong(), endExclusive - pos).toInt()
      streams.add(ByteArrayInputStream(block, offsetInBlock, take))
      pos += take
    }

    return if (streams.size == 1) {
      streams[0]
    } else {
      SequenceInputStream(Collections.enumeration(streams))
    }
  }
}
