package com.zigzura.droplets.ui.theme

import androidx.compose.ui.graphics.Color

object AppColors {
    // App Primary Colors
    val Primary = Color(0xFFFFD84E)      // Main yellow background
    val PrimaryDark = Color(0xFFFFC107)  // Darker yellow
    val PrimaryDeep = Color(0xFFFF9800)  // Deepest yellow

    // FAB Colors (Orange variations)
    val FABMain = Color(0xFFFFB74D)      // Golden yellow for main FAB
    val FABDarkOrange = Color(0xFFE5A800) // Dark orange for menu items
    val FABDarkerOrange = Color(0xFF4FC21A) // Darker orange
    val FABDeepOrange = Color(0xFF19A3E3)   // Deep orange

    // 3D Stack Card Background Colors
    val StackColors = listOf(
        Color(0xFFE91E63), // Pink
        Color(0xFF9C27B0), // Purple
        Color(0xFF673AB7), // Deep Purple
        Color(0xFF3F51B5), // Indigo
        Color(0xFF2196F3), // Blue
        Color(0xFF00BCD4), // Cyan
        Color(0xFF009688), // Teal
        Color(0xFF4CAF50), // Green
        Color(0xFF8BC34A), // Light Green
        Color(0xFFFF9800), // Orange
        Color(0xFFFF5722), // Deep Orange
        Color(0xFFF44336)  // Red
    )

    // Common UI Colors
    val White = Color.White
    val Black = Color.Black
    val Transparent = Color.Transparent

    // Overlay Colors
    val BackdropOverlay = Color.Black.copy(alpha = 0.3f)
}
