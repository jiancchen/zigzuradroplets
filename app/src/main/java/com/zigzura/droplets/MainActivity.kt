package com.zigzura.droplets

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.hilt.navigation.compose.hiltViewModel
import com.zigzura.droplets.data.LanguageManager
import com.zigzura.droplets.navigation.Screen
import com.zigzura.droplets.ui.screens.AppViewScreen
import com.zigzura.droplets.ui.screens.DebugScreen
import com.zigzura.droplets.ui.screens.SignupScreen
import com.zigzura.droplets.ui.screens.SplashScreen
import com.zigzura.droplets.ui.screens.StacksScreen
import com.zigzura.droplets.ui.screens.LoadingSplashScreen
import com.zigzura.droplets.ui.screens.welcome.WelcomeNavigationScreen
import com.zigzura.droplets.ui.theme.DropletsTheme
import com.zigzura.droplets.utils.AppNotificationManager
import com.zigzura.droplets.utils.PermissionManager
import com.zigzura.droplets.utils.PreferenceManager
import com.zigzura.droplets.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import com.zigzura.droplets.ui.screens.CreateScreen
import com.zigzura.droplets.ui.screens.SettingsScreen
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var permissionManager: PermissionManager

    @Inject
    lateinit var appNotificationManager: AppNotificationManager

    private lateinit var languageManager: LanguageManager
    private var lastAppliedLanguage: String = LanguageManager.SYSTEM_DEFAULT

    // Permission request launcher
    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted - notifications can now be shown
        } else {
            // Permission denied - handle accordingly
        }
    }

    override fun attachBaseContext(newBase: Context?) {
        if (newBase == null) {
            super.attachBaseContext(newBase)
            return
        }

        // Initialize language manager and apply language configuration
        languageManager = LanguageManager(newBase)

        // Get the selected language synchronously (we need to block here for attachBaseContext)
        val selectedLanguage = runBlocking {
            languageManager.selectedLanguage.first()
        }

        // Apply language context
        val localizedContext = languageManager.applyLanguage(newBase, selectedLanguage)
        lastAppliedLanguage = selectedLanguage

        super.attachBaseContext(localizedContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Initialize notification channel
        appNotificationManager.createNotificationChannel(this)

        // Request notification permission if needed
        if (!permissionManager.hasNotificationPermission(this)) {
            permissionManager.requestNotificationPermission(this)
        }

        // Initialize language manager if not already done
        if (!::languageManager.isInitialized) {
            languageManager = LanguageManager(this)
        }

        setContent {
            // Monitor language changes and recreate activity when needed
            val selectedLanguage by languageManager.selectedLanguage.collectAsState(initial = lastAppliedLanguage)

            // Only recreate if language actually changed to avoid infinite loops
            LaunchedEffect(selectedLanguage) {
                if (selectedLanguage != lastAppliedLanguage && selectedLanguage != LanguageManager.SYSTEM_DEFAULT) {
                    recreate()
                }
            }

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

    override fun onResume() {
        super.onResume()
        // App is in foreground - update ViewModel
        lifecycleScope.launch {
            val viewModel = try {
                // Get the MainViewModel if available
                // Note: This is a simplified approach - in production you might want
                // to use a more robust method to access the ViewModel
                null // We'll handle this in the Composable instead
            } catch (e: Exception) {
                null
            }
        }
    }

    override fun onPause() {
        super.onPause()
        // App is going to background - update ViewModel
        // We'll handle this in the Composable to have access to the ViewModel
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
                    navController.navigate(Screen.AppView.createRoute(appId))
                },
                onNavigateToMain = {
                    // Don't navigate if already on Stacks screen - just close the FAB
                    // This prevents adding multiple entries to the stack
                },
                onNavigateToCreate = {
                    navController.navigate(Screen.Create.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
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

        composable(Screen.Settings.route) {
            val viewModel: MainViewModel = hiltViewModel()
            val promptHistory by viewModel.promptHistory.collectAsState(initial = emptyList())

            SettingsScreen(
                onNavigateToSignup = {
                    navController.navigate(Screen.Signup.route)
                },
                onNavigateToDebug = {
                    navController.navigate(Screen.Debug.route)
                },
                onNavigateToStacks = {
                    navController.popBackStack()
                },
                promptHistory = promptHistory
            )
        }

        composable(Screen.Debug.route) {
            DebugScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Create.route) {
            val viewModel: MainViewModel = hiltViewModel()
            val isLoading by viewModel.isLoading.collectAsState()
            var prompt by remember { mutableStateOf("") }

            CreateScreen(
                prompt = prompt,
                onPromptChange = { prompt = it },
                onSubmit = {
                    if (prompt.isNotBlank()) {
                        viewModel.generateHtml(prompt)
                    }
                },
                onAppCreated = { appId ->
                    navController.navigate(Screen.AppView.createRoute(appId)) {
                        popUpTo(Screen.Create.route) { inclusive = true }
                    }
                },
                isLoading = isLoading
            )

            // Navigate to newly created app when generation is complete
            val currentHistoryItem by viewModel.currentHistoryItem.collectAsState()
            val currentHtml by viewModel.currentHtml.collectAsState()

            LaunchedEffect(currentHistoryItem, currentHtml) {
                currentHistoryItem?.let { item ->
                    if (currentHtml.isNotEmpty() && prompt.isNotBlank()) {
                        navController.navigate(Screen.AppView.createRoute(item.id)) {
                            popUpTo(Screen.Create.route) { inclusive = true }
                        }
                    }
                }
            }
        }
    }
}
