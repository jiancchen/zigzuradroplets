package com.zigzura.droplets.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.net.Uri
import android.widget.VideoView
import com.zigzura.droplets.data.PromptHistory
import com.zigzura.droplets.utils.ScreenshotUtils
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryItem(
    historyItem: PromptHistory,
    onClick: () -> Unit,
    onToggleFavorite: (String) -> Unit = {},
    onUpdateTitle: (String, String) -> Unit = { _, _ -> },
    onDelete: (String) -> Unit = {} // Add delete callback
) {
    var showTitleDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Check if this item is still generating
    val isGenerating = historyItem.html == "GENERATING..."

    // Check if this item is new (never used - accessCount < 1)
    val isNewItem = (historyItem.accessCount ?: 0) < 1 && !isGenerating && historyItem.html != "GENERATING..."

    // Animated gradient for generating items
    val infiniteTransition = rememberInfiniteTransition(label = "generating")
    val animatedOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gradientOffset"
    )

    // Load screenshot if available
    val screenshotBitmap = remember(historyItem.id) {
        val screenshotFile = ScreenshotUtils.getScreenshotFile(context, historyItem.id)
        screenshotFile?.let { file ->
            BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap()
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (screenshotBitmap != null) 200.dp else 120.dp)
            .padding(horizontal = 8.dp, vertical = 2.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Background screenshot if available
            screenshotBitmap?.let { bitmap ->
                Image(
                    bitmap = bitmap,
                    contentDescription = "App screenshot",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )

                // Gradient overlay for text readability
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.7f)
                                ),
                                startY = 0f,
                                endY = Float.POSITIVE_INFINITY
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                )
            }

            // Fallback background if no screenshot
            if (screenshotBitmap == null) {
                if (isGenerating) {
                    // Video background for generating items
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
                                    start()
                                }

                                // Mute the video (no sound)
                                setOnPreparedListener { mediaPlayer ->
                                    mediaPlayer.isLooping = true
                                    mediaPlayer.setAudioAttributes(null)
                                    mediaPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING)
                                    start()
                                }

                                // Start immediately if possible
                                start()
                            }
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                if (historyItem.favorite == true)
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(12.dp)
                            )
                    )
                }
            }

            // Animated gradient overlay for generating items (even with screenshots)
            if (isGenerating) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF6366F1).copy(alpha = 0.1f),
                                    Color.Transparent,
                                    Color(0xFF8B5CF6).copy(alpha = 0.1f),
                                    Color.Transparent,
                                    Color(0xFFA855F7).copy(alpha = 0.1f)
                                ),
                                start = androidx.compose.ui.geometry.Offset(
                                    animatedOffset * 300f - 150f,
                                    0f
                                ),
                                end = androidx.compose.ui.geometry.Offset(
                                    animatedOffset * 300f + 150f,
                                    150f
                                )
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                )
            }

            // NEW badge for unused items
            if (isNewItem) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFEF4444)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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

            // Content overlay
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Title or Prompt section
                Column {
                    if (!historyItem.title.isNullOrEmpty()) {
                        Text(
                            text = historyItem.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = if (screenshotBitmap != null)
                                Color.White
                            else if (historyItem.favorite == true)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isGenerating) "Generating your app..." else historyItem.prompt,
                            fontSize = 14.sp,
                            maxLines = if (screenshotBitmap != null) 1 else 2,
                            overflow = TextOverflow.Ellipsis,
                            color = if (screenshotBitmap != null)
                                Color.White.copy(alpha = 0.9f)
                            else if (historyItem.favorite == true)
                                MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = if (isGenerating) "⚡ Generating your app..." else historyItem.prompt,
                            fontWeight = FontWeight.Medium,
                            maxLines = if (screenshotBitmap != null) 2 else 3,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 14.sp,
                            color = if (screenshotBitmap != null)
                                Color.White
                            else if (historyItem.favorite == true)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Show generating indicator
                    if (isGenerating) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = if (screenshotBitmap != null)
                                    Color.White
                                else
                                    MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "In progress...",
                                fontSize = 12.sp,
                                color = if (screenshotBitmap != null)
                                    Color.White.copy(alpha = 0.8f)
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Bottom row with date and actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
                            .format(Date(historyItem.timestamp)),
                        fontSize = 12.sp,
                        color = if (screenshotBitmap != null)
                            Color.White.copy(alpha = 0.8f)
                        else if (historyItem.favorite == true)
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row {
                        // Edit title button
                        IconButton(
                            onClick = {
                                showTitleDialog = true
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit title",
                                tint = if (screenshotBitmap != null)
                                    Color.White
                                else if (historyItem.favorite == true)
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Favorite button
                        IconButton(
                            onClick = { onToggleFavorite(historyItem.id) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                if (historyItem.favorite == true) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = if (historyItem.favorite == true) "Remove from favorites" else "Add to favorites",
                                tint = if (historyItem.favorite == true)
                                    if (screenshotBitmap != null) Color.Red.copy(alpha = 0.9f) else MaterialTheme.colorScheme.primary
                                else
                                    if (screenshotBitmap != null) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Delete button
                        IconButton(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete item",
                                tint = if (screenshotBitmap != null)
                                    Color.White
                                else if (historyItem.favorite == true)
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Title edit dialog
    if (showTitleDialog) {
        var titleText by remember { mutableStateOf(historyItem.title ?: "") }

        AlertDialog(
            onDismissRequest = { showTitleDialog = false },
            title = { Text("Edit Title") },
            text = {
                OutlinedTextField(
                    value = titleText,
                    onValueChange = { titleText = it },
                    label = { Text("Title") },
                    placeholder = { Text("Enter a title for this prompt...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onUpdateTitle(historyItem.id, titleText.trim())
                        showTitleDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showTitleDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Confirm Delete") },
            text = { Text("Are you sure you want to delete this item?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete(historyItem.id)
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
