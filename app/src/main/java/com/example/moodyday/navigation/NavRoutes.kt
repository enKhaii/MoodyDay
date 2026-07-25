package com.example.moodyday.navigation

sealed class NavRoutes(val route: String) {
    object Login : NavRoutes("login")
    object Home : NavRoutes("home")
    object Forecast : NavRoutes("forecast")
    object Alerts : NavRoutes("alerts")
    object Goals : NavRoutes("goals")
    object Settings : NavRoutes("settings")
}