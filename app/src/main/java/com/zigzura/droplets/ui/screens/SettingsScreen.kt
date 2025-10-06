package com.zigzura.droplets.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zigzura.droplets.data.PromptHistory
import com.zigzura.droplets.data.PreferencesManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToSignup: () -> Unit,
    onNavigateToDebug: () -> Unit,
    onNavigateToStacks: () -> Unit = {},
    promptHistory: List<PromptHistory>
) {
    val context = LocalContext.current
    val preferencesManager = remember { PreferencesManager(context) }
    val scope = rememberCoroutineScope()

    // Load preferences
    val savedTemperature by preferencesManager.temperature.collectAsState(initial = 0.3f)
    val savedModel by preferencesManager.claudeModel.collectAsState(initial = "claude-3-5-sonnet-20241022")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFFFD84E),
                        Color(0xFFFFC107),
                        Color(0xFFFF9800)
                    )
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // API Configuration Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            text = "API Configuration",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B),
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        SettingsItem(
                            title = "API Key Settings",
                            description = "Configure your Claude API key",
                            onClick = onNavigateToSignup
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        SettingsItem(
                            title = "Debug Mode",
                            description = "Enable debugging features",
                            onClick = onNavigateToDebug
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        SettingsItem(
                            title = "3D Stacks View",
                            description = "View your apps in a 3D perspective stack",
                            onClick = onNavigateToStacks
                        )
                    }
                }
            }

            // Claude Model Configuration Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            text = "Claude Model Settings",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B),
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Model Selection
                        Column(
                            modifier = Modifier.padding(bottom = 20.dp)
                        ) {
                            Text(
                                text = "Model",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            var expanded by remember { mutableStateOf(false) }
                            var selectedModel by remember { mutableStateOf(savedModel) }

                            val models = listOf(
                                "claude-opus-4-1@20250805" to "Claude 4.1 Opus (Latest)",
                                "claude-opus-4@20250514" to "Claude 4 Opus",
                                "claude-sonnet-4@20250514" to "Claude 4 Sonnet",
                                "claude-3-7-sonnet@20250219" to "Claude 3.7 Sonnet",
                                "claude-3-5-haiku@20241022" to "Claude 3.5 Haiku",
                                "claude-3-5-sonnet-v2@20241022" to "Claude 3.5 Sonnet v2",
                                "claude-3-opus@20240229" to "Claude 3 Opus",
                                "claude-3-haiku@20240307" to "Claude 3 Haiku"
                            )

                            // Update saved model state when preferences change
                            LaunchedEffect(savedModel) {
                                selectedModel = savedModel
                            }

                            ExposedDropdownMenuBox(
                                expanded = expanded,
                                onExpandedChange = { expanded = !expanded }
                            ) {
                                OutlinedTextField(
                                    value = models.find { it.first == selectedModel }?.second ?: selectedModel,
                                    onValueChange = { },
                                    readOnly = true,
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF6366F1),
                                        unfocusedBorderColor = Color(0xFFE2E8F0),
                                        focusedLabelColor = Color(0xFF6366F1),
                                        unfocusedLabelColor = Color(0xFF64748B)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                ExposedDropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    models.forEach { (value, label) ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    label,
                                                    color = Color(0xFF1E293B)
                                                )
                                            },
                                            onClick = {
                                                selectedModel = value
                                                expanded = false
                                                scope.launch {
                                                    preferencesManager.saveClaudeModel(value)
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Temperature Setting
                        Column {
                            Text(
                                text = "Temperature",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B),
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            var temperature by remember { mutableFloatStateOf(savedTemperature) }

                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "0.0 (Deterministic)",
                                        fontSize = 12.sp,
                                        color = Color(0xFF64748B)
                                    )
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color(0xFF6366F1)
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = String.format("%.1f", temperature),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                        )
                                    }
                                    Text(
                                        text = "1.0 (Creative)",
                                        fontSize = 12.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Slider(
                                    value = temperature,
                                    onValueChange = {
                                        temperature = it
                                        scope.launch {
                                            preferencesManager.saveTemperature(it)
                                        }
                                    },
                                    valueRange = 0f..1f,
                                    steps = 9, // Creates 0.1 increments
                                    colors = SliderDefaults.colors(
                                        thumbColor = Color(0xFF6366F1),
                                        activeTrackColor = Color(0xFF6366F1),
                                        inactiveTrackColor = Color(0xFFE2E8F0)
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Text(
                                    text = when {
                                        temperature <= 0.2f -> "Very deterministic - consistent, focused responses"
                                        temperature <= 0.5f -> "Balanced - good mix of consistency and creativity"
                                        temperature <= 0.8f -> "Creative - more varied and innovative responses"
                                        else -> "Very creative - highly varied, experimental responses"
                                    },
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B),
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Storage & Data Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            text = "Storage & Data",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B),
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Stats cards
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFF8FAFC)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "${promptHistory.size}",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF6366F1)
                                    )
                                    Text(
                                        text = "Apps Created",
                                        fontSize = 12.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }

                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFF8FAFC)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "${promptHistory.count { it.favorite == true }}",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFEF4444)
                                    )
                                    Text(
                                        text = "Favorites",
                                        fontSize = 12.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // About Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🌊",
                            fontSize = 40.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = "Droplets v1.0.0",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = "AI-Powered Mini-Apps",
                            fontSize = 14.sp,
                            color = Color(0xFF64748B),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Text(
                            text = "© 2025 AI Mini-Apps",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsItem(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF8FAFC)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1E293B)
                )
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
            }
            Icon(
                Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = Color(0xFF94A3B8),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
