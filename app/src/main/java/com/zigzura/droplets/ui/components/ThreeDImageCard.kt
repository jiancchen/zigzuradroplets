package com.zigzura.droplets.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zigzura.droplets.cache.OptimizedImageLoader
import com.zigzura.droplets.data.PromptHistory

@Composable
fun GoldBadgeProgress(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(32.dp)
            .background(Color(0xFFFFD700), shape = CircleShape), // Gold color
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = Color.White,
            strokeWidth = 2.dp,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
fun RedBadgeHeart(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(32.dp)
            .background(Color(0xFFE91E63), shape = CircleShape), // Pink/Red color for favorites
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = "Favorite",
            tint = Color.White,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
fun ThreeDImageCard(
    historyItem: PromptHistory,
    rotationX: Float,
    rotationY: Float,
    backgroundColor: Color = Color.Black,
    modifier: Modifier = Modifier,
    onNavigateToApp: (String) -> Unit = {},
    isCurrentlyGenerating: Boolean = false,
    onShowSnackbar: (String) -> Unit = {}
) {
    val context = LocalContext.current

    // Async thumbnail loading with state management
    var screenshotBitmap by remember(historyItem.id) { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }
    var isLoading by remember(historyItem.id) { mutableStateOf(true) }

    // Load thumbnail asynchronously
    LaunchedEffect(historyItem.id) {
        screenshotBitmap = OptimizedImageLoader.loadThumbnail(
            context = context,
            appId = historyItem.id,
            maxWidth = 300,
            maxHeight = 200
        )
        isLoading = false
    }

    val displayTitle = historyItem.title?.takeIf { it.isNotBlank() }
        ?: (historyItem.prompt.take(50) + "...")

    // Check if this item is generating
    val isGenerating = isCurrentlyGenerating || historyItem.html == "GENERATING..."

    // Check if this item is new (never accessed and not generating)
    val isNewItem = (historyItem.accessCount ?: 0) < 1 && !isGenerating

    // Check if this item is a favorite
    val isFavorite = historyItem.favorite == true

    // Check if item was recently completed (has screenshot but was previously generating)
    val isRecentlyCompleted = screenshotBitmap != null && !isGenerating && !isNewItem && !isFavorite

    Box(
        modifier = modifier
            .graphicsLayer {
                this.rotationX = rotationX
                this.rotationY = rotationY
                cameraDistance = 12 * density
            }
            .clip(RoundedCornerShape(16.dp))
    ) {
        // Background image or color - VideoView completely removed
        when {
            screenshotBitmap != null -> {
                Image(
                    bitmap = screenshotBitmap!!,
                    contentDescription = "App screenshot",
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { alpha = 0.7f },
                    contentScale = ContentScale.Crop
                )
            }
            isLoading || isGenerating -> {
                // Show background color during loading or generating
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(backgroundColor.copy(alpha = 0.3f))
                )
            }
            else -> {
                // Fallback to solid background color when no image
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(backgroundColor)
                )
            }
        }

        // Black overlay for text readability and click handling
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.8f)
                        ),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY
                    )
                )
                .clickable {
                    // Show snackbar message when clicking on generating items
                    if (isGenerating) {
                        onShowSnackbar("Item is still generating, please wait...")
                    } else {
                        onNavigateToApp(historyItem.id)
                    }
                }
        )

        // Badge system: NEW, GENERATING, FAVORITE, or COMPLETED
        when {
            isNewItem -> {
                // RED "NEW" badge for unused items
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    androidx.compose.material3.Card(
                        colors = androidx.compose.material3.CardDefaults.cardColors(
                            containerColor = Color(0xFFEF4444)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Text(
                            text = "NEW",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
            isGenerating -> {
                // GOLD badge with spinning progress indicator for generating items
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    GoldBadgeProgress()
                }
            }
            isFavorite -> {
                // PINK/RED badge with heart for favorite items
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    RedBadgeHeart()
                }
            }
        }

        // Text content with improved contrast
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.BottomStart
        ) {
            Text(
                text = displayTitle,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                style = MaterialTheme.typography.headlineSmall.copy(
                    shadow = Shadow(
                        color = Color.Black,
                        offset = Offset(2f, 2f),
                        blurRadius = 4f
                    )
                )
            )
        }
    }
}