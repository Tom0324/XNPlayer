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
import app.marlboroadvance.mpvex.repository.NetworkRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
            val proxyUrl = proxy.registerStream(
              streamId = streamId,
              connection = connection,
              filePath = item.path,
              fileSize = item.size,
              mimeType = item.mimeType ?: "video/mp4",
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
        intent.putExtra("playlist_index", currentIndex)
        intent.setDataAndType(currentUri, file.mimeType ?: "video/*")
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
