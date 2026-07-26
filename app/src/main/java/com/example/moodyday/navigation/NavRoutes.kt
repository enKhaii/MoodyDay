package com.example.moodyday.navigation

sealed class NavRoutes(val route: String) {
    // Auth screens (NOT IN BOTTOM BAR)
    object Login : NavRoutes("login")

    // Main App screens (IN BOTTOM BAR)
    object Home : NavRoutes("home")
    object Forecast : NavRoutes("forecast")
    object Alerts : NavRoutes("alerts")
    object Tips : NavRoutes("tips")
    object Goals : NavRoutes("goals")

    // Alone screen (Icon in Home Screen)
    object Settings : NavRoutes("settings")
}