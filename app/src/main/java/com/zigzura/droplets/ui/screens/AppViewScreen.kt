package com.zigzura.droplets.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.zigzura.droplets.Weblet
import com.zigzura.droplets.data.PromptHistory
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppViewScreen(
    historyItem: PromptHistory,
    htmlContent: String,
    onNavigateBack: () -> Unit,
    onToggleFavorite: (String) -> Unit,
    onUpdateTitle: (String, String) -> Unit
) {
    var showTitleDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = historyItem.title?.takeIf { it.isNotBlank() }
                                ?: "Generated App",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                                .format(Date(historyItem.timestamp)),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Edit title button
                    IconButton(onClick = { showTitleDialog = true }) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit title"
                        )
                    }

                    // Favorite button
                    IconButton(onClick = { onToggleFavorite(historyItem.id) }) {
                        Icon(
                            if (historyItem.favorite == true)
                                Icons.Default.Favorite
                            else
                                Icons.Default.FavoriteBorder,
                            contentDescription = if (historyItem.favorite == true)
                                "Remove from favorites"
                            else
                                "Add to favorites",
                            tint = if (historyItem.favorite == true)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Show prompt info
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Original Prompt",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = historyItem.prompt,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // App content
            if (htmlContent.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Weblet(
                        htmlContent = htmlContent,
                        appId = historyItem.id
                    )
                }
            } else {
                // Loading or error state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Loading app...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                    placeholder = { Text("Enter a title for this app...") },
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
