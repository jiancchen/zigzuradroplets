package com.zigzura.droplets.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zigzura.droplets.data.PromptHistory
import kotlin.math.roundToInt

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

    // State for swipe gesture on favorites section
    var offsetY by remember { mutableFloatStateOf(0f) }
    var dragOpacity by remember { mutableFloatStateOf(1f) }
    val swipeThreshold = -100f // Threshold to dismiss when swiped up

    // State for swipe gesture on search bar
    var searchBarOffsetY by remember { mutableFloatStateOf(0f) }
    val searchBarSwipeThreshold = 100f // Threshold to show favorites when swiped down

    val draggableState = rememberDraggableState { delta ->
        val newOffsetY = (offsetY + delta).coerceAtMost(0f)
        offsetY = newOffsetY
        // Calculate opacity based on drag distance - fade out as user drags up
        dragOpacity = if (newOffsetY < 0) {
            (1f + (newOffsetY / swipeThreshold)).coerceAtLeast(0.3f)
        } else {
            1f
        }
    }

    val searchBarDraggableState = rememberDraggableState { delta ->
        searchBarOffsetY = (searchBarOffsetY + delta).coerceIn(0f, 50f) // Limit downward drag to 150px
    }

    // Calculate most used apps (you can implement your own logic here)
    val mostUsedItems = remember(favoriteItems) {
        // For now, using sample logic - you can replace with actual usage tracking
        favoriteItems.shuffled().take(5) // Placeholder for most used logic
    }

    // Reset offset and opacity when favorites become invisible
    LaunchedEffect(showFavorites) {
        if (!showFavorites) {
            offsetY = 0f
            dragOpacity = 1f
        }
    }

    Column(modifier = modifier) {
        // Semi-transparent white pill search bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .offset { IntOffset(0, searchBarOffsetY.roundToInt()) }
                .draggable(
                    state = searchBarDraggableState,
                    orientation = Orientation.Vertical,
                    onDragStopped = { velocity ->
                        if (searchBarOffsetY > searchBarSwipeThreshold || velocity > 500f) {
                            // User swiped down enough or with enough velocity - show favorites
                            if (!showFavorites) {
                                onToggleFavorites()
                            }
                            searchBarOffsetY = 0f // Reset offset
                        } else {
                            // Snap back to original position
                            searchBarOffsetY = 0f
                        }
                    }
                ),
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

        // White box with favorites and most used (swipeable to dismiss)
        AnimatedVisibility(
            visible = showFavorites && searchText.text.isBlank(),
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .draggable(
                        state = draggableState,
                        orientation = Orientation.Vertical,
                        onDragStopped = { velocity ->
                            if (offsetY < swipeThreshold || velocity < -500f) {
                                // User swiped up enough or with enough velocity - dismiss
                                onToggleFavorites()
                            } else {
                                // Snap back to original position
                                offsetY = 0f
                                dragOpacity = 1f
                            }
                        }
                    ),
                shape = RoundedCornerShape(16.dp),
                color = Color.White.copy(alpha = 0.95f * dragOpacity),
                shadowElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .graphicsLayer { alpha = dragOpacity }
                ) {
                    // Favorite Apps Section - always show title
                    Text(
                        text = "Favorite Apps",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black.copy(alpha = 0.8f),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    if (favoriteItems.isNotEmpty()) {
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
                    } else {
                        Text(
                            text = "No favorite apps yet",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Most Used Apps Section - always show title
                    Text(
                        text = "Most Used Apps",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black.copy(alpha = 0.8f),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    if (mostUsedItems.isNotEmpty()) {
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
                    } else {
                        Text(
                            text = "No usage data yet",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}