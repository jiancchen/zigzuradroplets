package com.zigzura.droplets.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateScreen(
    prompt: String,
    onPromptChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onAppCreated: (String) -> Unit = {},
    isLoading: Boolean
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFFFFD84E), // Your app's yellow background
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Create New App",
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                // Main prompt input card
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
                            text = "Describe your app",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black.copy(alpha = 0.8f),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        OutlinedTextField(
                            value = prompt,
                            onValueChange = onPromptChange,
                            placeholder = {
                                Text(
                                    "I want an app that tracks my daily habits...",
                                    color = Color.Gray
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            enabled = !isLoading,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFFB74D),
                                unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                                focusedTextColor = Color.Black.copy(alpha = 0.8f),
                                unfocusedTextColor = Color.Black.copy(alpha = 0.8f),
                                cursorColor = Color(0xFFFF8A65)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = onSubmit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            enabled = !isLoading && prompt.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFFB74D),
                                contentColor = Color.White,
                                disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Generating...")
                            } else {
                                Icon(
                                    Icons.AutoMirrored.Filled.Send,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Generate App",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // Quick templates section
            if (!isLoading) {
                item {
                    Text(
                        text = "Quick Templates",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black.copy(alpha = 0.8f),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                items(templateSuggestions) { template ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPromptChange(template.prompt) },
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.8f),
                        shadowElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = template.emoji,
                                fontSize = 24.sp,
                                modifier = Modifier.padding(end = 12.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = template.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black.copy(alpha = 0.8f)
                                )
                                Text(
                                    text = template.description,
                                    fontSize = 12.sp,
                                    color = Color.Black.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Template suggestions data
data class AppTemplate(
    val name: String,
    val emoji: String,
    val description: String,
    val prompt: String
)

val templateSuggestions = listOf(
    AppTemplate(
        name = "Todo List",
        emoji = "✅",
        description = "Simple task management",
        prompt = "Create a todo list app with add, delete, and mark complete functionality"
    ),
    AppTemplate(
        name = "Habit Tracker",
        emoji = "🎯",
        description = "Track daily habits",
        prompt = "Create a habit tracker that lets me check off daily habits and shows streaks"
    ),
    AppTemplate(
        name = "Note Taking",
        emoji = "📝",
        description = "Quick notes and memos",
        prompt = "Create a simple note-taking app where I can add, edit, and delete notes"
    ),
    AppTemplate(
        name = "Calculator",
        emoji = "🧮",
        description = "Basic calculator",
        prompt = "Create a calculator app with basic arithmetic operations"
    ),
    AppTemplate(
        name = "Timer",
        emoji = "⏰",
        description = "Countdown timer",
        prompt = "Create a countdown timer app with start, pause, and reset functionality"
    )
)
