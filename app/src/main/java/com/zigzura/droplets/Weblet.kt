package com.zigzura.droplets

import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun Weblet(paddingValues: PaddingValues, url: String? = null, htmlContent: String? = null) {
    val context = LocalContext.current
    val webView = remember {
        WebView(context).apply {
            settings.javaScriptEnabled = true // Enable JavaScript
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    // Page finished loading
                }
            }
        }
    }

    // Load content based on parameters
    LaunchedEffect(htmlContent, url) {
        when {
            !htmlContent.isNullOrBlank() -> {
                webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
            }
            !url.isNullOrBlank() -> {
                webView.loadUrl(url)
            }
            else -> {
                // Load default content
                val customHtml = """
                    <html>
                    <head>
                        <meta name="viewport" content="width=device-width, initial-scale=1.0">
                        <script>
                            function greet(name) {
                                return "Hello, " + name + "!";
                            }
                        </script>
                    </head>
                    <body>
                        <h1>Custom HTML in WebView</h1>
                        <button onclick="alert(greet('Android'))">Say Hello</button>
                    </body>
                    </html>
                """.trimIndent()
                webView.loadDataWithBaseURL(null, customHtml, "text/html", "UTF-8", null)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().padding(paddingValues)){
        AndroidView(factory = { webView }, modifier = Modifier.fillMaxSize())
    }
}
