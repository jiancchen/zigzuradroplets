package com.zigzura.droplets.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zigzura.droplets.viewmodel.SignupViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToSignup: () -> Unit,
    onNavigateToMain: () -> Unit
) {
    val signupViewModel: SignupViewModel = hiltViewModel()
    val existingApiKey by signupViewModel.existingApiKey.collectAsState(initial = null)

    LaunchedEffect(existingApiKey) {
        delay(2000) // Show splash for 2 seconds
        if (!existingApiKey.isNullOrBlank()) {
            onNavigateToMain()
        } else {
            onNavigateToSignup()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "💧",
                fontSize = 80.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Droplets",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "AI-Powered HTML Generation",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))
            CircularProgressIndicator()
        }
    }
}
