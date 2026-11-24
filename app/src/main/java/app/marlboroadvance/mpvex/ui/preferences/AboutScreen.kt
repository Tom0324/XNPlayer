package app.marlboroadvance.mpvex.ui.preferences

import android.content.Intent
import android.media.MediaCodecInfo
import android.media.MediaCodecList
import android.content.pm.PackageManager
import android.widget.ImageView
import android.widget.Toast
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import app.marlboroadvance.mpvex.BuildConfig
import app.marlboroadvance.mpvex.R
import app.marlboroadvance.mpvex.presentation.Screen
import app.marlboroadvance.mpvex.presentation.crash.CrashActivity.Companion.collectDeviceInfo
import app.marlboroadvance.mpvex.ui.utils.LocalBackStack
import app.marlboroadvance.mpvex.utils.update.UpdateViewModel
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import com.mikepenz.aboutlibraries.ui.compose.produceLibraries
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.Serializable

@Serializable
object AboutScreen : Screen {
  @Suppress("DEPRECATION")
  @OptIn(ExperimentalMaterial3Api::class)
  @Composable
  override fun Content() {
    val context = LocalContext.current
    val backstack = LocalBackStack.current
    val clipboardManager = LocalClipboardManager.current
    val packageManager: PackageManager = context.packageManager
    val packageInfo = packageManager.getPackageInfo(context.packageName, 0)
    val versionName = packageInfo.versionName?.substringBefore('-') ?: packageInfo.versionName ?: BuildConfig.VERSION_NAME
    val buildType = BuildConfig.BUILD_TYPE

    // Conditionally initialize update feature based on build config
    val updateViewModel: UpdateViewModel? = if (BuildConfig.ENABLE_UPDATE_FEATURE) {
      viewModel(context as androidx.activity.ComponentActivity)
    } else {
      null
    }
    val updateState by (updateViewModel?.updateState ?: MutableStateFlow(UpdateViewModel.UpdateState.Idle)).collectAsState()

    // Show toast when no update is available after manual check (only if update feature is enabled)
    LaunchedEffect(updateState) {
        if (BuildConfig.ENABLE_UPDATE_FEATURE && updateViewModel != null && updateState is UpdateViewModel.UpdateState.NoUpdate) {
            Toast.makeText(context, "Already using latest version", Toast.LENGTH_SHORT).show()
            updateViewModel.dismissNoUpdate()
        }
    }

    Scaffold(
      topBar = {
        TopAppBar(
          title = { 
            Text(
              text = stringResource(id = R.string.pref_about_title),
              style = MaterialTheme.typography.headlineSmall,
              fontWeight = FontWeight.ExtraBold,
              color = MaterialTheme.colorScheme.primary,
            ) 
          },
          navigationIcon = {
            IconButton(onClick = backstack::removeLastOrNull) {
              Icon(
                imageVector = Icons.AutoMirrored.Default.ArrowBack, 
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
              )
            }
          },
        )
      },
    ) { paddingValues ->
      val cs = MaterialTheme.colorScheme
      val colorPrimary = cs.primaryContainer
      val colorTertiary = cs.tertiaryContainer
      val transition = rememberInfiniteTransition()
      val fraction by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec =
          infiniteRepeatable(
            animation = tween(durationMillis = 5000),
            repeatMode = RepeatMode.Reverse,
          ),
      )
      val cornerRadius = 28.dp
      
      Column(
        modifier =
          Modifier
            .padding(paddingValues)
            .verticalScroll(rememberScrollState()),
      ) {
        PreferenceCard {
          Box(
            modifier =
              Modifier
                .drawWithCache {
                  val cx = size.width - size.width * fraction
                  val cy = size.height * fraction

                  val gradient =
                    Brush.radialGradient(
                      colors = listOf(colorPrimary, colorTertiary),
                      center = Offset(cx, cy),
                      radius = 800f,
                    )

                  onDrawBehind {
                    drawRoundRect(
                      brush = gradient,
                      cornerRadius =
                        CornerRadius(
                          cornerRadius.toPx(),
                          cornerRadius.toPx(),
                        ),
                    )
                  }
                }
                .padding(16.dp),
          ) {
            Column {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(64.dp)) {
                  AndroidView(
                    modifier = Modifier.matchParentSize(),
                    factory = { ctx ->
                      ImageView(ctx).apply {
                        setImageResource(R.mipmap.ic_launcher)
                      }
                    },
                  )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = "XNPlayer",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = cs.onPrimaryContainer,
                  )
                  Spacer(Modifier.height(4.dp))
                  Text(
                    text = "v$versionName $buildType",
                    style = MaterialTheme.typography.bodyMedium,
                    color = cs.onPrimaryContainer.copy(alpha = 0.85f),
                  )
                }
              }

              Spacer(modifier = Modifier.height(20.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
              ) {
                val btnContainer = cs.primary
                val btnContent = cs.onPrimary
                Button(
                  onClick = { backstack.add(LibrariesScreen) },
                  modifier =
                    Modifier
                      .weight(1f)
                      .height(56.dp),
                  shape = RoundedCornerShape(16.dp),
                  colors =
                    ButtonDefaults.buttonColors(
                      containerColor = btnContainer,
                      contentColor = btnContent,
                    ),
                ) {
                  Text(
                    text = stringResource(id = R.string.pref_about_oss_libraries),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                  )
                }

                Button(
                  onClick = {
                    context.startActivity(
                      Intent(
                        Intent.ACTION_VIEW,
                        context.getString(R.string.github_repo_url).toUri(),
                      ),
                    )
                  },
                  modifier =
                    Modifier
                      .weight(1f)
                      .height(56.dp),
                  shape = RoundedCornerShape(16.dp),
                  colors =
                    ButtonDefaults.buttonColors(
                      containerColor = btnContainer,
                      contentColor = btnContent,
                    ),
                ) {
                  Text(
                    text = "GitHub",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                  )
                }
              }

              Spacer(modifier = Modifier.height(20.dp))

              Column(
                modifier =
                  Modifier
                    .fillMaxWidth()
                    .clickable {
                      clipboardManager.setText(AnnotatedString(collectDeviceInfo()))
                    },
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.padding(bottom = 8.dp),
                ) {
                  Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = "设备与软件信息",
                    modifier = Modifier.size(20.dp),
                    tint = cs.onPrimaryContainer,
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = "设备与软件信息",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = cs.onPrimaryContainer,
                  )
                }
                Text(
                  text = "软件：XNPlayer v$versionName $buildType (${BuildConfig.VERSION_CODE})",
                  style = MaterialTheme.typography.bodySmall,
                  color = cs.onPrimaryContainer.copy(alpha = 0.85f),
                )
                Text(
                  text = "包名：${context.packageName}",
                  style = MaterialTheme.typography.bodySmall,
                  color = cs.onPrimaryContainer.copy(alpha = 0.85f),
                )
                Text(
                  text = collectDeviceInfo(),
                  style = MaterialTheme.typography.bodySmall,
                  color = cs.onPrimaryContainer.copy(alpha = 0.85f),
                )
              }
            }
          }
        }

        Spacer(Modifier.height(8.dp))

        PreferenceSectionHeader(title = "设备编解码器支持")
        PreferenceCard {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { backstack.add(CodecInfoScreen) }
              .padding(16.dp),
          ) {
            Text(
              text = "动态读取 Android MediaCodecList；HLG 按 Main 10 能力推断",
              style = MaterialTheme.typography.bodyMedium,
              color = cs.outline,
            )
            Spacer(Modifier.height(16.dp))

            Text(
              text = "HDR 功能",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = cs.primary,
            )
            Spacer(Modifier.height(8.dp))
            val codecSupport = rememberCodecSupport()
            CodecSupportRow("HDR10", codecSupport.hdr10)
            CodecSupportRow("HDR10+", codecSupport.hdr10Plus)
            CodecSupportRow("HLG（推断）", codecSupport.hlg)
            CodecSupportRow("杜比视界", codecSupport.dolbyVision)

            PreferenceDivider()

            Text(
              text = "10-bit 配置文件",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = cs.primary,
            )
            Spacer(Modifier.height(8.dp))
            CodecSupportRow("HEVC Main 10", codecSupport.hevcMain10)
            CodecSupportRow("AVC High 10", codecSupport.avcHigh10)

            Spacer(Modifier.height(8.dp))
            Text(
              text = "点击查看系统编解码器列表",
              style = MaterialTheme.typography.bodySmall,
              color = cs.primary,
            )
          }
        }

        Spacer(Modifier.height(12.dp))
      }
    }
  }
}

