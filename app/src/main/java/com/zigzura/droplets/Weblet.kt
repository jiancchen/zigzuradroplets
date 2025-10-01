package com.zigzura.droplets

import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun Weblet(paddingValues: PaddingValues = PaddingValues(0.dp), url: String? = null, htmlContent: String? = null) {
    val context = LocalContext.current

    // Create WebView with proper configuration
    val webView = remember {
        WebView(context).apply {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                loadWithOverviewMode = true
                useWideViewPort = true
                builtInZoomControls = false
                displayZoomControls = false
            }

            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    Log.d("Weblet", "Page finished loading")
                }
            }
        }
    }

    // Load content when parameters change
    LaunchedEffect(htmlContent, url) {
        when {
            !htmlContent.isNullOrBlank() -> {
                Log.d("Weblet", "Loading HTML content: ${htmlContent.take(100)}...")
                webView.loadDataWithBaseURL(
                    "https://localhost/",
                    htmlContent,
                    "text/html",
                    "UTF-8",
                    null
                )
            }
            !url.isNullOrBlank() -> {
                Log.d("Weblet", "Loading URL: $url")
                webView.loadUrl(url)
            }
            else -> {
                // Load default content
                val customHtml = """
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <meta name="viewport" content="width=device-width, initial-scale=1.0">
                        <style>
                            body {
                                font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                                margin: 20px;
                                padding: 20px;
                                background-color: #f5f5f5;
                            }
                            .container {
                                background: white;
                                padding: 20px;
                                border-radius: 8px;
                                box-shadow: 0 2px 4px rgba(0,0,0,0.1);
                            }
                        </style>
                    </head>
                    <body>
                        <div class="container">
                            <h1>🌐 WebView Ready</h1>
                            <p>Enter a prompt below to generate HTML content!</p>
                            <button onclick="alert('WebView is working!')">Test Button</button>
                        </div>
                    </body>
                    </html>
                """.trimIndent()
                webView.loadDataWithBaseURL(
                    "https://localhost/",
                    customHtml,
                    "text/html",
                    "UTF-8",
                    null
                )
            }
        }
    }

    // Wrap WebView in a rounded corner Card that fills the width
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        AndroidView(
            factory = { webView },
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp)),
            update = { view ->
                Log.d("Weblet", "AndroidView update called")
            }
        )
    }
}
