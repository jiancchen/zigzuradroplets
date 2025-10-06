package com.zigzura.droplets.ui.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zigzura.droplets.data.PromptHistory

@Composable
fun StacksScreen(
    promptHistory: List<PromptHistory> = emptyList()
) {
    // Convert prompt history to card titles, or use sample data if empty
    val cardTitles = if (promptHistory.isNotEmpty()) {
        promptHistory.map { history ->
            history.title?.takeIf { it.isNotBlank() } ?: history.prompt.take(50) + "..."
        }
    } else {
        listOf(
            "Create your first app",
            "Apps will appear here",
            "Start generating content"
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFD84E))
    ) {
        Scrollable3DStack(
            items = cardTitles,
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
    items: List<String>,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    // Generate random vibrant colors for background cards
    val backgroundColors = remember(items.size) {
        items.mapIndexed { index, _ ->
            val colors = listOf(
                Color(0xFFE91E63), // Pink
                Color(0xFF9C27B0), // Purple
                Color(0xFF673AB7), // Deep Purple
                Color(0xFF3F51B5), // Indigo
                Color(0xFF2196F3), // Blue
                Color(0xFF00BCD4), // Cyan
                Color(0xFF009688), // Teal
                Color(0xFF4CAF50), // Green
                Color(0xFF8BC34A), // Light Green
                Color(0xFFFF9800), // Orange
                Color(0xFFFF5722), // Deep Orange
                Color(0xFFF44336)  // Red
            )
            colors[index % colors.size]
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(vertical = 100.dp)
    ) {
        itemsIndexed(items) { index, item ->
            // Calculate the item's position relative to the LazyColumn viewport
            val layoutInfo = listState.layoutInfo
            val visibleItem = layoutInfo.visibleItemsInfo.find { it.index == index }

            val itemCenterY = visibleItem?.let { itemInfo ->
                val itemTop = itemInfo.offset
                val itemHeight = itemInfo.size
                itemTop + itemHeight / 2f
            } ?: 0f

            // Screen center relative to LazyColumn viewport
            val screenCenterY = layoutInfo.viewportSize.height / 2f

            // Distance from item center to screen center (normalized)
            val distanceFromCenter = kotlin.math.abs(itemCenterY - screenCenterY) / screenCenterY
            val normalizedDistance = distanceFromCenter.coerceIn(0f, 2f)

            // Smooth scaling based on distance from center (closer = larger)
            val smoothScale = 1f - (normalizedDistance * 0.3f).coerceIn(0f, 0.3f)

            // 3D effects based on position
            val depth = normalizedDistance.coerceIn(0f, 4f)
            val rotationX = 8f + (depth * 6f)
            val rotationY = -10f
            val translationY = depth * 40f
            val alpha = (1f - (depth * 0.2f)).coerceIn(0.2f, 1f)

            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size((250 * smoothScale).dp, (160 * smoothScale).dp)
            ) {
                // Background card (colorful offset)
                ThreeDCard(
                    title = "",
                    rotationX = rotationX,
                    rotationY = rotationY,
                    backgroundColor = backgroundColors[index],
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            this.translationY = translationY + 8f
                            this.translationX = 6f
                            this.alpha = alpha * 0.8f
                        }
                )

                // Main card (black with text)
                ThreeDCard(
                    title = item,
                    rotationX = rotationX,
                    rotationY = rotationY,
                    backgroundColor = Color.Black,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            this.translationY = translationY
                            this.alpha = alpha
                        }
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewScrollable3DStack() {
    val cards = listOf(
        "Ocean Waves – Coastal",
        "Mountain Wind – Alpine",
        "City Nights – Urban",
        "Rainforest – Tropical",
        "Desert Heat – Mirage"
    )
    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xFFFFD84E))
    ) {
        Scrollable3DStack(cards)
    }
}
