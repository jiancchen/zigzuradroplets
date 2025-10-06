package com.zigzura.droplets.navigation

sealed class Screen(val route: String) {
    object LoadingSplash : Screen("loading_splash")
    object Welcome : Screen("welcome")
    object Splash : Screen("splash")
    object Signup : Screen("signup")
    object Debug : Screen("debug")
    object Stacks : Screen("stacks")
    object AppView : Screen("app_view/{appId}") {
        fun createRoute(appId: String) = "app_view/$appId"
    }
}
