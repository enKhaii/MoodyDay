package com.example.moodyday.navigation

sealed class NavRoutes(val route: String) {
    // Splash Screen (NOT ACTUAL SCREEN)
    object Splash : NavRoutes("splash")

    // Auth screens (NOT IN BOTTOM BAR)
    object Onboarding : NavRoutes("onboard")
    object Login : NavRoutes("login")
    object Register : NavRoutes("register")

    // Main App screens (IN BOTTOM BAR)
    object Home : NavRoutes("home")
    object Forecast : NavRoutes("forecast")
    object Alerts : NavRoutes("alerts")
    object Tips : NavRoutes("tips")
    object Goals : NavRoutes("goals")

    // Alone screen (Icon in Home Screen)
    object Settings : NavRoutes("settings")
}