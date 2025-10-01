package com.zigzura.droplets.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zigzura.droplets.data.PromptHistory
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryItem(
    historyItem: PromptHistory,
    onClick: () -> Unit,
    onToggleFavorite: (String) -> Unit = {},
    onUpdateTitle: (String, String) -> Unit = { _, _ -> }
) {
    var showTitleDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (historyItem.favorite == true)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Title or Prompt
            if (!historyItem.title.isNullOrEmpty()) {
                Text(
                    text = historyItem.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = if (historyItem.favorite == true)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = historyItem.prompt,
                    fontSize = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = if (historyItem.favorite == true)
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = historyItem.prompt,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 14.sp,
                    color = if (historyItem.favorite == true)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

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
                    color = if (historyItem.favorite == true)
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
                            tint = if (historyItem.favorite == true)
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
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
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
