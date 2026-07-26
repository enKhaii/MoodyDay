package com.example.moodyday.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.moodyday.ui.alerts.AlertsScreen
import com.example.moodyday.ui.forecast.ForecastScreen
import com.example.moodyday.ui.goals.GoalsScreen
import com.example.moodyday.ui.settings.SettingsScreen
import com.example.moodyday.ui.tips.TipsScreen
import com.example.moodyday.ui.user.LoginScreen
import com.example.moodyday.ui.weather.HomeScreen

@Composable
fun NavHostSetup(
    navController : NavHostController,
    modifier : Modifier = Modifier
){
    NavHost(
        navController = navController,
        startDestination = NavRoutes.Login.route,
        modifier = modifier
    ){
        composable(route = NavRoutes.Login.route){
            LoginScreen(navController = navController)
        }
        composable(route = NavRoutes.Home.route){
            HomeScreen()
        }
        composable(route = NavRoutes.Forecast.route){
            ForecastScreen()
        }
        composable(route = NavRoutes.Alerts.route){
            AlertsScreen()
        }
        composable(route = NavRoutes.Tips.route){
            TipsScreen()
        }
        composable(route = NavRoutes.Goals.route){
            GoalsScreen()
        }
        composable(route = NavRoutes.Settings.route){
            SettingsScreen()
        }
    }
}