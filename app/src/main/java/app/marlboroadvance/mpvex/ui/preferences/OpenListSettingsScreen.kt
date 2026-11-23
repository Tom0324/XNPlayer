package app.marlboroadvance.mpvex.ui.preferences

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.marlboroadvance.mpvex.openlist.OpenListActivity
import app.marlboroadvance.mpvex.openlist.OpenListController
import app.marlboroadvance.mpvex.openlist.OpenListRuntimeState
import app.marlboroadvance.mpvex.presentation.Screen
import app.marlboroadvance.mpvex.ui.utils.LocalBackStack
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import me.zhanghai.compose.preference.Preference
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import me.zhanghai.compose.preference.SwitchPreference

@Serializable
object OpenListSettingsScreen : Screen {
  @OptIn(ExperimentalMaterial3Api::class)
  @Composable
  override fun Content() {
    val backstack = LocalBackStack.current
    val context = LocalContext.current
    var running by remember { mutableStateOf(OpenListRuntimeState.status == "running") }
    var autoStart by remember { mutableStateOf(OpenListController.isAutoStart(context)) }
    var showHelp by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
      while (true) {
        running = OpenListRuntimeState.status == "running"
        delay(500)
      }
    }

    fun openWeb() {
      try {
        context.startActivity(Intent(context, OpenListActivity::class.java))
      } catch (t: Throwable) {
      }
    }

    fun openUrl(url: String) {
      try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
      } catch (t: Throwable) {
      }
    }

    Scaffold(
      topBar = {
        TopAppBar(
          title = {
            Text(
              text = "OpenList 管理",
              style = MaterialTheme.typography.headlineSmall,
              fontWeight = FontWeight.ExtraBold,
              color = MaterialTheme.colorScheme.primary,
            )
          },
          navigationIcon = {
            IconButton(onClick = backstack::removeLastOrNull) {
              Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = null)
            }
          },
          actions = {
            IconButton(onClick = { showHelp = true }) {
              Icon(Icons.Outlined.Info, contentDescription = "帮助")
            }
          },
        )
      },
    ) { padding ->
      ProvidePreferenceLocals {
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
          item {
            Surface(
              modifier = Modifier.fillMaxWidth().padding(16.dp),
              shape = RoundedCornerShape(20.dp),
              color = MaterialTheme.colorScheme.surfaceContainer,
            ) {
              Text(
                text = if (running) "服务运行中" else "服务未运行",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.titleMedium,
              )
            }
          }

          item {
            Surface(
              modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
              shape = RoundedCornerShape(20.dp),
              color = MaterialTheme.colorScheme.surfaceContainer,
            ) {
              Text(
                text = "初次使用？点击了解如何像 Rclone 一样挂载网盘与本地流媒体",
                modifier = Modifier.padding(16.dp),
              )
            }
          }

          item { PreferenceSectionHeader(title = "服务控制") }

          item {
            Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
              SwitchPreference(
                value = running,
                onValueChange = {
                  if (it) OpenListController.start(context) else OpenListController.stop(context)
                  running = it
                },
                title = { Text("开启 OpenList 服务") },
                summary = { Text("实时启动或停止 OpenList 后台服务") },
              )
              PreferenceDivider()
              SwitchPreference(
                value = autoStart,
                onValueChange = {
                  OpenListController.setAutoStart(context, it)
                  autoStart = it
                },
                title = { Text("启动 APP 自动开启服务") },
                summary = { Text("每次打开应用时自动在后台启动 OpenList 服务") },
              )
            }
          }

          item { PreferenceSectionHeader(title = "核心管理") }

          item {
            Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
              Preference(
                enabled = running,
                title = { Text("设置管理员密码") },
                summary = { Text("默认账号 admin，初始密码 123456。点击进入管理后台修改") },
                icon = { Icon(Icons.Outlined.Code, contentDescription = null) },
                onClick = { openWeb() },
              )
              PreferenceDivider()
              Preference(
                enabled = running,
                title = { Text("用户管理") },
                summary = { Text("管理用户及权限。如需挂载 WebDAV，请开启 WebDAV 读写权限") },
                icon = { Icon(Icons.Outlined.Info, contentDescription = null) },
                onClick = { openWeb() },
              )
              PreferenceDivider()
              Preference(
                enabled = running,
                title = { Text("存储管理") },
                summary = { Text("管理已挂载的网盘、本地存储与远程驱动") },
                icon = { Icon(Icons.Outlined.Folder, contentDescription = null) },
                onClick = { openWeb() },
              )
              PreferenceDivider()
              Preference(
                enabled = running,
                title = { Text("添加存储") },
                summary = { Text("快速添加阿里云盘、百度网盘、OneDrive、WebDAV 等存储源") },
                icon = { Icon(Icons.Filled.Cloud, contentDescription = null) },
                onClick = { openWeb() },
              )
            }
          }

          item { PreferenceSectionHeader(title = "关于与文档") }

          item {
            Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
              Preference(
                title = { Text("关于 OpenList") },
                summary = { Text("了解 OpenList 聚合文件列表服务的版本与系统信息") },
                icon = { Icon(Icons.Outlined.Info, contentDescription = null) },
                onClick = { openUrl("https://github.com/OpenListTeam/OpenList") },
              )
              PreferenceDivider()
              Preference(
                title = { Text("OpenList 官方文档") },
                summary = { Text("查看 OpenList 官方配置教程、挂载手册与使用指南") },
                icon = { Icon(Icons.Outlined.Folder, contentDescription = null) },
                onClick = { openUrl("https://openlist.team/") },
              )
              PreferenceDivider()
              Preference(
                title = { Text("GitHub 开源项目") },
                summary = { Text("访问 OpenList 开源项目源码与社区支持") },
                icon = { Icon(Icons.Outlined.Code, contentDescription = null) },
                onClick = { openUrl("https://github.com/OpenListTeam/OpenList") },
              )
            }
          }
        }
      }

      if (showHelp) {
        AlertDialog(
          onDismissRequest = { showHelp = false },
          title = { Text("OpenList 使用说明") },
          text = {
            Text(
              "1. 开启 OpenList 服务。\n" +
              "2. 设置管理员密码，初始账号 admin，初始密码 123456。\n" +
              "3. 添加存储并开启 WebDAV 权限。\n" +
              "4. 在本机网络中添加 WebDAV：http://127.0.0.1:5244/dav"
            )
          },
          confirmButton = { TextButton(onClick = { showHelp = false }) { Text("知道了") } },
        )
      }
    }
  }
}
