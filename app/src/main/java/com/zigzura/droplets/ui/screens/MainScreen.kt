package com.zigzura.droplets.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
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
    onNavigateToDebug: () -> Unit,
    onNavigateToAppView: (String) -> Unit = {}
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

    // Navigate to newly created app
    LaunchedEffect(currentHistoryItem) {
        currentHistoryItem?.let { item ->
            // If we have a current item and we're on the create tab, navigate to it
            if (selectedTab == 1 && currentHtml.isNotEmpty()) {
                onNavigateToAppView(item.id)
                selectedTab = 0 // Switch back to My Apps tab
            }
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
                        onNavigateToAppView(historyItem.id)
                    },
                    onToggleFavorite = { id ->
                        viewModel.toggleFavorite(id)
                    },
                    onUpdateTitle = { id, title ->
                        viewModel.updateTitle(id, title)
                    },
                    onClearHistory = {
                        viewModel.clearHistory()
                    }
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
                    onAppCreated = { appId ->
                        onNavigateToAppView(appId)
                    },
                    isLoading = isLoading
                )

                2 -> SettingsScreen(
                    onNavigateToSignup = onNavigateToSignup,
                    onNavigateToDebug = onNavigateToDebug,
                    promptHistory = promptHistory
                )
            }

            // Add the floating navigation toolbar
            FloatingM3Toolbar(
                selectedTab = selectedTab,
                onMyAppsClick = { selectedTab = 0 },
                onCreateClick = { selectedTab = 1 },
                onSettingsClick = { selectedTab = 2 }
            )
        }
    }
}

@Composable
fun FloatingM3Toolbar(
    selectedTab: Int,
    modifier: Modifier = Modifier,
    onMyAppsClick: () -> Unit = {},
    onCreateClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    var isExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        // Backdrop for dismissing menu
        if (isExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        isExpanded = false
                    }
            )
        }

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Menu Items (visible when expanded)
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FloatingMenuItem(
                        icon = Icons.Default.Home,
                        label = "My Apps",
                        isSelected = selectedTab == 0,
                        onClick = {
                            onMyAppsClick()
                            isExpanded = false
                        }
                    )
                    FloatingMenuItem(
                        icon = Icons.Default.Add,
                        label = "Create",
                        isSelected = selectedTab == 1,
                        onClick = {
                            onCreateClick()
                            isExpanded = false
                        }
                    )
                    FloatingMenuItem(
                        icon = Icons.Default.Settings,
                        label = "Settings",
                        isSelected = selectedTab == 2,
                        onClick = {
                            onSettingsClick()
                            isExpanded = false
                        }
                    )
                }
            }

            // Main FAB
            FloatingActionButton(
                onClick = { isExpanded = !isExpanded },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 8.dp
                )
            ) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.Close else Icons.Default.Menu,
                    contentDescription = if (isExpanded) "Close menu" else "Open menu",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun FloatingMenuItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            shadowElevation = 4.dp
        ) {
            Text(
                text = label,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelLarge,
                color = if (isSelected)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onSurface,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }

        SmallFloatingActionButton(
            onClick = onClick,
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.secondaryContainer,
            contentColor = if (isSelected)
                MaterialTheme.colorScheme.onPrimary
            else
                MaterialTheme.colorScheme.onSecondaryContainer,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = if (isSelected) 6.dp else 4.dp,
                pressedElevation = 8.dp
            )
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
