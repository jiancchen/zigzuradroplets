package com.zigzura.droplets.utils

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.webkit.WebView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object ScreenshotUtils {
    private const val TAG = "ScreenshotUtils"
    private const val SCREENSHOTS_DIR = "screenshots"

    /**
     * Captures a screenshot of the WebView and saves it as PNG
     * @param context Application context
     * @param webView The WebView to capture
     * @param appId Unique identifier for the app
     * @return Path to saved screenshot file or null if failed
     */
    suspend fun captureWebViewScreenshot(
        context: Context,
        webView: WebView,
        appId: String
    ): String? = withContext(Dispatchers.IO) {
        try {
            // Create screenshots directory if it doesn't exist
            val screenshotsDir = File(context.filesDir, SCREENSHOTS_DIR)
            if (!screenshotsDir.exists()) {
                screenshotsDir.mkdirs()
            }

            // Generate filename
            val filename = "screenshot_$appId.png"
            val file = File(screenshotsDir, filename)

            // Capture screenshot on main thread
            val bitmap = withContext(Dispatchers.Main) {
                try {
                    // Create bitmap of the WebView
                    val bitmap = Bitmap.createBitmap(
                        webView.width,
                        webView.height,
                        Bitmap.Config.ARGB_8888
                    )
                    val canvas = android.graphics.Canvas(bitmap)
                    webView.draw(canvas)
                    bitmap
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to capture WebView bitmap", e)
                    null
                }
            }

            if (bitmap != null) {
                // Save bitmap to file
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                bitmap.recycle()

                Log.d(TAG, "Screenshot saved: ${file.absolutePath}")
                file.absolutePath
            } else {
                Log.e(TAG, "Failed to create bitmap")
                null
            }
        } catch (e: IOException) {
            Log.e(TAG, "Failed to save screenshot", e)
            null
        }
    }

    /**
     * Gets the screenshot file for an app if it exists
     * @param context Application context
     * @param appId Unique identifier for the app
     * @return File object if screenshot exists, null otherwise
     */
    fun getScreenshotFile(context: Context, appId: String): File? {
        val screenshotsDir = File(context.filesDir, SCREENSHOTS_DIR)
        val filename = "screenshot_$appId.png"
        val file = File(screenshotsDir, filename)

        return if (file.exists()) file else null
    }

    /**
     * Deletes the screenshot file for an app
     * @param context Application context
     * @param appId Unique identifier for the app
     * @return true if deleted successfully, false otherwise
     */
    fun deleteScreenshot(context: Context, appId: String): Boolean {
        val file = getScreenshotFile(context, appId)
        return file?.delete() ?: false
    }

    /**
     * Clears all screenshot files
     * @param context Application context
     */
    fun clearAllScreenshots(context: Context) {
        val screenshotsDir = File(context.filesDir, SCREENSHOTS_DIR)
        if (screenshotsDir.exists()) {
            screenshotsDir.listFiles()?.forEach { file ->
                if (file.name.startsWith("screenshot_") && file.name.endsWith(".png")) {
                    file.delete()
                }
            }
        }
    }
}
