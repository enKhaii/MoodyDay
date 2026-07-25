package com.example.moodyday.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.moodyday.ui.user.LoginScreen
import com.example.moodyday.ui.weather.HomeScreen

@Composable
fun NavHostSetup(
    navController : NavHostController
){
    NavHost(navController = navController,
        startDestination = NavRoutes.Login.route
    ){
        composable(
            route = NavRoutes.Login.route
        ){
            LoginScreen(navController = navController)
        }
        composable(
            route = NavRoutes.Home.route
        ){
            HomeScreen()
        }
    }
}