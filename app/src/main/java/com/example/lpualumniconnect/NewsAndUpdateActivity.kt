package com.example.lpualumniconnect

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient

class NewsAndUpdateActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news_and_update)

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            var newVisibility = visibility
            newVisibility = visibility or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_FULLSCREEN
            window.decorView.systemUiVisibility = newVisibility
        }

        webView = findViewById(R.id.news_webView)
        webView.webViewClient = WebViewClient()

        webView.webChromeClient = WebChromeClient()

        webView.loadUrl("https://alumni.lpu.in/news?tag=")
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}