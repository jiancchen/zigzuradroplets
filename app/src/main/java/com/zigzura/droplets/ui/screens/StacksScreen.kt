package com.zigzura.droplets.ui.screens

import android.graphics.BitmapFactory
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zigzura.droplets.data.PromptHistory
import com.zigzura.droplets.utils.ScreenshotUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

@Composable
fun StacksScreen(
    promptHistory: List<PromptHistory> = emptyList(),
    onNavigateToApp: (String) -> Unit = {},
    onNavigateToMain: () -> Unit = {},
    onNavigateToCreate: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    // Use actual prompt history or sample data if empty
    val historyItems = if (promptHistory.isNotEmpty()) {
        promptHistory
    } else {
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
fun ThreeDCard(
    title: String,
    rotationX: Float,
    rotationY: Float,
    backgroundColor: Color = Color.Black,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .graphicsLayer {
                this.rotationX = rotationX
                this.rotationY = rotationY
                cameraDistance = 12 * density
            }
            .background(backgroundColor, RoundedCornerShape(16.dp))
            .border(1.dp, Color.DarkGray, RoundedCornerShape(16.dp))
            .padding(24.dp)
    ) {
        Text(
            title,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
    }
}

@Composable
fun Scrollable3DStack(
    items: List<PromptHistory>,
    onNavigateToApp: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

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
                val distanceFromCenter = kotlin.math.abs(itemCenterY - screenCenterY) / screenCenterY
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

@Composable
fun ThreeDImageCard(
    historyItem: PromptHistory,
    rotationX: Float,
    rotationY: Float,
    backgroundColor: Color = Color.Black,
    modifier: Modifier = Modifier,
    onNavigateToApp: (String) -> Unit = {}
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
        ?: historyItem.prompt.take(50) + "..."

    Box(
        modifier = modifier
            .graphicsLayer {
                this.rotationX = rotationX
                this.rotationY = rotationY
                cameraDistance = 12 * density
            }
            .clip(RoundedCornerShape(16.dp))
    ) {
        // Background image or color
        when {
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
                .clickable { onNavigateToApp(historyItem.id) }
        )

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

// Data class to hold pre-calculated transform values
private data class CardTransform(
    val scale: Float = 1f,
    val depth: Float = 0f,
    val alpha: Float = 1f,
    val isNearCenter: Boolean = true
)

// Global image cache to store thumbnails across recompositions
private object ImageCache {
    private val cache = ConcurrentHashMap<String, androidx.compose.ui.graphics.ImageBitmap>()

    fun get(key: String) = cache[key]
    fun put(key: String, bitmap: androidx.compose.ui.graphics.ImageBitmap) {
        // Limit cache size to prevent memory issues
        if (cache.size >= 50) {
            cache.clear() // Simple cache eviction
        }
        cache[key] = bitmap
    }
}

// Optimized image loading utility
object OptimizedImageLoader {
    suspend fun loadThumbnail(
        context: android.content.Context,
        appId: String,
        maxWidth: Int = 300,
        maxHeight: Int = 200
    ): androidx.compose.ui.graphics.ImageBitmap? = withContext(Dispatchers.IO) {
        val cacheKey = "${appId}_${maxWidth}_${maxHeight}"

        // Check cache first
        ImageCache.get(cacheKey)?.let { return@withContext it }

        // Load and resize image
        val screenshotFile = ScreenshotUtils.getScreenshotFile(context, appId)
        screenshotFile?.let { file ->
            try {
                // First, get image dimensions without loading full image
                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                BitmapFactory.decodeFile(file.absolutePath, options)

                // Calculate sample size for downscaling
                val sampleSize = calculateInSampleSize(options, maxWidth, maxHeight)

                // Load the scaled-down image
                val loadOptions = BitmapFactory.Options().apply {
                    inSampleSize = sampleSize
                    inJustDecodeBounds = false
                    inPreferredConfig = android.graphics.Bitmap.Config.RGB_565 // Use less memory
                }

                val bitmap = BitmapFactory.decodeFile(file.absolutePath, loadOptions)
                bitmap?.let {
                    val imageBitmap = it.asImageBitmap()
                    ImageCache.put(cacheKey, imageBitmap)
                    imageBitmap
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
}

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
                    StacksFloatingMenuItem(
                        icon = Icons.Default.Home,
                        label = "My Apps",
                        onClick = {
                            onNavigateToMain()
                            isExpanded = false
                        }
                    )
                    StacksFloatingMenuItem(
                        icon = Icons.Default.Add,
                        label = "Create",
                        onClick = {
                            onNavigateToCreate()
                            isExpanded = false
                        }
                    )
                    StacksFloatingMenuItem(
                        icon = Icons.Default.Settings,
                        label = "Settings",
                        onClick = {
                            onNavigateToSettings()
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
fun StacksFloatingMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            shadowElevation = 4.dp
        ) {
            Text(
                text = label,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Normal
            )
        }

        SmallFloatingActionButton(
            onClick = onClick,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
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

@Composable
fun SearchBarWithFavorites(
    searchText: TextFieldValue,
    onSearchTextChange: (TextFieldValue) -> Unit,
    showFavorites: Boolean,
    onToggleFavorites: () -> Unit,
    onClearSearch: () -> Unit,
    favoriteItems: List<PromptHistory>,
    onNavigateToApp: (String) -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    // Calculate most used apps (you can implement your own logic here)
    val mostUsedItems = remember(favoriteItems) {
        // For now, using sample logic - you can replace with actual usage tracking
        favoriteItems.shuffled().take(5) // Placeholder for most used logic
    }

    Column(modifier = modifier) {
        // Semi-transparent white pill search bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            color = Color.White.copy(alpha = 0.9f),
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Logo/Star icon (clickable to show favorites)
                IconButton(
                    onClick = onToggleFavorites,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Show favorites",
                        tint = if (showFavorites) Color(0xFFFFB74D) else Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Search text field
                OutlinedTextField(
                    value = searchText,
                    onValueChange = onSearchTextChange,
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester),
                    placeholder = {
                        Text(
                            text = "Search your apps...",
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchText.text.isNotBlank()) {
                            IconButton(onClick = onClearSearch) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear search",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = Color(0xFFFF8A65)
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Search
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            keyboardController?.hide()
                        }
                    )
                )
            }
        }

        // White box with favorites and most used (shown when favorites are active and no typing)
        AnimatedVisibility(
            visible = showFavorites && searchText.text.isBlank() && (favoriteItems.isNotEmpty() || mostUsedItems.isNotEmpty()),
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color.White.copy(alpha = 0.95f),
                shadowElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Favorite Apps Section
                    if (favoriteItems.isNotEmpty()) {
                        Text(
                            text = "Favorite Apps",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black.copy(alpha = 0.8f),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            items(favoriteItems.size) { index ->
                                FavoriteAppCard(
                                    historyItem = favoriteItems[index],
                                    onNavigateToApp = onNavigateToApp
                                )
                            }
                        }
                    }

                    // Most Used Apps Section
                    if (mostUsedItems.isNotEmpty()) {
                        if (favoriteItems.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(20.dp))
                        }

                        Text(
                            text = "Most Used Apps",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black.copy(alpha = 0.8f),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            items(mostUsedItems.size) { index ->
                                MostUsedAppCard(
                                    historyItem = mostUsedItems[index],
                                    onNavigateToApp = onNavigateToApp
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
fun FavoriteAppCard(
    historyItem: PromptHistory,
    onNavigateToApp: (String) -> Unit
) {
    Surface(
        modifier = Modifier
            .size(width = 120.dp, height = 80.dp)
            .clickable { onNavigateToApp(historyItem.id) },
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.8f),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Favorite app",
                tint = Color(0xFFFFB74D),
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = historyItem.title?.take(15) ?: historyItem.prompt.take(15),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black.copy(alpha = 0.8f),
                maxLines = 2,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun MostUsedAppCard(
    historyItem: PromptHistory,
    onNavigateToApp: (String) -> Unit
) {
    Surface(
        modifier = Modifier
            .size(width = 120.dp, height = 80.dp)
            .clickable { onNavigateToApp(historyItem.id) },
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.8f),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Placeholder for most used icon (can be a different icon or same as favorite)
            Icon(
                imageVector = Icons.Default.Star, // Change this icon if needed
                contentDescription = "Most used app",
                tint = Color(0xFFFFB74D),
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = historyItem.title?.take(15) ?: historyItem.prompt.take(15),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black.copy(alpha = 0.8f),
                maxLines = 2,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
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
