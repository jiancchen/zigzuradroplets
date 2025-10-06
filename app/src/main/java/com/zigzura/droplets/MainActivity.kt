package com.zigzura.droplets

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.zigzura.droplets.navigation.Screen
import com.zigzura.droplets.ui.screens.DebugScreen
import com.zigzura.droplets.ui.screens.SignupScreen
import com.zigzura.droplets.ui.screens.SplashScreen
import com.zigzura.droplets.ui.screens.StacksScreen
import com.zigzura.droplets.ui.screens.LoadingSplashScreen
import com.zigzura.droplets.ui.screens.welcome.WelcomeNavigationScreen
import com.zigzura.droplets.ui.theme.DropletsTheme
import com.zigzura.droplets.utils.PreferenceManager
import com.zigzura.droplets.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            DropletsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DropletsNavigation()
                }
            }
        }
    }
}

@Composable
fun DropletsNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    var isFirstTimeUser by remember { mutableStateOf(true) }

    // Check if user is first time on app start
    LaunchedEffect(Unit) {
        isFirstTimeUser = PreferenceManager.isFirstTimeUser(context)
    }

    NavHost(
        navController = navController,
        startDestination = Screen.LoadingSplash.route
    ) {
        composable(Screen.LoadingSplash.route) {
            LoadingSplashScreen(
                onNavigateToStacks = {
                    if (isFirstTimeUser) {
                        navController.navigate(Screen.Welcome.route) {
                            popUpTo(Screen.LoadingSplash.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.Stacks.route) {
                            popUpTo(Screen.LoadingSplash.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Screen.Welcome.route) {
            WelcomeNavigationScreen(
                onCompleteWelcome = {
                    PreferenceManager.setWelcomeCompleted(context)
                    navController.navigate(Screen.Stacks.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Stacks.route) {
            val viewModel: MainViewModel = hiltViewModel()
            val promptHistory by viewModel.promptHistory.collectAsState(initial = emptyList())

            StacksScreen(
                promptHistory = promptHistory,
                onNavigateToApp = { appId ->
                    // Navigate to the specific app - you can customize this behavior
                    // For now, we'll just log or handle as needed
                    // Since AppViewScreen is being removed, you might want to handle this differently
                },
                onNavigateToMain = {
                    // Since MainScreen is removed, we can navigate to Debug or handle differently
                    navController.navigate(Screen.Debug.route)
                },
                onNavigateToCreate = {
                    // You can implement a create flow here or navigate to a specific screen
                    // For now, navigate to debug as placeholder
                    navController.navigate(Screen.Debug.route)
                },
                onNavigateToSettings = {
                    // You can implement settings screen or navigate to signup for now
                    navController.navigate(Screen.Signup.route)
                }
            )
        }

        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToSignup = {
                    navController.navigate(Screen.Signup.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToMain = {
                    navController.navigate(Screen.Stacks.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Signup.route) {
            SignupScreen(
                onNavigateToMain = {
                    navController.navigate(Screen.Stacks.route) {
                        popUpTo(Screen.Signup.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Debug.route) {
            DebugScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
