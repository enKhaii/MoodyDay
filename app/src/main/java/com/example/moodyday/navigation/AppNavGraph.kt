package com.example.moodyday.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.moodyday.MoodyDayApplication
import com.example.moodyday.data.auth.SessionManager
import com.example.moodyday.ui.alerts.AlertsScreen
import com.example.moodyday.ui.alerts.AlertsViewModel
import com.example.moodyday.ui.auth.LoginScreen
import com.example.moodyday.ui.auth.RegisterScreen
import com.example.moodyday.ui.forecast.ForecastScreen
import com.example.moodyday.ui.forecast.ForecastViewModel
import com.example.moodyday.ui.goals.GoalsScreen
import com.example.moodyday.ui.goals.GoalsViewModel
import com.example.moodyday.ui.settings.SettingsScreen
import com.example.moodyday.ui.splash.LoadingScreen
import com.example.moodyday.ui.tips.TipsScreen
import com.example.moodyday.ui.user.OnboardingScreen
import com.example.moodyday.ui.user.ProfileScreen
import com.example.moodyday.ui.weather.CitySearchScreen
import com.example.moodyday.ui.weather.CitySearchViewModel
import com.example.moodyday.ui.weather.HomeScreen
import com.example.moodyday.ui.weather.SelectedCityViewModel

private val routesWithBottomBar = BottomNavBar.items.map { it.route }.toSet()
private val routesWithTopBar = BottomNavBar.items.map { it.route }.toSet()

@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val appContainer = (context.applicationContext as MoodyDayApplication).container

    val selectedCityViewModel: SelectedCityViewModel = viewModel()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val startDestination = if (SessionManager.currentUserId != null) {
        NavRoutes.Home.route
    } else {
        NavRoutes.Splash.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(route = NavRoutes.Splash.route) {
            LoadingScreen(navController = navController)
        }
        composable(route = NavRoutes.Onboarding.route) {
            OnboardingScreen(navController = navController)
        }
        composable(route = NavRoutes.Login.route) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(NavRoutes.Register.route)
                },
                onNavigateToHome = {
                    navController.navigate(NavRoutes.Home.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable(route = NavRoutes.Register.route) {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onRegisterSuccess = {
                    navController.navigate(NavRoutes.Home.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable(route = NavRoutes.Home.route) {
            HomeScreen(
                selectedCityViewModel = selectedCityViewModel,
                onSearchClick = { navController.navigate(NavRoutes.CitySearch.route) },
                onForecastClick = { navController.navigateToTab(NavRoutes.Forecast.route) },
                onAlertsClick = { navController.navigateToTab(NavRoutes.Alerts.route) },
                onTipsClick = { navController.navigateToTab(NavRoutes.Tips.route) }
            )
        }
        composable(route = NavRoutes.CitySearch.route) {
            val activeUserId = SessionManager.getActiveUserId()
            CitySearchScreen(
                viewModel = viewModel(key = activeUserId) {
                    CitySearchViewModel(
                        repository = appContainer.savedCityRepository,
                        userId = activeUserId
                    )
                },
                selectedCityViewModel = selectedCityViewModel,
                onCitySelected = { navController.popBackStack() },
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(route = NavRoutes.Forecast.route) {
            val forecastViewModel: ForecastViewModel = viewModel()
            ForecastScreen(
                viewModel = forecastViewModel,
                selectedCityViewModel = selectedCityViewModel
            )
        }
        composable(route = NavRoutes.Alerts.route) { backStackEntry ->
            val alertsViewModel: AlertsViewModel = viewModel(backStackEntry)
            AlertsScreen(
                viewModel = alertsViewModel,
                selectedCityViewModel = selectedCityViewModel,
                onNavigateToTips = { navController.navigateToTab(NavRoutes.Tips.route) }
            )
        }
        composable(route = NavRoutes.Tips.route) {
            TipsScreen(
                selectedCityViewModel = selectedCityViewModel
            )
        }
        composable(route = NavRoutes.Goals.route) {
            val activeUserId = SessionManager.getActiveUserId()
            val goalsViewModel: GoalsViewModel = viewModel(key = activeUserId) {
                GoalsViewModel(
                    goalDao = appContainer.goalDao,
                    userStreakDao = appContainer.userStreakDao,
                    userStreakRepository = appContainer.userStreakRepository,
                    userId = activeUserId,
                    selectedCityViewModel = selectedCityViewModel
                )
            }
            GoalsScreen(
                viewModel = goalsViewModel
            )
        }
        composable(route = NavRoutes.Settings.route) {
            SettingsScreen(
                onBackClick = { navController.popBackStack() },
                onCitySearchClick = {
                    // Replace with your actual search/city route
                    navController.navigate(NavRoutes.CitySearch.route)
                },
                onLogoutClick = {
                    navController.navigate(NavRoutes.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable(route = "profile_route") {
            ProfileScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}