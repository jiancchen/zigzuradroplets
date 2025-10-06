package com.zigzura.droplets.cache

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.asImageBitmap
import com.zigzura.droplets.utils.ScreenshotUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Optimized image loading utility
object OptimizedImageLoader {
    suspend fun loadThumbnail(
        context: android.content.Context,
        appId: String,
        maxWidth: Int = 300,
        maxHeight: Int = 200
    ): androidx.compose.ui.graphics.ImageBitmap? = withContext(Dispatchers.IO) {
        val cacheKey = "${appId}_${maxWidth}_${maxHeight}"

        // Check cache first
        ImageCache.get(cacheKey)?.let { return@withContext it }

        // Load and resize image
        val screenshotFile = ScreenshotUtils.getScreenshotFile(context, appId)
        screenshotFile?.let { file ->
            try {
                // First, get image dimensions without loading full image
                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                BitmapFactory.decodeFile(file.absolutePath, options)

                // Calculate sample size for downscaling
                val sampleSize = calculateInSampleSize(options, maxWidth, maxHeight)

                // Load the scaled-down image
                val loadOptions = BitmapFactory.Options().apply {
                    inSampleSize = sampleSize
                    inJustDecodeBounds = false
                    inPreferredConfig = android.graphics.Bitmap.Config.RGB_565 // Use less memory
                }

                val bitmap = BitmapFactory.decodeFile(file.absolutePath, loadOptions)
                bitmap?.let {
                    val imageBitmap = it.asImageBitmap()
                    ImageCache.put(cacheKey, imageBitmap)
                    imageBitmap
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
}