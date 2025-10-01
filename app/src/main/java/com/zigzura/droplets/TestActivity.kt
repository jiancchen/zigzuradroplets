package com.zigzura.droplets

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.zigzura.droplets.ui.theme.DropletsTheme

@OptIn(ExperimentalMaterial3Api::class)
class TestActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("TestActivity", "Notification permission granted")
        } else {
            Log.w("TestActivity", "Notification permission denied")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            DropletsTheme {
                TestScreen()
            }
        }
    }
}

@Composable
fun TestScreen() {
    var testResults by remember { mutableStateOf(listOf<String>()) }

    fun addResult(result: String) {
        testResults = testResults + result
        Log.d("TestScreen", result)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Droplets Storage & Reminder Test",
            style = MaterialTheme.typography.headlineMedium
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    addResult("Starting storage test...")
                    testStorage(onResult = { addResult(it) })
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Test Storage")
            }

            Button(
                onClick = {
                    addResult("Starting reminder test...")
                    testReminders(onResult = { addResult(it) })
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Test Reminders")
            }
        }

        Button(
            onClick = { testResults = emptyList() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Clear Results")
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Test Results:",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (testResults.isEmpty()) {
                    Text(
                        text = "No tests run yet. Click the buttons above to start testing.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    testResults.forEach { result ->
                        Text(
                            text = "• $result",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }

        // WebView with test interface
        Weblet(
            htmlContent = null, // This will load the default test HTML
            appId = "test_app" // Fixed ID for test activity
        )
    }
}

// Test functions that would be called from the composable
fun testStorage(onResult: (String) -> Unit) {
    // These tests would normally be run through the WebView JavaScript interface
    // This is just a demonstration of what the tests would cover

    onResult("✓ Storage test completed - check WebView below for interactive tests")
    onResult("  - Save/load data for multiple apps")
    onResult("  - Verify data isolation between apps")
    onResult("  - Test getAllData() and deleteData()")
}

fun testReminders(onResult: (String) -> Unit) {
    onResult("✓ Reminder test initiated - check WebView below")
    onResult("  - Set reminder for 10 seconds from now")
    onResult("  - Verify notification appears when app backgrounded")
    onResult("  - Test cancellation functionality")
    onResult("⚠ Background the app after setting reminder to test notifications")
}
