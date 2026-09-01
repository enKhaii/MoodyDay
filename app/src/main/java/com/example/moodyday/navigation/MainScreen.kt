package com.example.moodyday.navigation

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.RippleConfiguration
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

@Composable
fun MainScreen() {
    val navController = rememberNavController()

    // Observe current route
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Check if current route matches the BottomNavBar items
    val showBottomBar = BottomNavBar.items.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomBar(navController = navController)
            }
        }
    ) { padding ->
        AppNavGraph(
            navController = navController,
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        )
    }
}

@Composable
fun BottomBar(navController: NavHostController) {
    val screens = listOf(
        BottomNavBar.Home,
        BottomNavBar.Forecast,
        BottomNavBar.Alerts,
        BottomNavBar.Tips,
        BottomNavBar.Goals,
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Define color
    val barBackgroundColor = Color(0xFFf8f9fb)
    val customHighlightColor = Color(0xFFcce6f5)


    // ----- TAB SLIDE ANIMATION -----
    // Find the index of the current selected tab
    val selectedIndex = BottomNavBar.items.indexOfFirst { screen ->
        currentDestination?.hierarchy?.any { it.route == screen.route } == true
    }.coerceAtLeast(0)

    // Smoothly animate target position multiplier (0.0 - 4.0)
    val animatedIndex by animateFloatAsState(
        targetValue = selectedIndex.toFloat(),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "TabSlideAnimation"
    )

    // Root Container Box (holds background, shape, elevation, layout layers)
    Box(
        modifier = Modifier
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(barBackgroundColor)
            .fillMaxWidth()
            .height(64.dp)
    ) {
        // Layer 1: Sliding Highlight Pill
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val totalWidth = maxWidth
            val tabWidth = totalWidth / BottomNavBar.items.size

            Box(
                modifier = Modifier
                    .offset(x = tabWidth * animatedIndex) // Sliding along x-axis
                    .width(tabWidth)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                // Animated sliding pill
                Box(
                    modifier = Modifier
                        .background(
                            color = customHighlightColor,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .fillMaxSize()
                )
            }
        }

        // Layer 2: Interactive NavBar
        NavigationBar(
            containerColor = Color.Transparent,
            windowInsets = WindowInsets(0, 0, 0, 0), // Ensures no extra bottom spacing
            modifier = Modifier.fillMaxSize()
        ) {
            screens.forEach { screen ->
                AddItem(
                    screen = screen,
                    currentDestination = currentDestination,
                    navController = navController
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RowScope.AddItem(
    screen: BottomNavBar,
    currentDestination: NavDestination?,
    navController: NavHostController

) {
    // Define text, icon, highlight color
    val selectedContentColor = Color(0xFF506775)
    val unselectedContentColor = Color(0xFF40474D)
    val customHighlightColor = Color(0xFFcce6f5)

    // Calculate selection state and store in variable
    val isSelected = currentDestination?.hierarchy?.any {
        it.route == screen.route
    } == true

    // Custom interaction source to manage ripples
    val interactionSource = remember { MutableInteractionSource() }

    CompositionLocalProvider(
        LocalRippleConfiguration provides RippleConfiguration(
            color = customHighlightColor,
            rippleAlpha = RippleAlpha(0.2f, 0.2f, 0.1f, 0.2f)
        )
    ) {
        NavigationBarItem(
            selected = isSelected,
            onClick = {
                navController.navigate(screen.route) {
                    popUpTo(navController.graph.findStartDestination().id)
                    launchSingleTop = true
                }
            },
            interactionSource = interactionSource,
            icon = {
                // Box wraps BOTH Icon & Text so the highlight shape covers both
                Box(
                    modifier = Modifier
                        .background(
                            color = if (isSelected) customHighlightColor else Color.Transparent,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = screen.icon,
                            contentDescription = screen.title,
                            tint = if (isSelected) selectedContentColor else unselectedContentColor
                        )
                        Text(
                            text = screen.title,
                            color = if (isSelected) selectedContentColor else unselectedContentColor,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            },
            label = null,
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = Color.Transparent
            )
        )
    }
}