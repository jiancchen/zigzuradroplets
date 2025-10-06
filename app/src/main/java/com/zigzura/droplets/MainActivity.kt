package com.zigzura.droplets

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import com.zigzura.droplets.navigation.Screen
import com.zigzura.droplets.ui.screens.AppViewScreen
import com.zigzura.droplets.ui.screens.DebugScreen
import com.zigzura.droplets.ui.screens.MainScreen
import com.zigzura.droplets.ui.screens.SignupScreen
import com.zigzura.droplets.ui.screens.SplashScreen
import com.zigzura.droplets.ui.theme.DropletsTheme
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

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToSignup = {
                    navController.navigate(Screen.Signup.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToMain = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Signup.route) {
            SignupScreen(
                onNavigateToMain = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Signup.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Main.route) {
            MainScreen(
                onNavigateToSignup = {
                    navController.navigate(Screen.Signup.route)
                },
                onNavigateToDebug = {
                    navController.navigate(Screen.Debug.route)
                },
                onNavigateToAppView = { appId ->
                    navController.navigate(Screen.AppView.createRoute(appId))
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

        composable(
            route = Screen.AppView.route,
            arguments = listOf(navArgument("appId") { type = NavType.StringType })
        ) { backStackEntry ->
            val appId = backStackEntry.arguments?.getString("appId") ?: return@composable
            val viewModel: MainViewModel = hiltViewModel()
            val promptHistory by viewModel.promptHistory.collectAsState(initial = emptyList())
            val currentHtml by viewModel.currentHtml.collectAsState()

            val historyItem = promptHistory.find { it.id == appId }

            if (historyItem != null) {
                // Load the app if not already loaded
                LaunchedEffect(appId) {
                    if (currentHtml.isEmpty() || viewModel.currentHistoryItem.value?.id != appId) {
                        viewModel.loadHistoryItem(historyItem)
                    }
                }

                AppViewScreen(
                    historyItem = historyItem,
                    htmlContent = currentHtml,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onToggleFavorite = { id ->
                        viewModel.toggleFavorite(id)
                    },
                    onUpdateTitle = { id, title ->
                        viewModel.updateTitle(id, title)
                    },
                    onUpdateScreenshot = { id, screenshotPath ->
                        viewModel.updateScreenshot(id, screenshotPath)
                    }
                )
            }
        }
    }
}
