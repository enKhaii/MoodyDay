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
import com.example.moodyday.ui.alerts.AlertsScreen
import com.example.moodyday.ui.auth.LoginScreen
import com.example.moodyday.ui.auth.RegisterScreen
import com.example.moodyday.ui.forecast.ForecastScreen
import com.example.moodyday.ui.forecast.ForecastViewModel
import com.example.moodyday.ui.goals.GoalsScreen
import com.example.moodyday.ui.settings.SettingsScreen
import com.example.moodyday.ui.splash.AnimatedSplashScreen
import com.example.moodyday.ui.tips.TipsScreen
import com.example.moodyday.ui.user.OnboardingScreen
import com.example.moodyday.ui.weather.CitySearchScreen
import com.example.moodyday.ui.weather.CitySearchViewModel
import com.example.moodyday.ui.weather.HomeScreen
import com.example.moodyday.ui.weather.SelectedCityViewModel

// Retrieved from BottomNavBar.items
// The routes string of screen that CONTAINS bottom navigation bar
// routesWithTopBar use BottomNavBar.items because it's the SAME 5 SCREENS
// Separating name just make it easier to understand
private val routesWithBottomBar = BottomNavBar.items.map { it.route }.toSet()
private val routesWithTopBar = BottomNavBar.items.map { it.route }.toSet()

@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val appContainer = (context.applicationContext as MoodyDayApplication).container
    val testUserId = "chongwc"      // REPLACE WITH REAL AUTH UID ONCE LOGIN IS COMPLETED!!

    val selectedCityViewModel: SelectedCityViewModel = viewModel()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route


    NavHost(
        navController = navController,
        startDestination = NavRoutes.Home.route,
        modifier = modifier
    ) {
        composable(route = NavRoutes.Splash.route) {
            AnimatedSplashScreen(navController)
        }
        composable(route = NavRoutes.Onboarding.route) {
            OnboardingScreen(navController = navController)
        }
        composable(route = NavRoutes.Login.route) {
            LoginScreen(navController = navController)
        }
        composable(route = NavRoutes.Register.route) {
            RegisterScreen(
                onNavigateToLogin = { navController.navigate(NavRoutes.Login.route) },
                onRegisterSuccess = { navController.navigate(NavRoutes.Home.route) }
            )
        }
        composable(route = NavRoutes.Home.route) {
            HomeScreen(
                selectedCityViewModel = selectedCityViewModel,
                // City Search is not MAIN tab so no navigateToTab
                onSearchClick = { navController.navigate(NavRoutes.CitySearch.route) },
                onForecastClick = { navController.navigateToTab(NavRoutes.Forecast.route) },
                onAlertsClick = { navController.navigateToTab(NavRoutes.Alerts.route) },
                onTipsClick = { navController.navigateToTab(NavRoutes.Tips.route) }
            )
        }
        composable(route = NavRoutes.CitySearch.route) {
            CitySearchScreen(
                viewModel = viewModel {
                    CitySearchViewModel(
                        repository = appContainer.savedCityRepository,
                        userId = testUserId
                    )
                },
                selectedCityViewModel = selectedCityViewModel,
                onCitySelected = { navController.popBackStack() },
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(route = NavRoutes.Forecast.route) {
            val forecastViewModel: ForecastViewModel = viewModel()
            ForecastScreen(viewModel = forecastViewModel)
        }
        composable(route = NavRoutes.Alerts.route) {
            AlertsScreen()
        }
        composable(route = NavRoutes.Tips.route) {
            TipsScreen()
        }
        composable(route = NavRoutes.Goals.route) {
            GoalsScreen()
        }
        composable(route = NavRoutes.Settings.route) {
            SettingsScreen()
        }
    }
}