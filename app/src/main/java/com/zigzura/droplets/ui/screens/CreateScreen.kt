package com.zigzura.droplets.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zigzura.droplets.R
import com.zigzura.droplets.data.PromptStyles

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateScreen(
    prompt: String,
    onPromptChange: (String) -> Unit,
    onSubmit: (String) -> Unit,
    onAppCreated: (String) -> Unit = {},
    isLoading: Boolean
) {
    var selectedStyle by remember { mutableStateOf(PromptStyles.DEFAULT) }
    var customStyle by remember { mutableStateOf("") }
    var showCustomStyleDialog by remember { mutableStateOf(false) }

    // Create template suggestions using string resources
    val templateSuggestions = remember {
        listOf(
            AppTemplate(
                name = "", // Will be set in UI
                emoji = "✅",
                description = "", // Will be set in UI
                prompt = "" // Will be set in UI
            ),
            AppTemplate(
                name = "",
                emoji = "🎯", 
                description = "",
                prompt = ""
            ),
            AppTemplate(
                name = "",
                emoji = "📝",
                description = "",
                prompt = ""
            ),
            AppTemplate(
                name = "",
                emoji = "🧮",
                description = "",
                prompt = ""
            ),
            AppTemplate(
                name = "",
                emoji = "⏰",
                description = "",
                prompt = ""
            )
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFFFFD84E), // Your app's yellow background
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.create_new_app),
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
                // Style selection card
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
                            text = stringResource(R.string.pick_a_style),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black.copy(alpha = 0.8f),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(listOf(PromptStyles.DEFAULT, PromptStyles.FUN, PromptStyles.SIMPLE)) { style ->
                                StyleCard(
                                    style = style,
                                    isSelected = selectedStyle == style,
                                    onClick = { selectedStyle = style }
                                )
                            }
                            item {
                                CustomStyleCard(
                                    isSelected = selectedStyle == "Custom",
                                    onClick = {
                                        showCustomStyleDialog = true
                                    }
                                )
                            }
                        }
                    }
                }
            }

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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.describe_your_app),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black.copy(alpha = 0.8f)
                            )

                            // Clear text button
                            if (prompt.isNotBlank()) {
                                IconButton(
                                    onClick = { onPromptChange("") },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Clear,
                                        contentDescription = stringResource(R.string.clear_text),
                                        tint = Color.Gray,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = prompt,
                            onValueChange = onPromptChange,
                            placeholder = {
                                Text(
                                    stringResource(R.string.app_description_placeholder),
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
                            onClick = {
                                val styleToUse = if (selectedStyle == "Custom") customStyle else selectedStyle
                                onSubmit(styleToUse)
                            },
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
                                Text(stringResource(R.string.generating))
                            } else {
                                Icon(
                                    Icons.AutoMirrored.Filled.Send,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    stringResource(R.string.generate_app),
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
                        text = stringResource(R.string.quick_templates),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black.copy(alpha = 0.8f),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                // Use separate items for each template with string resources
                item {
                    TemplateCard(
                        emoji = "✅",
                        name = stringResource(R.string.template_todo_name),
                        description = stringResource(R.string.template_todo_description),
                        prompt = stringResource(R.string.template_todo_prompt),
                        onPromptChange = onPromptChange
                    )
                }
                
                item {
                    TemplateCard(
                        emoji = "🎯",
                        name = stringResource(R.string.template_habit_name),
                        description = stringResource(R.string.template_habit_description),
                        prompt = stringResource(R.string.template_habit_prompt),
                        onPromptChange = onPromptChange
                    )
                }
                
                item {
                    TemplateCard(
                        emoji = "📝",
                        name = stringResource(R.string.template_notes_name),
                        description = stringResource(R.string.template_notes_description),
                        prompt = stringResource(R.string.template_notes_prompt),
                        onPromptChange = onPromptChange
                    )
                }
                
                item {
                    TemplateCard(
                        emoji = "🧮",
                        name = stringResource(R.string.template_calculator_name),
                        description = stringResource(R.string.template_calculator_description),
                        prompt = stringResource(R.string.template_calculator_prompt),
                        onPromptChange = onPromptChange
                    )
                }
                
                item {
                    TemplateCard(
                        emoji = "⏰",
                        name = stringResource(R.string.template_timer_name),
                        description = stringResource(R.string.template_timer_description),
                        prompt = stringResource(R.string.template_timer_prompt),
                        onPromptChange = onPromptChange
                    )
                }
            }
        }
    }

    // Custom style dialog
    if (showCustomStyleDialog) {
        CustomStyleDialog(
            currentStyle = customStyle,
            onDismiss = { showCustomStyleDialog = false },
            onSave = { style ->
                customStyle = style
                selectedStyle = "Custom"
                showCustomStyleDialog = false
            }
        )
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

@Composable
fun StyleCard(
    style: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) Color(0xFFFFB74D) else Color.White
    val textColor = if (isSelected) Color.White else Color.Black.copy(alpha = 0.8f)
    val borderColor = if (isSelected) Color(0xFFFFB74D) else Color.Gray.copy(alpha = 0.3f)

    Surface(
        modifier = Modifier
            .clickable { onClick() }
            .width(100.dp)
            .height(80.dp),
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        shadowElevation = if (isSelected) 6.dp else 2.dp,
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = borderColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = getStyleEmoji(style),
                fontSize = 24.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = style,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = textColor,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun CustomStyleCard(
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) Color(0xFFFFB74D) else Color.White
    val textColor = if (isSelected) Color.White else Color.Black.copy(alpha = 0.8f)
    val borderColor = if (isSelected) Color(0xFFFFB74D) else Color.Gray.copy(alpha = 0.3f)

    Surface(
        modifier = Modifier
            .clickable { onClick() }
            .width(100.dp)
            .height(80.dp),
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        shadowElevation = if (isSelected) 6.dp else 2.dp,
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = borderColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "✨",
                fontSize = 24.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = stringResource(R.string.style_custom),
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = textColor,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun CustomStyleDialog(
    currentStyle: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var styleText by remember { mutableStateOf(currentStyle) }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = stringResource(R.string.custom_style),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black.copy(alpha = 0.8f),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = stringResource(R.string.custom_style_description),
                    fontSize = 14.sp,
                    color = Color.Black.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = styleText,
                    onValueChange = { styleText = it },
                    placeholder = {
                        Text(stringResource(R.string.custom_style_placeholder))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFFB74D),
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color.Gray
                        )
                    ) {
                        Text(stringResource(R.string.cancel))
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = { onSave(styleText.trim()) },
                        enabled = styleText.trim().isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFB74D),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(stringResource(R.string.save_style))
                    }
                }
            }
        }
    }
}

fun getStyleEmoji(style: String): String {
    return when (style) {
        PromptStyles.DEFAULT -> "🎨"
        PromptStyles.FUN -> "🎯"
        PromptStyles.SIMPLE -> "✨"
        else -> "🎨"
    }
}

@Composable
fun TemplateCard(
    emoji: String,
    name: String,
    description: String,
    prompt: String,
    onPromptChange: (String) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPromptChange(prompt) },
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.8f),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = emoji,
                fontSize = 24.sp,
                modifier = Modifier.padding(end = 12.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black.copy(alpha = 0.8f)
                )
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = Color.Black.copy(alpha = 0.6f)
                )
            }
        }
    }
}
