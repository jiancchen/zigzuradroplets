package com.zigzura.droplets.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zigzura.droplets.Weblet
import com.zigzura.droplets.data.PromptHistory
import com.zigzura.droplets.ui.components.HistoryItem

@Composable
fun MyAppsScreen(
    promptHistory: List<PromptHistory>,
    showFavoritesOnly: Boolean,
    onShowFavoritesToggle: (Boolean) -> Unit,
    onHistoryItemClick: (PromptHistory) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onUpdateTitle: (String, String) -> Unit,
    onClearHistory: () -> Unit,
    currentHistoryItem: PromptHistory?,
    currentHtml: String,
    isLoading: Boolean
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Current App Display
        if (currentHtml.isNotEmpty() && currentHistoryItem != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Weblet(
                    htmlContent = currentHtml,
                    appId = currentHistoryItem.id
                )
            }
        }

        // Filters and Controls
        if (promptHistory.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = showFavoritesOnly,
                        onCheckedChange = onShowFavoritesToggle,
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFF6366F1)
                        )
                    )
                    Text(
                        text = "Favorites only",
                        fontSize = 14.sp,
                        color = Color(0xFF475569)
                    )
                }

                TextButton(
                    onClick = onClearHistory
                ) {
                    Text(
                        text = "Clear All",
                        color = Color(0xFFEF4444)
                    )
                }
            }
        }

        // History List or Empty State
        if (promptHistory.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🎨",
                        fontSize = 64.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No apps yet",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Text(
                        text = "Create your first app to get started",
                        fontSize = 14.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(promptHistory) { historyItem ->
                    HistoryItem(
                        historyItem = historyItem,
                        onClick = { onHistoryItemClick(historyItem) },
                        onToggleFavorite = onToggleFavorite,
                        onUpdateTitle = onUpdateTitle
                    )
                }
            }
        }
    }
}
