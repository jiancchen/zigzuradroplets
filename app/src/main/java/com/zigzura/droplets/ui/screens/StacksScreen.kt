package com.zigzura.droplets.ui.screens

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zigzura.droplets.data.PromptHistory
import com.zigzura.droplets.utils.ScreenshotUtils
import kotlinx.coroutines.delay

@Composable
fun StacksScreen(
    promptHistory: List<PromptHistory> = emptyList()
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
                title = "Sample App 1"
            ),
            PromptHistory(
                id = "sample2",
                prompt = "Apps will appear here",
                html = "",
                title = "Sample App 2"
            ),
            PromptHistory(
                id = "sample3",
                prompt = "Start generating content",
                html = "",
                title = "Sample App 3"
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFD84E))
    ) {
        Scrollable3DStack(
            items = historyItems,
            modifier = Modifier.fillMaxSize()
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
                    ThreeDImageCard(
                        historyItem = item,
                        rotationX = rotationX,
                        rotationY = rotationY,
                        backgroundColor = backgroundColors[index],
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                this.translationY = translationY + 8f
                                this.translationX = 6f
                                this.alpha = transform.alpha * 0.8f
                            }
                    )
                }

                // Main card with screenshot/image
                ThreeDImageCard(
                    historyItem = item,
                    rotationX = rotationX,
                    rotationY = rotationY,
                    backgroundColor = Color.Black,
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
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Lazy load screenshot with remember to avoid reloading on recomposition
    val screenshotBitmap = remember(historyItem.id) {
        val screenshotFile = ScreenshotUtils.getScreenshotFile(context, historyItem.id)
        screenshotFile?.let { file ->
            BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap()
        }
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
        if (screenshotBitmap != null) {
            Image(
                bitmap = screenshotBitmap,
                contentDescription = "App screenshot",
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = 0.7f }, // 70% opacity for image
                contentScale = ContentScale.Crop
            )
        } else {
            // Fallback to solid background color
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor)
            )
        }

        // Black overlay for text readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
        )

        // Text content
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
                fontSize = 18.sp
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
