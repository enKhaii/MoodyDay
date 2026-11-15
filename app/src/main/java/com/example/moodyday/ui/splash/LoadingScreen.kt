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
import com.example.moodyday.data.remote.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull

@Composable
fun LoadingScreen(navController: NavController) {
    LaunchedEffect(Unit) {
        val minSplashTime = 800L
        val startTime = System.currentTimeMillis()

        var navigated = false
        fun navigateTo(route: String) {
            if (navigated) return
            navigated = true
            navController.navigate(route) {
                popUpTo(NavRoutes.Splash.route) { inclusive = true }
            }
        }

        withTimeoutOrNull(3000L) {
            supabase.auth.sessionStatus.collect { status ->
                when (status) {
                    is SessionStatus.Authenticated -> {
                        val elapsed = System.currentTimeMillis() - startTime
                        if (elapsed < minSplashTime) {
                            delay(minSplashTime - elapsed)
                        }
                        navigateTo(NavRoutes.Home.route)
                    }
                    is SessionStatus.NotAuthenticated -> {
                        val elapsed = System.currentTimeMillis() - startTime
                        if (elapsed < minSplashTime) {
                            delay(minSplashTime - elapsed)
                        }
                        navigateTo(NavRoutes.Onboarding.route)
                    }
                    is SessionStatus.RefreshFailure -> {
                        val elapsed = System.currentTimeMillis() - startTime
                        if (elapsed < minSplashTime) {
                            delay(minSplashTime - elapsed)
                        }
                        if (supabase.auth.currentUserOrNull() != null) {
                            navigateTo(NavRoutes.Home.route)
                        } else {
                            navigateTo(NavRoutes.Onboarding.route)
                        }
                    }
                    is SessionStatus.Initializing -> {
                        // Wait for session initialization to complete
                    }
                }
            }
        }

        // Fallback if timeout expires
        if (!navigated) {
            val destination = if (supabase.auth.currentUserOrNull() != null) {
                NavRoutes.Home.route
            } else {
                NavRoutes.Onboarding.route
            }
            navigateTo(destination)
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