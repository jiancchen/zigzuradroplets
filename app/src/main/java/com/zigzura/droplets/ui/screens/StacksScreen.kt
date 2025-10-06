package com.zigzura.droplets.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zigzura.droplets.R
import com.zigzura.droplets.data.PromptHistory
import com.zigzura.droplets.ui.components.SearchBarWithFavorites
import com.zigzura.droplets.ui.components.ThreeDImageCard
import com.zigzura.droplets.utils.rememberScrollStateWithPreservation
import kotlinx.coroutines.delay

@Composable
fun StacksScreen(
    promptHistory: List<PromptHistory> = emptyList(),
    onNavigateToApp: (String) -> Unit = {},
    onNavigateToMain: () -> Unit = {},
    onNavigateToCreate: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    isGenerating: Boolean = false, // Add generation state
    currentGeneratingItemId: String? = null, // Add current generating item ID
    onShowSnackbar: (String) -> Unit = {} // Add snackbar callback
) {
    // Use actual prompt history or sample data if empty
    val historyItems = promptHistory.ifEmpty {
        listOf(
            PromptHistory(
                id = "sample1",
                prompt = "Create your first app",
                html = "",
                title = "Sample App 1",
                favorite = true
            ),
            PromptHistory(
                id = "sample2",
                prompt = "Apps will appear here",
                html = "",
                title = "Sample App 2",
                favorite = false
            ),
            PromptHistory(
                id = "sample3",
                prompt = "Start generating content",
                html = "",
                title = "Sample App 3",
                favorite = true
            )
        )
    }

    var searchText by remember { mutableStateOf(TextFieldValue("")) }
    var showFavorites by remember { mutableStateOf(false) }
    var isSearchActive by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    // Filter items only when search is active, not when showing favorites
    val filteredItems = remember(historyItems, searchText, isSearchActive) {
        when {
            isSearchActive && searchText.text.isNotBlank() -> {
                historyItems.filter { item ->
                    item.title?.contains(searchText.text, ignoreCase = true) == true ||
                            item.prompt.contains(searchText.text, ignoreCase = true)
                }
            }

            else -> historyItems // Always show all items when not searching
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFD84E))
    ) {
        // Main 3D Stack - fills entire screen
        Scrollable3DStack(
            items = filteredItems,
            onNavigateToApp = onNavigateToApp,
            isGenerating = isGenerating,
            currentGeneratingItemId = currentGeneratingItemId,
            onShowSnackbar = onShowSnackbar,
            modifier = Modifier.fillMaxSize()
        )

        // Search bar at the top with proper system padding
        SearchBarWithFavorites(
            searchText = searchText,
            onSearchTextChange = {
                searchText = it
                isSearchActive = it.text.isNotBlank()
                if (!isSearchActive) {
                    showFavorites = false
                }
            },
            showFavorites = showFavorites,
            onToggleFavorites = {
                showFavorites = !showFavorites
                if (showFavorites) {
                    isSearchActive = false
                    searchText = TextFieldValue("")
                    keyboardController?.hide()
                }
            },
            onClearSearch = {
                searchText = TextFieldValue("")
                isSearchActive = false
                showFavorites = false
                keyboardController?.hide()
            },
            favoriteItems = historyItems.filter { it.favorite == true },
            onNavigateToApp = onNavigateToApp,
            focusRequester = focusRequester,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding() // Add system status bar padding
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // Add floating navigation toolbar
        StacksFloatingToolbar(
            onNavigateToMain = onNavigateToMain,
            onNavigateToCreate = onNavigateToCreate,
            onNavigateToSettings = onNavigateToSettings,
            modifier = Modifier.align(Alignment.BottomEnd)
        )
    }
}

