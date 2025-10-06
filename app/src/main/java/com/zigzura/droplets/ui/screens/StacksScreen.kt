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
    elevation: Dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .graphicsLayer {
                this.rotationX = rotationX
                this.rotationY = rotationY
                cameraDistance = 12 * density
                shadowElevation = elevation.toPx()
            }
            .background(Color.Black, RoundedCornerShape(16.dp))
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

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(vertical = 100.dp)
    ) {
        itemsIndexed(items) { index, item ->
            val firstVisible = listState.firstVisibleItemIndex
            val visibleOffset = listState.firstVisibleItemScrollOffset / 1000f

            // compute a relative depth (0 = top, 1+ = below)
            val depth = (index - firstVisible) + visibleOffset
            val clamped = depth.coerceIn(0f, 4f)  // only animate first few visible

            val rotationX = 8f + (clamped * 6f)
            val rotationY = -10f
            val translationY = clamped * 50f
            val scale = 1f - (clamped * 0.08f)
            val alpha = 1f - (clamped * 0.25f)

            ThreeDCard(
                title = item,
                rotationX = rotationX,
                rotationY = rotationY,
                elevation = (12 - clamped * 2).dp,
                modifier = Modifier
                    .graphicsLayer {
                        this.translationY = translationY
                        this.scaleX = scale
                        this.scaleY = scale
                        this.alpha = alpha
                    }
                    .padding(vertical = 16.dp)
                    .size(250.dp, 160.dp)
            )
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
