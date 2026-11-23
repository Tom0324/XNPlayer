package app.marlboroadvance.mpvex.openlist

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient

class OpenListActivity : Activity() {
  @SuppressLint("SetJavaScriptEnabled")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    try {
      startForegroundService(Intent(this, AlistService::class.java))
    } catch (t: Throwable) {
      // 云朵入口不能因为服务启动失败而崩溃
    }

    val web = WebView(this)
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
          if (retry < 8) {
            retry++
            view?.postDelayed({ view.reload() }, 1000)
          }
        }
      }
    }
    setContentView(web)
    web.postDelayed({ web.loadUrl("http://127.0.0.1:5244") }, 1200)
  }
}
