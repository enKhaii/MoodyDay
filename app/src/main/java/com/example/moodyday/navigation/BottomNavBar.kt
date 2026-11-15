package com.example.moodyday.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavBar(
    val destination: NavRoutes,
    val title: String,
    val icon: ImageVector
) {
    val route: String get() = destination.route
    object Home: BottomNavBar(
        destination = NavRoutes.Home,
        title = "Home",
        icon = Icons.Default.Home
    )
    object Forecast: BottomNavBar(
        destination = NavRoutes.Forecast,
        title = "Forecast",
        icon = Icons.Default.WbSunny
    )
    object Alerts: BottomNavBar(
        destination = NavRoutes.Alerts,
        title = "Alerts",
        icon = Icons.Default.Warning
    )
    object Tips: BottomNavBar(
        destination = NavRoutes.Tips,
        title = "Tips",
        icon = Icons.Default.Lightbulb
    )
    object Goals: BottomNavBar(
        destination = NavRoutes.Goals,
        title = "Goals",
        icon = Icons.Default.CheckCircle
    )

    companion object{
        // List of screens to display in the bottom navigation bar
        val items = listOf(Home, Forecast, Alerts, Tips, Goals)
    }
}