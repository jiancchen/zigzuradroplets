package com.zigzura.droplets.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zigzura.droplets.Weblet
import com.zigzura.droplets.data.PromptHistory
import com.zigzura.droplets.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateToSignup: () -> Unit,
    onNavigateToDebug: () -> Unit
) {
    val viewModel: MainViewModel = hiltViewModel()
    val currentHtml by viewModel.currentHtml.collectAsState()
    val currentHistoryItem by viewModel.currentHistoryItem.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val promptRejection by viewModel.promptRejection.collectAsState()
    val promptHistory by viewModel.promptHistory.collectAsState(initial = emptyList())
    val keyboardController = LocalSoftwareKeyboardController.current

    var prompt by remember { mutableStateOf("") }
    var showSettingsMenu by remember { mutableStateOf(false) }
    var showFavoritesOnly by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Filter history based on favorites toggle
    val filteredHistory = remember(promptHistory, showFavoritesOnly) {
        if (showFavoritesOnly) {
            promptHistory.filter { it.favorite == true }
        } else {
            promptHistory
        }
    }

    // Show error snackbar
    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            snackbarHostState.showSnackbar(
                message = errorMessage,
                duration = SnackbarDuration.Long
            )
            viewModel.clearError()
        }
    }

    // Show prompt rejection snackbar
    promptRejection?.let { rejectionReason ->
        LaunchedEffect(rejectionReason) {
            snackbarHostState.showSnackbar(
                message = "Prompt rejected: $rejectionReason",
                duration = SnackbarDuration.Long,
                actionLabel = "OK"
            )
            viewModel.clearPromptRejection()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(320.dp)
            ) {
                // Drawer Header
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Prompt History",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "${promptHistory.size} prompts",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }

                // Drawer Content
                if (promptHistory.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "📝",
                                fontSize = 48.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No prompts yet",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Start by entering a prompt!",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    Column {
                        // Clear All Button
                        TextButton(
                            onClick = {
                                viewModel.clearHistory()
                                scope.launch { drawerState.close() }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text("Clear All History")
                        }

                        // Favorites Toggle
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = showFavoritesOnly,
                                onCheckedChange = { showFavoritesOnly = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = MaterialTheme.colorScheme.primary,
                                    uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Show favorites only",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }

                        HorizontalDivider()

                        // History List
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(filteredHistory) { historyItem ->
                                HistoryItem(
                                    historyItem = historyItem,
                                    onClick = {
                                        viewModel.loadHistoryItem(historyItem)
                                        scope.launch { drawerState.close() }
                                    },
                                    onToggleFavorite = { id ->
                                        viewModel.toggleFavorite(id)
                                    },
                                    onUpdateTitle = { id, title ->
                                        viewModel.updateTitle(id, title)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .imePadding(),
            topBar = {
                TopAppBar(
                    title = { Text("Droplets") },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    if (drawerState.isClosed) {
                                        drawerState.open()
                                    } else {
                                        drawerState.close()
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        Box {
                            IconButton(onClick = { showSettingsMenu = true }) {
                                Icon(Icons.Default.Settings, contentDescription = "Settings")
                            }
                            DropdownMenu(
                                expanded = showSettingsMenu,
                                onDismissRequest = { showSettingsMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("API Key Settings") },
                                    onClick = {
                                        showSettingsMenu = false
                                        onNavigateToSignup()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Debug HTML") },
                                    onClick = {
                                        showSettingsMenu = false
                                        onNavigateToDebug()
                                    }
                                )
                            }
                        }
                    }
                )
            },
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState)
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // WebView
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .weight(1f, fill = false)
                        .heightIn(min = 200.dp)
                ) {
                    if (currentHtml.isNotEmpty()) {
                        Weblet(
                            htmlContent = currentHtml,
                            appId = currentHistoryItem!!.id
                        )
                    } else {
                        // Placeholder when no HTML is loaded
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "💧",
                                    fontSize = 48.sp
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Enter a prompt below to generate HTML",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Loading overlay
                    if (isLoading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Card(
                                modifier = Modifier.padding(32.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    CircularProgressIndicator()
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("Generating HTML...")
                                }
                            }
                        }
                    }
                }

                // Prompt Input
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        OutlinedTextField(
                            value = prompt,
                            onValueChange = { prompt = it },
                            label = { Text("Enter your prompt") },
                            placeholder = { Text("Create a beautiful landing page for a coffee shop...") },
                            modifier = Modifier.weight(1f),
                            maxLines = 3,
                            enabled = !isLoading,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                cursorColor = MaterialTheme.colorScheme.primary
                            )
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        FloatingActionButton(
                            onClick = {
                                if (prompt.isNotBlank()) {
                                    viewModel.generateHtml(prompt)
                                    prompt = ""
                                    keyboardController?.hide()
                                }
                            },
                            modifier = Modifier.size(48.dp),
                            containerColor = MaterialTheme.colorScheme.primary
                        ) {
                            Icon(
                                Icons.Default.Send,
                                contentDescription = "Send",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }

                // Error display
                error?.let { errorMessage ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = errorMessage,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = viewModel::clearError) {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = "Clear error",
                                    tint = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

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
