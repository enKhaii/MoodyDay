package com.example.moodyday

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.moodyday.navigation.MainScreen
import com.example.moodyday.navigation.NavHostSetup
import com.example.moodyday.ui.theme.MoodyDayTheme

class MainActivity : ComponentActivity() {
    lateinit var navController : NavHostController

    override fun onCreate(savedInstanceState: Bundle?) {
        // System Splash Screen API
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MoodyDayTheme {
                // Navigation Setup(NavController)
                navController = rememberNavController()
                NavHostSetup(navController = navController)

                MainScreen()
            }
        }
    }
}