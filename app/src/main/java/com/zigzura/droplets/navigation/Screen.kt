package com.zigzura.droplets.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Signup : Screen("signup")
    object Main : Screen("main")
    object Debug : Screen("debug")
    object AppView : Screen("app_view/{appId}") {
        fun createRoute(appId: String) = "app_view/$appId"
    }
}
