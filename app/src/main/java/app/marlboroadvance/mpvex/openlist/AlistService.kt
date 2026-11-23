package app.marlboroadvance.mpvex.openlist

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import alistlib.Alistlib
import alistlib.Event
import alistlib.LogCallback
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

object OpenListController {
  private const val PREFS = "openlist_settings"
  private const val KEY_AUTO_START = "auto_start"

  fun isAutoStart(context: Context): Boolean =
    context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_AUTO_START, false)

  fun setAutoStart(context: Context, value: Boolean) {
    context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
      .putBoolean(KEY_AUTO_START, value).apply()
  }

  fun start(context: Context) {
    try {
      context.startForegroundService(Intent(context, AlistService::class.java))
    } catch (t: Throwable) {
      OpenListRuntimeState.status = "error"
      OpenListRuntimeState.error = t.toString()
    }
  }

  fun stop(context: Context) {
    try {
      Alistlib.shutdown(5000L)
    } catch (t: Throwable) {
      Log.w("OpenListController", "OpenList shutdown failed", t)
    }
    OpenListRuntimeState.status = "idle"
    OpenListRuntimeState.error = null
    try {
      context.stopService(Intent(context, AlistService::class.java))
    } catch (t: Throwable) {
      Log.w("OpenListController", "Stop service failed", t)
    }
  }
}

object OpenListRuntimeState {
  @Volatile var status: String = "idle"
  @Volatile var error: String? = null
}

class AlistService : Service() {
  companion object {
    private const val TAG = "AlistService"
    private const val CHANNEL_ID = "neoplayer_openlist"
    private const val NOTIFICATION_ID = 2001
    private val initialized = AtomicBoolean(false)
  }

  private val event = object : Event {
    override fun onShutdown(reason: String) {
      OpenListRuntimeState.status = "error"
      OpenListRuntimeState.error = "OpenList shutdown: $reason"
    }
    override fun onStartError(a: String, b: String) {
      OpenListRuntimeState.status = "error"
      OpenListRuntimeState.error = "$a $b"
    }
  }

  private val logCallback = object : LogCallback {
    override fun onLog(level: Short, msg: String) { Log.d(TAG, msg) }
  }

  override fun onBind(intent: Intent?): IBinder? = null

  override fun onCreate() {
    super.onCreate()
    createChannel()
    try {
      if (android.os.Build.VERSION.SDK_INT >= 34) {
        startForeground(
          NOTIFICATION_ID,
          buildNotification(),
          ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
        )
      } else {
        @Suppress("DEPRECATION")
        startForeground(NOTIFICATION_ID, buildNotification())
      }
    } catch (t: Throwable) {
      Log.e(TAG, "startForeground failed", t)
      stopSelf()
    }
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    if (OpenListRuntimeState.status != "running") {
      OpenListRuntimeState.status = "starting"
      OpenListRuntimeState.error = null
      Thread { startOpenListSafely() }.start()
    }
    return START_STICKY
  }

  private fun startOpenListSafely() {
    try {
      if (!initialized.get()) {
        // Match AListLiteAndroid: internal filesDir/data avoids Go/SELinux issues.
        val base = File(filesDir, "data").apply { mkdirs() }
        Alistlib.setConfigData(base.absolutePath)
        Alistlib.setConfigLogStd(true)
        Alistlib.init(event, logCallback)
        initialized.set(true)
      }
      if (!Alistlib.isRunning("")) {
        Alistlib.start()
      }
      OpenListRuntimeState.status = "running"
      OpenListRuntimeState.error = null
    } catch (t: Throwable) {
      OpenListRuntimeState.status = "error"
      OpenListRuntimeState.error = t.toString()
      Log.e(TAG, "OpenList init/start failed", t)
    }
  }

  private fun createChannel() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
    val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    if (nm.getNotificationChannel(CHANNEL_ID) == null) {
      nm.createNotificationChannel(NotificationChannel(CHANNEL_ID, "OpenList 服务", NotificationManager.IMPORTANCE_LOW))
    }
  }

  private fun buildNotification(): Notification {
    val pi = PendingIntent.getActivity(
      this, 0, Intent(this, OpenListActivity::class.java),
      PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )
    val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      Notification.Builder(this, CHANNEL_ID)
    } else {
      @Suppress("DEPRECATION") Notification.Builder(this)
    }
    return builder
      .setSmallIcon(android.R.drawable.stat_sys_download)
      .setContentTitle("OpenList 服务")
      .setContentText("本地网盘服务运行中")
      .setContentIntent(pi)
      .setOngoing(true)
      .build()
  }
}