@Serializable
object CodecInfoScreen : Screen {
  @OptIn(ExperimentalMaterial3Api::class)
  @Composable
  override fun Content() {
    val backstack = LocalBackStack.current
    val codecs = rememberCodecInfoList()
    val hardwareCount = codecs.count { it.hardware }
    val softwareCount = codecs.size - hardwareCount
    val videoCount = codecs.count { it.category == "视频" }
    val audioCount = codecs.count { it.category == "音频" }
    val otherCount = codecs.size - videoCount - audioCount
    Scaffold(
      topBar = {
        TopAppBar(
          title = {
            Text(
              text = "设备编解码器支持",
              style = MaterialTheme.typography.headlineSmall,
              fontWeight = FontWeight.ExtraBold,
              color = MaterialTheme.colorScheme.primary,
            )
          },
          navigationIcon = {
            IconButton(onClick = backstack::removeLastOrNull) {
              Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = null)
            }
          },
        )
      },
    ) { paddingValues ->
      Column(
        modifier = Modifier
          .padding(paddingValues)
          .verticalScroll(rememberScrollState()),
      ) {
        PreferenceCard {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
          ) {
            Text(
              text = "系统编解码器总览",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(4.dp))
            Text(
              text = "动态读取 Android MediaCodecList",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.outline,
            )
            Spacer(Modifier.height(12.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              StatPill("视频", videoCount, MaterialTheme.colorScheme.primaryContainer)
              StatPill("音频", audioCount, MaterialTheme.colorScheme.secondaryContainer)
              StatPill("硬件", hardwareCount, MaterialTheme.colorScheme.tertiaryContainer)
              StatPill("软件", softwareCount, MaterialTheme.colorScheme.surfaceVariant)
            }
          }
        }

        Spacer(Modifier.height(8.dp))
        CodecGroup("视频编解码器", codecs.filter { it.category == "视频" })
        CodecGroup("音频编解码器", codecs.filter { it.category == "音频" })
        if (otherCount > 0) CodecGroup("其他", codecs.filter { it.category == "其他" })
      }
    }
  }
}

