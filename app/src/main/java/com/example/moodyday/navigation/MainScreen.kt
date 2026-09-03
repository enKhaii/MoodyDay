package com.example.moodyday.navigation

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.RippleConfiguration
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    // Bottom and Top Bar uses the SAME items because it's the same 5 screens
    val showBars = BottomNavBar.items.any { it.route == currentRoute }

    Scaffold(
        topBar = {
            if (showBars) {
                AppTopBar(
                    streakCount = 0,     // CONNECT TO REAL VERSION AFTER IMPLEMENTED!!
                    onSettingsClick = { navController.navigate(NavRoutes.Settings.route) }
                )
            }
        },
        bottomBar = {
            if (showBars) {
                BottomBar(navController = navController)
            }
        }
    ) { padding ->
        AppNavGraph(
            navController = navController,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
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
    val customHighlightColor = Color(0xFFDFF1FB)


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

    // Root Container Surface (holds background, shape, elevation, layout layers)
    Surface(
        color = barBackgroundColor,
        shape = RectangleShape,
        shadowElevation = 10.dp,
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
        ) {
            val tabWidth = maxWidth / screens.size

            // Layer 1: Centered Pill
            Box(
                modifier = Modifier
                    .offset(x = tabWidth * animatedIndex)
                    .width(tabWidth)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(tabWidth - 14.dp) // Proportional inset
                        .height(52.dp)
                        .background(
                            color = customHighlightColor,
                            shape = RoundedCornerShape(16.dp)
                        )
                )
            }

            // Layer 2: Tabs using direct Row to guarantee equal spacing & center alignment
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                screens.forEach { screen ->
                    val isSelected =
                        currentDestination?.hierarchy?.any { it.route == screen.route } == true
                    val selectedContentColor = Color(0xFF003D61)
                    val unselectedContentColor = Color(0xFF64748B)

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                navController.navigateToTab(screen.route)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title,
                                tint = if (isSelected) selectedContentColor else unselectedContentColor,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = screen.title,
                                color = if (isSelected) selectedContentColor else unselectedContentColor,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
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
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            interactionSource = interactionSource,
            icon = {
                // Keep icon and text centered, LAYER 1 provides the background pill
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = screen.icon,
                        contentDescription = screen.title,
                        tint = if (isSelected) selectedContentColor else unselectedContentColor,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = screen.title,
                        fontSize = 11.sp,
                        color = if (isSelected) selectedContentColor else unselectedContentColor,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            },
            label = null,
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = Color.Transparent
            )
        )
    }
}