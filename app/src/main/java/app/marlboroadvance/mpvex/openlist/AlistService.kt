package app.marlboroadvance.mpvex.openlist

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import openlistlib.Event
import openlistlib.LogCallback
import openlistlib.Openlistlib
import java.util.concurrent.atomic.AtomicBoolean

class AlistService : Service() {
  companion object {
    private const val TAG = "AlistService"
    private const val CHANNEL_ID = "neoplayer_openlist"
    private const val NOTIFICATION_ID = 2001
    private val initialized = AtomicBoolean(false)
    private val started = AtomicBoolean(false)
  }

  private val event = object : Event {
    override fun onProcessExit(code: Long) { Log.w(TAG, "OpenList process exit: $code") }
    override fun onShutdown(reason: String) { Log.w(TAG, "OpenList shutdown: $reason") }
    override fun onStartError(a: String, b: String) { Log.e(TAG, "OpenList start error: $a $b") }
  }

  private val logCallback = object : LogCallback {
    override fun onLog(level: Short, time: Long, msg: String) {
      Log.d(TAG, msg)
    }
  }

  override fun onBind(intent: Intent?): IBinder? = null

  override fun onCreate() {
    super.onCreate()
    createChannel()
    startForeground(NOTIFICATION_ID, buildNotification())
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    Thread { startOpenListSafely() }.start()
    return START_STICKY
  }

  private fun startOpenListSafely() {
    try {
      if (!initialized.get()) {
        Openlistlib.init(event, logCallback)
        initialized.set(true)
      }
      if (!started.get()) {
        Openlistlib.start()
        started.set(true)
      }
    } catch (t: Throwable) {
      Log.e(TAG, "OpenList init/start failed", t)
    }
  }

  private fun createChannel() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
    val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    if (nm.getNotificationChannel(CHANNEL_ID) == null) {
      val channel = NotificationChannel(CHANNEL_ID, "OpenList 服务", NotificationManager.IMPORTANCE_LOW)
      nm.createNotificationChannel(channel)
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