@Composable
private fun StatPill(label: String, count: Int, color: Color) {
  Box(
    modifier = Modifier
      .height(36.dp)
      .background(color, RoundedCornerShape(18.dp))
      .padding(horizontal = 12.dp),
    contentAlignment = Alignment.Center,
  ) {
    Text(
      text = "$label $count",
      style = MaterialTheme.typography.labelMedium,
      fontWeight = FontWeight.SemiBold,
      color = MaterialTheme.colorScheme.onSurface,
    )
  }
}

@Composable
private fun CodecGroup(title: String, items: List<CodecInfo>) {
  if (items.isEmpty()) return
  PreferenceSectionHeader(title = "$title（${items.size}）")
  PreferenceCard {
    Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
      items.forEachIndexed { index, codec ->
        if (index > 0) PreferenceDivider()
        CodecInfoRow(codec)
      }
    }
  }
  Spacer(Modifier.height(8.dp))
}

@Composable
private fun CodecInfoRow(codec: CodecInfo) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 8.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Box(
      modifier = Modifier
        .height(24.dp)
        .background(
          if (codec.hardware) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
          RoundedCornerShape(12.dp),
        )
        .padding(horizontal = 10.dp),
      contentAlignment = Alignment.Center,
    ) {
      Text(
        text = if (codec.hardware) "硬" else "软",
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
      )
    }
    Spacer(Modifier.width(10.dp))
    Column(modifier = Modifier.weight(1f)) {
      Text(text = codec.label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
      Text(text = codec.name, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
      Text(text = codec.mime, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
      if (codec.maxDimension > 0) {
        Text(text = "最大 ${codec.maxDimension}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
      }
    }
  }
}

data class CodecInfo(
  val label: String,
  val name: String,
  val mime: String,
  val hardware: Boolean,
  val maxDimension: Int,
  val category: String,
)

private fun codecLabel(mime: String): String = when (mime.lowercase()) {
  "video/avc" -> "H.264 / AVC"
  "video/hevc" -> "H.265 / HEVC"
  "video/x-vnd.on2.vp8" -> "VP8"
  "video/x-vnd.on2.vp9" -> "VP9"
  "video/av01" -> "AV1"
  "video/dolby-vision" -> "Dolby Vision"
  "audio/mp4a-latm" -> "AAC"
  "audio/opus" -> "Opus"
  "audio/mpeg" -> "MP3"
  "audio/flac" -> "FLAC"
  else -> mime.substringAfterLast('/').uppercase()
}

private fun codecCategory(mime: String): String = when {
  mime.startsWith("video/", ignoreCase = true) -> "视频"
  mime.startsWith("audio/", ignoreCase = true) -> "音频"
  else -> "其他"
}

@Composable
private fun rememberCodecInfoList(): List<CodecInfo> = androidx.compose.runtime.remember {
  val result = mutableListOf<CodecInfo>()
  for (info in MediaCodecList(MediaCodecList.ALL_CODECS).codecInfos) {
    if (info.isEncoder) continue
    for (mime in info.supportedTypes) {
      var maxDimension = 0
      try {
        val caps = info.getCapabilitiesForType(mime)
        caps.videoCapabilities?.let { vc ->
          maxDimension = maxOf(vc.supportedWidths.upper, vc.supportedHeights.upper)
        }
      } catch (_: Exception) {
      }
      result.add(
        CodecInfo(
          label = codecLabel(mime),
          name = info.name,
          mime = mime,
          hardware = info.isHardwareAccelerated,
          maxDimension = maxDimension,
          category = codecCategory(mime),
        )
      )
    }
  }
  result.sortedWith(compareBy({ it.category }, { !it.hardware }, { it.label }, { it.name }))
}

@Suppress("DEPRECATION")
@Serializable
object LibrariesScreen : Screen {
  @OptIn(ExperimentalMaterial3Api::class)
  @Composable
  override fun Content() {
    val backstack = LocalBackStack.current
    val context = LocalContext.current
    val libraries by produceLibraries {
      context.resources.openRawResource(R.raw.aboutlibraries).bufferedReader().use { it.readText() }
    }
    Scaffold(
      topBar = {
        TopAppBar(
          title = {
            Text(
              text = stringResource(id = R.string.pref_about_oss_libraries),
              style = MaterialTheme.typography.headlineSmall,
              fontWeight = FontWeight.ExtraBold,
              color = MaterialTheme.colorScheme.primary,
            )
          },
          navigationIcon = {
            IconButton(onClick = backstack::removeLastOrNull) {
              Icon(
                imageVector = Icons.AutoMirrored.Default.ArrowBack,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
              )
            }
          },
        )
      },
    ) { paddingValues ->
      LibrariesContainer(
        libraries = libraries,
        modifier =
          Modifier
            .fillMaxSize()
            .padding(paddingValues),
      )
    }
  }
}
