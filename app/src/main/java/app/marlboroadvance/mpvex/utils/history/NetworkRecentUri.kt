package app.marlboroadvance.mpvex.utils.history

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import app.marlboroadvance.mpvex.domain.network.NetworkConnection
import app.marlboroadvance.mpvex.repository.NetworkRepository
import app.marlboroadvance.mpvex.ui.browser.networkstreaming.proxy.NetworkStreamingProxy
import app.marlboroadvance.mpvex.ui.player.PlayerActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

object NetworkRecentUri : KoinComponent {
  const val SCHEME = "xnplayer-network"
  private const val TAG = "NetworkRecentUri"
  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
  private val repository: NetworkRepository by inject()

  data class Target(
    val connectionId: Long,
    val path: String,
  )

  fun encode(connectionId: Long, path: String): String {
    return "$SCHEME://$connectionId/${Uri.encode(path)}"
  }

  fun isSupported(value: String?): Boolean {
    return value?.startsWith("$SCHEME://", ignoreCase = true) == true
  }

  fun decode(value: String): Target? = runCatching {
    val uri = Uri.parse(value)
    if (!uri.scheme.equals(SCHEME, ignoreCase = true)) return null
    val connectionId = uri.host?.toLongOrNull() ?: uri.pathSegments.firstOrNull()?.toLongOrNull() ?: return null
    val encodedPath = uri.pathSegments.drop(1).joinToString("/")
    Target(connectionId, Uri.decode(encodedPath))
  }.getOrNull()

  fun displayName(value: String): String {
    val decoded = decode(value)
    val raw = decoded?.path ?: value
    return raw.substringAfterLast('/').takeIf { it.isNotBlank() } ?: "网络视频"
  }

  fun launch(
    context: Context,
    value: String,
    launchSource: String?,
    displayName: String? = null,
    fileSize: Long = -1L,
    mimeType: String = "video/mp4",
  ) {
    val target = decode(value)
    if (target == null) return
    scope.launch {
      val connection: NetworkConnection? = withContext(Dispatchers.IO) {
        runCatching { repository.getConnectionById(target.connectionId) }.getOrNull()
      }
      if (connection == null) {
        Toast.makeText(context, "网络连接已删除，无法播放", Toast.LENGTH_SHORT).show()
        return@launch
      }
      val proxyUrl = runCatching {
        NetworkStreamingProxy.getInstance().registerStream(
          streamId = "recent_${target.connectionId}_${System.currentTimeMillis()}",
          connection = connection,
          filePath = target.path,
          fileSize = fileSize,
          mimeType = mimeType,
        )
      }.getOrNull()
      if (proxyUrl == null) {
        Toast.makeText(context, "无法重建网络播放地址", Toast.LENGTH_SHORT).show()
        return@launch
      }

      val title = displayName?.takeIf { it.isNotBlank() } ?: this@NetworkRecentUri.displayName(value)
      val intent = Intent(Intent.ACTION_VIEW, Uri.parse(proxyUrl))
      intent.setClass(context, PlayerActivity::class.java)
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      intent.putExtra("internal_launch", true)
      intent.putExtra("launch_source", launchSource)
      intent.putExtra("title", title)
      intent.putExtra("filename", title)
      intent.putExtra("network_file_path", target.path)
      intent.putExtra("network_connection_id", target.connectionId)
      context.startActivity(intent)
    }
  }
}
