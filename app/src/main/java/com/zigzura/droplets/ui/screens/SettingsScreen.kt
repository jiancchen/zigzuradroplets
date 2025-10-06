package com.zigzura.droplets.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zigzura.droplets.R
import com.zigzura.droplets.constants.ClaudeModel
import com.zigzura.droplets.data.LanguageManager
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
    val savedModel by preferencesManager.claudeModel.collectAsState(initial = ClaudeModel.getDefaultModel())

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFFFFD84E), // Your app's yellow background
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black.copy(alpha = 0.8f)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // API Configuration Section
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White.copy(alpha = 0.95f),
                    shadowElevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "API Configuration",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black.copy(alpha = 0.8f),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        SettingsItem(
                            title = "API Key Settings",
                            description = "Configure your Claude API key",
                            onClick = onNavigateToSignup
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        SettingsItem(
                            title = "Debug Mode",
                            description = "Enable debugging features",
                            onClick = onNavigateToDebug
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                    }
                }
            }

            // Claude Model Configuration Section
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White.copy(alpha = 0.95f),
                    shadowElevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "Claude Model Settings",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black.copy(alpha = 0.8f),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        // Model Selection
                        Text(
                            text = "Model",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black.copy(alpha = 0.8f),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        var expanded by remember { mutableStateOf(false) }

                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            OutlinedTextField(
                                value = savedModel,
                                onValueChange = { },
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFFFB74D),
                                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                                    focusedTextColor = Color.Black.copy(alpha = 0.8f),
                                    unfocusedTextColor = Color.Black.copy(alpha = 0.8f)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )

                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                ClaudeModel.models.forEach { model ->
                                    DropdownMenuItem(
                                        text = { Text(model) },
                                        onClick = {
                                            scope.launch {
                                                preferencesManager.saveClaudeModel(model)
                                            }
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }

// After the ExposedDropdownMenuBox in your model selection section:
                        Text(
                            text = ClaudeModel.modelCosts[savedModel] ?: "",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B),
                            modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                        )


                        Spacer(modifier = Modifier.height(16.dp))

                        // Temperature Setting
                        Text(
                            text = "Temperature: ${String.format("%.1f", savedTemperature)}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black.copy(alpha = 0.8f),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Slider(
                            value = savedTemperature,
                            onValueChange = { newValue ->
                                scope.launch {
                                    preferencesManager.saveTemperature(newValue)
                                }
                            },
                            valueRange = 0f..1f,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFFFFB74D),
                                activeTrackColor = Color(0xFFFFB74D),
                                inactiveTrackColor = Color.Gray.copy(alpha = 0.3f)
                            )
                        )

                        Text(
                            text = "Lower values make responses more focused, higher values more creative",
                            fontSize = 12.sp,
                            color = Color.Black.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            // Language & Region Section
            item {
                val languageManager = remember { LanguageManager(context) }
                val selectedLanguage by languageManager.selectedLanguage.collectAsState(initial = LanguageManager.SYSTEM_DEFAULT)

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White.copy(alpha = 0.95f),
                    shadowElevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.language_region_settings),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black.copy(alpha = 0.8f),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        // Language Selection
                        Text(
                            text = stringResource(R.string.app_language),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black.copy(alpha = 0.8f),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        var languageExpanded by remember { mutableStateOf(false) }

                        ExposedDropdownMenuBox(
                            expanded = languageExpanded,
                            onExpandedChange = { languageExpanded = !languageExpanded }
                        ) {
                            OutlinedTextField(
                                value = languageManager.getLanguageDisplayName(selectedLanguage),
                                onValueChange = { },
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = languageExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFFFB74D),
                                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                                    focusedTextColor = Color.Black.copy(alpha = 0.8f),
                                    unfocusedTextColor = Color.Black.copy(alpha = 0.8f)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )

                            ExposedDropdownMenu(
                                expanded = languageExpanded,
                                onDismissRequest = { languageExpanded = false }
                            ) {
                                languageManager.availableLanguages.forEach { language ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(language.displayName)
                                                if (language.code != LanguageManager.SYSTEM_DEFAULT) {
                                                    Text(
                                                        text = language.nativeName,
                                                        fontSize = 12.sp,
                                                        color = Color.Gray
                                                    )
                                                }
                                            }
                                        },
                                        onClick = {
                                            scope.launch {
                                                languageManager.setLanguage(language.code)
                                            }
                                            languageExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        if (selectedLanguage != LanguageManager.SYSTEM_DEFAULT) {
                            Text(
                                text = stringResource(R.string.language_override_note),
                                fontSize = 12.sp,
                                color = Color(0xFF64748B),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }

            // App Statistics Section
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White.copy(alpha = 0.95f),
                    shadowElevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "App Statistics",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black.copy(alpha = 0.8f),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatItem(
                                label = "Total Apps",
                                value = promptHistory.size.toString()
                            )
                            StatItem(
                                label = "Favorites",
                                value = promptHistory.count { it.favorite == true }.toString()
                            )
                        }
                    }
                }
            }

            // Spacer after last main section
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Privacy Policy Section
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White.copy(alpha = 0.95f),
                    shadowElevation = 4.dp
                ) {
                    UrlSettingsItem(
                        title = "Privacy Policy",
                        description = "Learn how we protect your data",
                        url = "https://your-app-domain.com/privacy-policy" // Replace with your actual URL
                    )
                }
            }

            // Terms of Service Section
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White.copy(alpha = 0.95f),
                    shadowElevation = 4.dp
                ) {
                    UrlSettingsItem(
                        title = "Terms of Service",
                        description = "Read our terms and conditions",
                        url = "https://your-app-domain.com/terms-of-service" // Replace with your actual URL
                    )
                }
            }

            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(32.dp))
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

@Composable
fun StatItem(
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black.copy(alpha = 0.8f)
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Black.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun UrlSettingsItem(
    title: String,
    description: String,
    url: String
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                // Open the URL in a browser
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                context.startActivity(intent)
            },
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
                Icons.Outlined.Email,
                contentDescription = "Open in browser",
                tint = Color(0xFF94A3B8),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
