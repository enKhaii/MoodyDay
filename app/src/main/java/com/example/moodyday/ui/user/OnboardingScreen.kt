package com.example.moodyday.ui.user

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.moodyday.R
import com.example.moodyday.navigation.NavRoutes
import kotlinx.coroutines.delay

// Data and Page List
data class OnboardingPage(
    val imageRes: Int,
    val title: String,
    val description: String
)

val onboardingPages = listOf(
    OnboardingPage(
        imageRes = R.drawable.onboard_page1,
        title = "Hyper-Local Weather",
        description = "Get precise, real-time forecasts for your exact location. Stay prepared for whatever the day brings."
    ),
    OnboardingPage(
        imageRes = R.drawable.onboard_page2,
        title = "Smart Climate Insights",
        description = "Compare today's weather with decades of history. Spot unusual trends early and stay safe with timely alerts."
    ),
    OnboardingPage(
        imageRes = R.drawable.onboard_page3,
        title = "Severe Weather Alerts",
        description = "Receive instant notifications about sudden rainstorms, high winds, and severe weather conditions."
    ),
    OnboardingPage(
        imageRes = R.drawable.onboard_page4,
        title = "Climate Goals",
        description = "Turn eco-friendly habits into a streak worth celebrating. Track daily actions and watch your impact grows."
    )
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(navController: NavController) {
// Start somewhere in the middle of a huge count so user can swipe left or right seamlessly
    val initialPage = 1000 * onboardingPages.size
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { 100000 } // Huge virtual page count
    )

    // Smooth continuous forward auto-swipe
    LaunchedEffect(Unit) {
        while (true) {
            delay(3000)
            if (!pagerState.isScrollInProgress) {
                pagerState.animateScrollToPage(
                    page = pagerState.currentPage + 1,
                    animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
                )
            }
        }
    }

// 3. Simple auto-scroll loop
    LaunchedEffect(Unit) {
        while (true) {
            delay(3000)
            if (!pagerState.isScrollInProgress) {
                pagerState.animateScrollToPage(
                    page = pagerState.currentPage + 1,
                    animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
                )
            }
        }
    }

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFDFEDFA),
            Color(0xFFB0D0E6),
            Color(0xFFC6E1F4)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
            .statusBarsPadding()    // Push header up below status bar
            .padding(horizontal = 24.dp)
    ){
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Spacer(modifier = Modifier.height(12.dp))

            // Header Section (Logo + App Title)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_weather_logo),
                    contentDescription = "App Logo",
                    modifier = Modifier.size(96.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "MoodyDay",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF024A70)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Master the forecast. Command the clouds.",
                    fontSize = 15.sp,
                    color = Color(0xFF5A7184),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier =  Modifier.height(28.dp))

            // Middle Card Container
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFBFCFE)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { pageIndex ->
                    val actualIndex = pageIndex % onboardingPages.size
                    val item = onboardingPages[actualIndex]

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Image
                        Image(
                            painter = painterResource(id = item.imageRes),
                            contentDescription = item.title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Title
                        Text(
                            text = item.title,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF181C1D)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Description
                        Text(
                            text = item.description,
                            fontSize = 14.sp,
                            color = Color(0xFF40474D),
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Bottom Action Section (Indicators + Button)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 12.dp)
            ) {
                val currentActualIndex = pagerState.currentPage % onboardingPages.size
                // Page Indicator Dots (Outside Card)
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 20.dp)
                ) {
                    repeat(onboardingPages.size) { index ->
                        val isSelected = currentActualIndex == index
                        Box(
                            modifier = Modifier
                                .width(if (isSelected) 24.dp else 8.dp)
                                .height(8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) Color(0xFF024A70) else Color(
                                        0xFFC0C7D1
                                    )
                                )
                        )
                        if (index < onboardingPages.size - 1) {
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                    }
                }

                // Get Started Button
                Button(
                    onClick = {
                        // Route to Register/Sign Up
                        navController.navigate(NavRoutes.Register.route)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF004B72)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Get Started",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Sign-In Link
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Already have an account?",
                        fontSize = 14.sp,
                        color = Color(0xFF647883)
                    )
                    TextButton(
                        onClick = {
                            // Route to Log in Screen
                            navController.navigate(NavRoutes.Login.route)
                        },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = "Sign in",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2795F5)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}