package com.zigzura.droplets.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.zigzura.droplets.Weblet
import com.zigzura.droplets.data.PromptConstant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugScreen(
    onNavigateBack: () -> Unit
) {
    var htmlInput by remember {
        mutableStateOf(PromptConstant.PROMPT2)
    }

    var currentHtml by remember { mutableStateOf("") }
    var showPreview by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Debug HTML") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            currentHtml = htmlInput
                            showPreview = true
                        }
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Load HTML")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (!showPreview) {
                // HTML Input Section
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Enter HTML to test:",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        OutlinedTextField(
                            value = htmlInput,
                            onValueChange = { htmlInput = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(400.dp),
                            placeholder = { Text("Enter your HTML code here...") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Button(
                                onClick = {
                                    currentHtml = htmlInput
                                    showPreview = true
                                }
                            ) {
                                Text("Load HTML")
                            }

                            Button(
                                onClick = {
                                    htmlInput = """<!DOCTYPE html>
<html><head><title>Simple Test</title></head>
<body style="margin:20px; font-family:Arial;">
<h1>Simple Test</h1>
<p>This is a minimal HTML test.</p>
</body></html>"""
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary
                                )
                            ) {
                                Text("Load Simple")
                            }
                        }
                    }
                }

                // Instructions
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Debug Instructions:",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = "• Paste your calculator HTML or any other HTML\n" +
                                  "• Click 'Load HTML' or the play button to test\n" +
                                  "• Use 'Load Simple' for a minimal test case\n" +
                                  "• Check if content renders at the top correctly\n" +
                                  "• Look for viewport or positioning issues",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                // Preview Section
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "HTML Preview:",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Button(
                        onClick = { showPreview = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Text("Edit HTML")
                    }
                }

                // WebView Container
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Weblet(htmlContent = currentHtml, appId = "test_app")
                }
            }
        }
    }
}
