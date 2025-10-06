package com.zigzura.droplets.cache

import java.util.concurrent.ConcurrentHashMap

// Global image cache to store thumbnails across recompositions
object ImageCache {
    private val cache = ConcurrentHashMap<String, androidx.compose.ui.graphics.ImageBitmap>()

    fun get(key: String) = cache[key]
    fun put(key: String, bitmap: androidx.compose.ui.graphics.ImageBitmap) {
        // Limit cache size to prevent memory issues
        if (cache.size >= 50) {
            cache.clear() // Simple cache eviction
        }
        cache[key] = bitmap
    }
}