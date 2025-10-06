package com.zigzura.droplets.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.zigzura.droplets.data.PromptHistory
import com.zigzura.droplets.utils.ScreenshotUtils
import java.text.SimpleDateFormat
import java.util.*
import android.graphics.BitmapFactory

@Composable
fun HistoryItem(
    historyItem: PromptHistory,
    onClick: () -> Unit,
    onToggleFavorite: (String) -> Unit = {},
    onUpdateTitle: (String, String) -> Unit = { _, _ -> }
) {
    var showTitleDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

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
                            text = historyItem.prompt,
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
                            text = historyItem.prompt,
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
}