@Composable
fun Scrollable3DStack(
    items: List<PromptHistory>,
    onNavigateToApp: (String) -> Unit = {},
    isGenerating: Boolean = false, // Add generation state
    currentGeneratingItemId: String? = null, // Add current generating item ID
    onShowSnackbar: (String) -> Unit = {}, // Add snackbar callback
    modifier: Modifier = Modifier
) {
    val listState = rememberScrollStateWithPreservation()

    // Stable color array - only recreated when items change
    val backgroundColors = remember(items) {
        items.mapIndexed { index, _ ->
            val colors = listOf(
                Color(0xFFE91E63), Color(0xFF9C27B0), Color(0xFF673AB7), Color(0xFF3F51B5),
                Color(0xFF2196F3), Color(0xFF00BCD4), Color(0xFF009688), Color(0xFF4CAF50),
                Color(0xFF8BC34A), Color(0xFFFF9800), Color(0xFFFF5722), Color(0xFFF44336)
            )
            colors[index % colors.size]
        }
    }

    // Debounced scroll state for expensive calculations
    var debouncedScrolling by remember { mutableStateOf(false) }
    var lastScrollTime by remember { mutableLongStateOf(0L) }

    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress) {
            debouncedScrolling = true
            lastScrollTime = System.currentTimeMillis()
        } else {
            // Wait a bit after scrolling stops before enabling full calculations
            delay(50)
            if (System.currentTimeMillis() - lastScrollTime >= 50) {
                debouncedScrolling = false
            }
        }
    }

    // Optimized layout calculations using derivedStateOf
    val layoutCalculations by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val screenCenterY = layoutInfo.viewportSize.height / 2f
            val visibleItems = layoutInfo.visibleItemsInfo

            // Pre-calculate positions for all visible items
            visibleItems.associate { itemInfo ->
                val itemCenterY = itemInfo.offset + itemInfo.size / 2f
                val distanceFromCenter =
                    kotlin.math.abs(itemCenterY - screenCenterY) / screenCenterY
                val normalizedDistance = distanceFromCenter.coerceIn(0f, 2f)

                itemInfo.index to CardTransform(
                    scale = 1f - (normalizedDistance * 0.3f).coerceIn(0f, 0.3f),
                    depth = normalizedDistance.coerceIn(0f, 4f),
                    alpha = (1f - (normalizedDistance * 0.2f)).coerceIn(0.2f, 1f),
                    isNearCenter = normalizedDistance < 1f
                )
            }
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(vertical = 100.dp)
    ) {
        itemsIndexed(
            items = items,
            key = { index, item -> "$index-${item.hashCode()}" } // Stable keys for better recycling
        ) { index, item ->
            val transform = layoutCalculations[index] ?: CardTransform()

            // Reduce complexity for items far from center during fast scrolling
            val useSimplifiedGraphics = debouncedScrolling && !transform.isNearCenter

            val rotationX = if (useSimplifiedGraphics) 15f else 8f + (transform.depth * 6f)
            val rotationY = -10f
            val translationY = if (useSimplifiedGraphics) 0f else transform.depth * 40f

            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size((300 * transform.scale).dp, (160 * transform.scale).dp)
            ) {
                // Background card (colorful offset) - skip for distant items during fast scroll
                if (!useSimplifiedGraphics) {
                    // Simple colored background card without image
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                this.rotationX = rotationX
                                this.rotationY = rotationY
                                cameraDistance = 12 * density
                                this.translationY = translationY + 8f
                                this.translationX = 6f
                                this.alpha = transform.alpha * 0.8f
                            }
                            .clip(RoundedCornerShape(16.dp))
                            .background(backgroundColors[index])
                    )
                }

                // Main card with screenshot/image
                ThreeDImageCard(
                    historyItem = item,
                    rotationX = rotationX,
                    rotationY = rotationY,
                    backgroundColor = Color.Black,
                    onNavigateToApp = onNavigateToApp,
                    isCurrentlyGenerating = isGenerating && currentGeneratingItemId == item.id,
                    onShowSnackbar = onShowSnackbar,
                    modifier = Modifier
                        .fillMaxSize()
                        .then(
                            if (useSimplifiedGraphics) {
                                // Simplified graphics for far items during fast scroll
                                Modifier.graphicsLayer { alpha = transform.alpha }
                            } else {
                                // Full 3D effects for near items or when not scrolling
                                Modifier.graphicsLayer {
                                    this.translationY = translationY
                                    this.alpha = transform.alpha
                                }
                            }
                        )
                )
            }
        }
    }
}

// Data class to hold pre-calculated transform values
private data class CardTransform(
    val scale: Float = 1f,
    val depth: Float = 0f,
    val alpha: Float = 1f,
    val isNearCenter: Boolean = true
)


@Composable
fun StacksFloatingToolbar(
    onNavigateToMain: () -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        // Remove the backdrop - no more black overlay covering content

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
                    StacksFloatingMenuItem(
                        icon = Icons.Default.Home,
                        label = "My Apps",
                        backgroundColor = Color(0xFF2196F3), // Vibrant blue
                        onClick = {
                            onNavigateToMain()
                            isExpanded = false
                        }
                    )
                    StacksFloatingMenuItem(
                        icon = Icons.Default.Add,
                        label = "Create",
                        backgroundColor = Color(0xFF4CAF50), // Vibrant green
                        onClick = {
                            onNavigateToCreate()
                            isExpanded = false
                        }
                    )
                    StacksFloatingMenuItem(
                        icon = Icons.Default.Settings,
                        label = "Settings",
                        backgroundColor = Color(0xFF9C27B0), // Vibrant purple
                        onClick = {
                            onNavigateToSettings()
                            isExpanded = false
                        }
                    )
                }
            }

            // Main FAB with custom colors to match your yellow theme
            FloatingActionButton(
                onClick = { isExpanded = !isExpanded },
                containerColor = Color(0xFFFFB74D), // Golden yellow to match your theme
                contentColor = Color.White, // White icon for contrast
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 12.dp
                )
            ) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.Close else Icons.Default.Menu,
                    contentDescription = if (isExpanded) stringResource(R.string.close_menu) else stringResource(R.string.open_menu),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun StacksFloatingMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    backgroundColor: Color = Color(0xFFE91E63) // Default vibrant color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = backgroundColor, // Use vibrant background color
            tonalElevation = 2.dp,
            shadowElevation = 4.dp,
            modifier = Modifier.width(100.dp) // Fixed width to prevent jarring text differences
        ) {
            Text(
                text = label,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelLarge,
                color = Color.White, // White text for contrast on vibrant background
                fontWeight = FontWeight.Medium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center // Center align for consistency
            )
        }

        SmallFloatingActionButton(
            onClick = onClick,
            containerColor = backgroundColor, // Match the text background color
            contentColor = Color.White, // White icon for contrast
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 4.dp,
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


@Preview
@Composable
fun PreviewScrollable3DStack() {
    val cards = listOf(
        PromptHistory(
            id = "preview1",
            prompt = "Ocean Waves – Coastal",
            html = "",
            title = "Ocean Waves"
        ),
        PromptHistory(
            id = "preview2",
            prompt = "Mountain Wind – Alpine",
            html = "",
            title = "Mountain Wind"
        ),
        PromptHistory(
            id = "preview3",
            prompt = "City Nights – Urban",
            html = "",
            title = "City Nights"
        ),
        PromptHistory(
            id = "preview4",
            prompt = "Rainforest – Tropical",
            html = "",
            title = "Rainforest"
        ),
        PromptHistory(
            id = "preview5",
            prompt = "Desert Heat – Mirage",
            html = "",
            title = "Desert Heat"
        )
    )
    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xFFFFD84E))
    ) {
        Scrollable3DStack(cards)
    }
}
