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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zigzura.droplets.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateToSignup: () -> Unit,
    onNavigateToDebug: () -> Unit,
    onNavigateToStacks: () -> Unit = {}
) {
    val viewModel: MainViewModel = hiltViewModel()
    val currentHtml by viewModel.currentHtml.collectAsState()
    val currentHistoryItem by viewModel.currentHistoryItem.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val generationProgress by viewModel.generationProgress.collectAsState()
    val error by viewModel.error.collectAsState()
    val promptRejection by viewModel.promptRejection.collectAsState()
    val promptHistory by viewModel.promptHistory.collectAsState(initial = emptyList())
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

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

    // Show success message when app is created
    LaunchedEffect(currentHistoryItem) {
        currentHistoryItem?.let { item ->
            // If we have a current item and we're on the create tab, show success
            if (selectedTab == 1 && currentHtml.isNotEmpty()) {
                snackbarHostState.showSnackbar(
                    message = "App '${item.title ?: "Untitled"}' created successfully!",
                    duration = SnackbarDuration.Short
                )
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
                0 -> {
                    // Global generation progress bar for My Apps screen
                    if (isGenerating) {
                        AnimatedGenerationProgress(
                            progressText = generationProgress,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        )
                    }

                    MyAppsScreen(
                        promptHistory = filteredHistory,
                        showFavoritesOnly = showFavoritesOnly,
                        onShowFavoritesToggle = { showFavoritesOnly = it },
                        onHistoryItemClick = { historyItem ->
                            // Since AppViewScreen is removed, you might want to handle this differently
                            // For now, we'll just show a message or handle as needed
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "App '${historyItem.title ?: "Untitled"}' selected"
                                )
                            }
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
                        onDeleteItem = { id ->
                            viewModel.deletePromptHistory(context, id)
                        }
                    )
                }

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
                        // Since AppViewScreen is removed, just show success message
                        // The LaunchedEffect above will handle showing the success message
                    },
                    isLoading = isLoading,
                    isGenerating = isGenerating,
                    generationProgress = generationProgress
                )

                2 -> SettingsScreen(
                    onNavigateToSignup = onNavigateToSignup,
                    onNavigateToDebug = onNavigateToDebug,
                    onNavigateToStacks = onNavigateToStacks,
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

@Composable
fun AnimatedGenerationProgress(
    progressText: String,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "progress")

    // Animated progress bar that moves left to right
    val progressOffset by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progressOffset"
    )

    // Pulsing alpha for the text
    val textAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "textAlpha"
    )

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF6366F1).copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "⚡ Generating your app...",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6366F1)
            )

            Text(
                text = progressText,
                fontSize = 14.sp,
                color = Color(0xFF475569).copy(alpha = textAlpha),
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            // Animated progress bar container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFFE2E8F0))
            ) {
                // Moving progress indicator
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.3f)
                        .offset(x = (progressOffset * 200).dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF6366F1).copy(alpha = 0.3f),
                                    Color(0xFF6366F1),
                                    Color(0xFF6366F1).copy(alpha = 0.3f)
                                )
                            )
                        )
                )
            }

            Text(
                text = "⚠️ Don't leave the app until generation is complete",
                fontSize = 12.sp,
                color = Color(0xFF64748B),
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
        }
    }
}
