package com.zigzura.droplets.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Signup : Screen("signup")
    object Main : Screen("main")
    object Debug : Screen("debug")
}
