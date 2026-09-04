package com.example.moodyday.ui.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.moodyday.R
import com.example.moodyday.navigation.NavRoutes
import kotlinx.coroutines.delay

@Composable
fun LoadingScreen(navController: NavController) {
    // Automatically navigate to Onboarding Screen after a short delay (or replace delay with actual initialization logic)
    LaunchedEffect(Unit) {
        delay(1500)
        navController.navigate(NavRoutes.Onboarding.route) {
            // Pop the loading screen off the stack so the user can't press back to return to it
            popUpTo(NavRoutes.Splash.route) { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA)), // Matches your main app background
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Logo Container
            Image(
                painter = painterResource(R.drawable.ic_weather_logo),
                contentDescription = "MoodyDay Logo",
                modifier = Modifier.size(92.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "MoodyDay",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Syncing local weather...",
                fontSize = 15.sp,
                color = Color(0xFF64748B),
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Styled Progress Indicator
            CircularProgressIndicator(
                color = Color(0xFF003D61),
                trackColor = Color(0xFFDDF1F8),
                modifier = Modifier.size(36.dp),
                strokeWidth = 3.dp
            )
        }
    }
}