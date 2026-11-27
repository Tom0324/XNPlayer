package app.marlboroadvance.mpvex.ui.browser.networkstreaming

import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.marlboroadvance.mpvex.domain.network.NetworkConnection
import app.marlboroadvance.mpvex.domain.network.NetworkFile
import app.marlboroadvance.mpvex.domain.network.NetworkProtocol
import app.marlboroadvance.mpvex.domain.recentlyplayed.repository.RecentlyPlayedRepository
import app.marlboroadvance.mpvex.repository.NetworkRepository
import app.marlboroadvance.mpvex.utils.history.NetworkRecentUri
import app.marlboroadvance.mpvex.utils.history.RecentlyPlayedOps
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * ViewModel for browsing files on a network share
 * Follows MVVM pattern with proper separation of concerns
 */
class NetworkBrowserViewModel(
  private val application: Application,
  private val connectionId: Long,
  private val currentPath: String,
) : AndroidViewModel(application),
  KoinComponent {
  private val repository: NetworkRepository by inject()
  private val recentlyPlayedRepository: RecentlyPlayedRepository by inject()

  /**
   * Network-browser-specific last-played path.
   *
   * The global [RecentlyPlayedOps.observeLastPlayedPath] deliberately filters network
   * streams out of "last played for highlight" (SQL launchSource whitelist excludes
   * `network_stream`). That is correct for the home screen, but it breaks this screen:
   * after playing an episode over WebDAV/SMB/FTP and coming back, the highlight path is
   * null so the list always jumps back to the top instead of the episode just played.
   *
   * Here we observe the full recent list and pick the newest entry that belongs to the
   * current connection, without touching the global highlight behaviour.
   */
  val recentlyPlayedFilePath: StateFlow<String?> =
    recentlyPlayedRepository
      .observeRecentlyPlayed(200)
      .mapLatest { entries ->
        entries.firstOrNull { entry ->
          val target = NetworkRecentUri.decode(entry.filePath) ?: return@firstOrNull false
          if (target.connectionId != connectionId) return@firstOrNull false

          // Match the local video browser behaviour: find the newest recently
          // played file inside THIS folder, not merely the newest file in the
          // whole connection. Otherwise browsing back into an older folder
          // after playing another folder would always jump to the top.
          val parent = target.path.substringBeforeLast('/', "")
          parent.trim('/').equals(currentPath.trim('/'), ignoreCase = true)
        }?.filePath
      }
      .distinctUntilChanged()
      .flowOn(Dispatchers.IO)
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

  private val _files = MutableStateFlow<List<NetworkFile>>(emptyList())
  val files: StateFlow<List<NetworkFile>> = _files.asStateFlow()

  private val _isLoading = MutableStateFlow(false)
  val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

  private val _error = MutableStateFlow<String?>(null)
  val error: StateFlow<String?> = _error.asStateFlow()

  /**
   * Load files in the current directory
   */
  fun loadFiles() {
    viewModelScope.launch {
      _isLoading.value = true
      _error.value = null

      try {
        val connection = repository.getConnectionById(connectionId)
          ?: throw Exception("连接ion not found")

        repository.listFiles(connection, currentPath)
          .onSuccess { fileList ->
            _files.value = fileList.sortedWith(
              compareBy<NetworkFile> { !it.isDirectory }
                .thenBy { it.name.lowercase() },
            )
          }
          .onFailure { e ->
            _error.value = e.message ?: "未知错误"
          }
      } catch (e: Exception) {
        _error.value = e.message ?: "未知错误"
      } finally {
        _isLoading.value = false
      }
    }
  }



  /**
   * Play a video file
   */
  fun playVideo(file: NetworkFile, playlistFiles: List<NetworkFile> = listOf(file)) {
    viewModelScope.launch {
      try {
        val connection = repository.getConnectionById(connectionId)
          ?: throw Exception("connection not found")

        val useProxy = connection.protocol in PROXY_PROTOCOLS
        val orderedFiles =
          if (playlistFiles.any { it.path == file.path }) playlistFiles else listOf(file) + playlistFiles
        val currentIndex = orderedFiles.indexOfFirst { it.path == file.path }.coerceAtLeast(0)
        val batchId = System.currentTimeMillis()
        val playlistUris = ArrayList<android.net.Uri>()

        orderedFiles.forEachIndexed { index, item ->
          val itemUri = if (useProxy) {
            val proxy = app.marlboroadvance.mpvex.ui.browser.networkstreaming.proxy.NetworkStreamingProxy.getInstance()
            val streamId = "${connectionId}_${batchId}_${index}"
            // ISO images must not be advertised as a video mime-type: mpv would
            // pick the ffmpeg mpegts demuxer and play the disc linearly (extras
            // first) instead of using dvdnav/bluray disc navigation.
            val isDiscImage = item.path.lowercase().endsWith(".iso") || item.path.lowercase().endsWith(".img")
            val proxyMime = if (isDiscImage) "application/x-iso9660-image" else (item.mimeType ?: "video/mp4")
            val proxyUrl = proxy.registerStream(
              streamId = streamId,
              connection = connection,
              filePath = item.path,
              fileSize = item.size,
              mimeType = proxyMime,
            )
            android.net.Uri.parse(proxyUrl)
          } else {
            NetworkStreamingProvider.setConnection(connectionId, connection)
            NetworkStreamingProvider.getUri(application, connectionId, item.path)
          }
          playlistUris.add(itemUri)
        }

        val currentUri = playlistUris[currentIndex]
        val intent = Intent(Intent.ACTION_VIEW, currentUri)
        intent.setClass(application, app.marlboroadvance.mpvex.ui.player.PlayerActivity::class.java)
        intent.putExtra("internal_launch", true)
        intent.putExtra("launch_source", "network_stream")
        intent.putExtra("title", file.name)
        intent.putExtra("filename", file.name)
        intent.putExtra("network_file_path", file.path)
        intent.putExtra("network_connection_id", connectionId)
        intent.putParcelableArrayListExtra("playlist", playlistUris)
        intent.putStringArrayListExtra("playlist_names", ArrayList(orderedFiles.map { it.name }))
        intent.putExtra("playlist_index", currentIndex)
        // Disc images use a non-video mime so mpv's disc demuxer is preferred
        val currentIsDisc = file.path.lowercase().endsWith(".iso") || file.path.lowercase().endsWith(".img")
        val intentMime = if (currentIsDisc) "application/x-iso9660-image" else (file.mimeType ?: "video/*")
        intent.setDataAndType(currentUri, intentMime)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        if (!useProxy) {
          intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
          val clip = android.content.ClipData.newUri(
            application.contentResolver,
            "Video playlist",
            playlistUris[0],
          )
          for (i in 1 until playlistUris.size) {
            clip.addItem(android.content.ClipData.Item(playlistUris[i]))
          }
          intent.clipData = clip
        }

        application.startActivity(intent)
      } catch (e: Exception) {
        Log.e(TAG, "Error playing video", e)
        _error.value = e.message ?: "unknown error"
      }
    }
  }

  companion object {
    private const val TAG = "NetworkBrowserVM"

    // Protocols that require proxy server for seeking support
    private val PROXY_PROTOCOLS = setOf(
      NetworkProtocol.SMB,
      NetworkProtocol.FTP,
      NetworkProtocol.WEBDAV,
    )

    fun factory(
      application: Application,
      connectionId: Long,
      currentPath: String,
    ): ViewModelProvider.Factory =
      viewModelFactory {
        initializer {
          NetworkBrowserViewModel(application, connectionId, currentPath)
        }
      }
  }
}
