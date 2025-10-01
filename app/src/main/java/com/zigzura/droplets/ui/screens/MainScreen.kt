package com.zigzura.droplets.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zigzura.droplets.viewmodel.MainViewModel

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

    var selectedTab by remember { mutableIntStateOf(0) }
    var prompt by remember { mutableStateOf("") }
    var showFavoritesOnly by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = when (selectedTab) {
                                0 -> "My Apps"
                                1 -> "Create"
                                2 -> "Settings"
                                else -> "Droplets"
                            },
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = when (selectedTab) {
                                0 -> "${promptHistory.size} apps created"
                                1 -> "Generate a new app"
                                2 -> "Manage your preferences"
                                else -> ""
                            },
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("My Apps") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    label = { Text("Create") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = { Text("Settings") },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 }
                )
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        modifier = Modifier.background(
            Brush.linearGradient(
                colors = listOf(
                    Color(0xFFF8FAFC),
                    Color(0xFFF1F5F9),
                    Color(0xFFE2E8F0)
                )
            )
        )
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            when (selectedTab) {
                0 -> MyAppsScreen(
                    promptHistory = filteredHistory,
                    showFavoritesOnly = showFavoritesOnly,
                    onShowFavoritesToggle = { showFavoritesOnly = it },
                    onHistoryItemClick = { historyItem ->
                        viewModel.loadHistoryItem(historyItem)
                    },
                    onToggleFavorite = { id ->
                        viewModel.toggleFavorite(id)
                    },
                    onUpdateTitle = { id, title ->
                        viewModel.updateTitle(id, title)
                    },
                    onClearHistory = {
                        viewModel.clearHistory()
                    },
                    currentHistoryItem = currentHistoryItem,
                    currentHtml = currentHtml,
                    isLoading = isLoading
                )

                1 -> CreateScreen(
                    prompt = prompt,
                    onPromptChange = { prompt = it },
                    onSubmit = {
                        if (prompt.isNotBlank()) {
                            viewModel.generateHtml(prompt)
                            prompt = ""
                            keyboardController?.hide()
                        }
                    },
                    isLoading = isLoading
                )

                2 -> SettingsScreen(
                    onNavigateToSignup = onNavigateToSignup,
                    onNavigateToDebug = onNavigateToDebug,
                    promptHistory = promptHistory
                )
            }
        }
    }
}
