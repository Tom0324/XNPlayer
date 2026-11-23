package app.marlboroadvance.mpvex.openlist

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.LinearLayout

class OpenListActivity : Activity() {
  private lateinit var web: WebView
  private lateinit var status: TextView
  private var started = false

  @SuppressLint("SetJavaScriptEnabled")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val root = LinearLayout(this).apply {
      orientation = LinearLayout.VERTICAL
      layoutParams = ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT
      )
    }
    status = TextView(this).apply { text = "正在启动 OpenList 服务..." }
    web = WebView(this)
    web.layoutParams = FrameLayout.LayoutParams(
      ViewGroup.LayoutParams.MATCH_PARENT,
      0,
    )
    web.settings.javaScriptEnabled = true
    web.settings.domStorageEnabled = true
    web.settings.allowFileAccess = true
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
      web.settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
    }
    var retry = 0
    web.webViewClient = object : WebViewClient() {
      override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
        if (request == null || request.isForMainFrame) {
          status.text = "OpenList 页面加载失败，正在重试..."
          if (retry < 8) {
            retry++
            view?.postDelayed({ view.reload() }, 1000)
          }
        }
      }
    }
    val retryButton = Button(this).apply { text = "重试" }
    retryButton.setOnClickListener { startOpenListAndLoad() }
    root.addView(status)
    root.addView(retryButton)
    root.addView(web)
    setContentView(root)
    startOpenListAndLoad()
  }

  private fun startOpenListAndLoad() {
    try {
      startForegroundService(Intent(this, AlistService::class.java))
    } catch (t: Throwable) {
      status.text = "启动服务失败：" + t.toString()
      return
    }
    if (started) return
    started = true
    var attempts = 0
    val checker = object : Runnable {
      override fun run() {
        attempts++
        when (OpenListRuntimeState.status) {
          "running" -> {
            status.text = "OpenList 服务已启动"
            web.loadUrl("http://127.0.0.1:5244")
            return
          }
          "error" -> {
            status.text = "OpenList 启动失败：" + (OpenListRuntimeState.error ?: "未知错误")
            started = false
            return
          }
          else -> {
            status.text = "正在启动 OpenList 服务... ($attempts/20)"
            if (attempts < 20) web.postDelayed(this, 500)
            else {
              status.text = "OpenList 启动超时，请点重试"
              started = false
            }
          }
        }
      }
    }
    web.postDelayed(checker, 300)
  }
}
