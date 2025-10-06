package com.zigzura.droplets.utils

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import kotlinx.coroutines.delay

object ScrollStateManager {
    private var savedScrollPosition: Pair<Int, Int>? = null

    fun saveScrollPosition(firstVisibleItemIndex: Int, firstVisibleItemScrollOffset: Int) {
        savedScrollPosition = Pair(firstVisibleItemIndex, firstVisibleItemScrollOffset)
    }

    fun getSavedScrollPosition(): Pair<Int, Int>? = savedScrollPosition

    fun clearScrollPosition() {
        savedScrollPosition = null
    }
}

@Composable
fun rememberScrollStateWithPreservation(): LazyListState {
    // Create the scroll state with rememberSaveable for process death survival
    val scrollState = rememberSaveable(saver = LazyListState.Saver) {
        LazyListState()
    }

    // Track if we've restored the position to avoid repeated attempts
    var hasRestoredPosition by remember { mutableStateOf(false) }

    // Restore scroll position after the LazyList is fully composed and laid out
    LaunchedEffect(scrollState) {
        if (!hasRestoredPosition) {
            val saved = ScrollStateManager.getSavedScrollPosition()
            if (saved != null) {
                // Add a small delay to ensure the LazyList is fully composed
                delay(100)
                try {
                    scrollState.scrollToItem(saved.first, saved.second)
                    hasRestoredPosition = true
                } catch (e: Exception) {
                    // If scrolling fails, try without offset
                    try {
                        scrollState.scrollToItem(saved.first, 0)
                        hasRestoredPosition = true
                    } catch (e2: Exception) {
                        // If that also fails, just mark as restored to avoid infinite attempts
                        hasRestoredPosition = true
                    }
                }
            } else {
                hasRestoredPosition = true
            }
        }
    }

    // Save scroll position when the composable is disposed
    DisposableEffect(scrollState) {
        onDispose {
            if (scrollState.layoutInfo.totalItemsCount > 0) {
                ScrollStateManager.saveScrollPosition(
                    scrollState.firstVisibleItemIndex,
                    scrollState.firstVisibleItemScrollOffset
                )
            }
        }
    }

    return scrollState
}
