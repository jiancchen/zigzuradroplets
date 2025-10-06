package com.zigzura.droplets.ui.components

import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import android.widget.VideoView
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun ThreeDImageCard(
    historyItem: PromptHistory,
    rotationX: Float,
    rotationY: Float,
    backgroundColor: Color = Color.Black,
    modifier: Modifier = Modifier,
    onNavigateToApp: (String) -> Unit = {},
    isCurrentlyGenerating: Boolean = false, // Add generation state parameter
    onShowSnackbar: (String) -> Unit = {} // Add snackbar callback
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

    // Check if this item is generating - either currently generating or has "GENERATING..." HTML
    val isGenerating = isCurrentlyGenerating || historyItem.html == "GENERATING..."

    // Check if this item is new (never accessed and not generating)
    val isNewItem = (historyItem.accessCount ?: 0) < 1 && !isGenerating

    // Log for debugging
    Log.d("ThreeDImageCard", "Item ${historyItem.id}: isCurrentlyGenerating=$isCurrentlyGenerating, html=${historyItem.html}, isGenerating=$isGenerating")

    // Video fade animation
    val videoAlpha by animateFloatAsState(
        targetValue = if (isGenerating) 1f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "videoFade"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                this.rotationX = rotationX
                this.rotationY = rotationY
                cameraDistance = 12 * density
            }
            .clip(RoundedCornerShape(16.dp))
    ) {
        // Background image, video, or color
        when {
            isGenerating && screenshotBitmap == null -> {
                // Video background for generating items without screenshots
                Log.d("ThreeDImageCard", "Loading generating video background for item ${historyItem.id}")
                AndroidView(
                    factory = { context ->
                        VideoView(context).apply {
                            // Set up the video from assets
                            val uri = Uri.parse("android.resource://${context.packageName}/raw/generating_video")
                            setVideoURI(uri)

                            // Configure video playback
                            setOnPreparedListener { mediaPlayer ->
                                mediaPlayer.isLooping = true
                                mediaPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING)
                                // Mute the video by setting volume to 0 instead of setting AudioAttributes to null
                                mediaPlayer.setVolume(0f, 0f)
                                start()
                            }

                            // Start immediately if possible
                            start()
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp))
                        .graphicsLayer { alpha = videoAlpha }, // Apply video fade animation
                )
            }
            screenshotBitmap != null -> {
                Image(
                    bitmap = screenshotBitmap!!,
                    contentDescription = "App screenshot",
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { alpha = 0.7f }, // 70% opacity for image
                    contentScale = ContentScale.Crop
                )
            }
            isLoading -> {
                // Show loading state with background color
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

        // NEW badge for unused items
        if (isNewItem) {
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