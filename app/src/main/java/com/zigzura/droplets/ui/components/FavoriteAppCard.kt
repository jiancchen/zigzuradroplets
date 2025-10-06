package com.zigzura.droplets.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zigzura.droplets.cache.OptimizedImageLoader
import com.zigzura.droplets.data.PromptHistory

@Composable
fun FavoriteAppCard(
    historyItem: PromptHistory,
    onNavigateToApp: (String) -> Unit
) {
    val context = LocalContext.current
    var screenshotBitmap by remember(historyItem.id) { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }
    var isLoading by remember(historyItem.id) { mutableStateOf(true) }

    LaunchedEffect(historyItem.id) {
        screenshotBitmap = OptimizedImageLoader.loadThumbnail(
            context = context,
            appId = historyItem.id,
            maxWidth = 120,
            maxHeight = 80
        )
        isLoading = false
    }

    Surface(
        modifier = Modifier
            .size(width = 120.dp, height = 80.dp)
            .clickable { onNavigateToApp(historyItem.id) },
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.8f),
        shadowElevation = 2.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            if (screenshotBitmap != null) {
                Image(
                    bitmap = screenshotBitmap!!,
                    contentDescription = "App screenshot",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.LightGray)
                )
            } else {
                // Fallback when no screenshot available
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Gray.copy(alpha = 0.3f))
                )
            }

            // Dark overlay for text readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
            )

            // App title with proper width and ellipsization - placed AFTER overlay so it renders on top
            Text(
                text = historyItem.title ?: historyItem.prompt,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 14.sp,
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}