package com.zigzura.droplets

import android.util.Log
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.zigzura.droplets.utils.ScreenshotUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID
import java.security.MessageDigest

@Composable
fun Weblet(
    paddingValues: PaddingValues = PaddingValues(0.dp),
    url: String? = null,
    htmlContent: String? = null,
    appId: String, // Required - no fallback needed since PromptHistory always has an ID
    onScreenshotCaptured: ((String?) -> Unit)? = null // Callback for when screenshot is captured
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Use the provided appId directly - no fallback needed
    val webAppId = remember(appId) { appId }

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
            setBackgroundColor(0x00000000)
            setLayerType(View.LAYER_TYPE_HARDWARE, null)
            isVerticalScrollBarEnabled = false
            // Add JavaScript interface for storage and reminders - initial setup
            addJavascriptInterface(WebAppInterface(context, webAppId), "Android")

            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    Log.d("Weblet", "Page finished loading for appId: $webAppId")

                    // Capture screenshot after a short delay to ensure content is rendered
                    view?.let { webView ->
                        coroutineScope.launch {
                            delay(1000) // Wait for content to fully render
                            val screenshotPath = ScreenshotUtils.captureWebViewScreenshot(
                                context, webView, webAppId
                            )
                            onScreenshotCaptured?.invoke(screenshotPath)
                        }
                    }
                }
            }
        }
    }

    // Update WebAppInterface and load content when appId or content changes
    LaunchedEffect(webAppId, htmlContent, url) {
        Log.d("Weblet", "Updating WebAppInterface for appId: $webAppId")

        // FIRST: Update the WebAppInterface
        try {
            webView.removeJavascriptInterface("Android")
        } catch (e: Exception) {
            // Ignore if removeJavascriptInterface is not available
        }
        webView.addJavascriptInterface(WebAppInterface(context, webAppId), "Android")

        // THEN: Load the content (ensuring the interface is ready)
        when {
            !htmlContent.isNullOrBlank() -> {
                Log.d("Weblet", "Loading HTML content for appId: $webAppId")
                webView.loadDataWithBaseURL(
                    "https://localhost/",
                    htmlContent,
                    "text/html",
                    "UTF-8",
                    null
                )
            }
            !url.isNullOrBlank() -> {
                Log.d("Weblet", "Loading URL: $url for appId: $webAppId")
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
            .padding(paddingValues),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        AndroidView(
            factory = { webView },
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
                .clip(RoundedCornerShape(16.dp)),
            update = { view ->
                Log.d("Weblet", "AndroidView update called")
            }
        )
    }
}
