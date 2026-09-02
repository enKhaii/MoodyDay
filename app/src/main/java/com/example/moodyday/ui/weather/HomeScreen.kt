package com.example.moodyday.ui.weather

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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController

@Composable
fun HomeScreen(viewModel: HomeViewModel = viewModel()) {
    val navController = rememberNavController()
    val state by viewModel.uiState.collectAsState()

    // Background color gradient
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFDFF1DB),
            Color(0xFF1F88FB)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 48.dp, bottom = 24.dp)   // Top padding pushes content down, bottom padding clears scroll space
        ) {
            when {
                state.isLoading -> {
                    Spacer(modifier = Modifier.height(80.dp))
                    CircularProgressIndicator(color = Color(0xFF003D61))
                }

                state.error != null -> {
                    Spacer(modifier = Modifier.height(80.dp))
                    Text(text = "Unable to Load Weather", fontWeight = FontWeight.Bold, color = Color(0xFF003D61))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(state.error ?: "", fontSize = 13.sp, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            viewModel.loadWeather(3.140853, 101.693207, "Kuala Lumpur")
                        }
                    ) {
                        Text(text = "Retry")
                    }
                }

                else -> {
                    // Header: City Name, Icon, Temperature, Feels Like
                    Text(
                        text = state.cityName,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.WbSunny,
                            contentDescription = state.condition,
                            modifier = Modifier.size(72.dp),
                            tint = Color(0xFF003D61)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "${state.temperature?.toInt()}°",
                            fontSize = 80.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF003D61),
                            letterSpacing = (-2).sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Feels like ${state.feelsLike?.toInt()}°C",
                        fontSize = 16.sp,
                        color = Color(0xFF475569),
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Status Row: Humidity, Wind, UV Index
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            label = "Humidity",
                            value = "${state.humidity?.toInt()}%",
                            icon = Icons.Default.WaterDrop,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            label = "Wind",
                            value = "${state.windSpeed?.toInt()} kmh",
                            icon = Icons.Default.Air,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            label = "UV Index",
                            value = "${state.uvIndex?.toString()}",
                            icon = Icons.Default.WbSunny,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Climate Insight Card
                    ClimateInsightCard()

                    Spacer(modifier = Modifier.height(24.dp))

                    // Action Buttons
                    ActionButton(
                        title = "Forecast",
                        icon = Icons.Default.DateRange,
                        backgroundColor = Color(0xFF003D61),
                        contentColor = Color.White
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    ActionButton(
                        title = "Alerts",
                        icon = Icons.Outlined.Notifications,
                        backgroundColor = Color.White,
                        contentColor = Color(0xFF1E293B),
                        iconTint = Color(0xFFD32F2F)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    ActionButton(
                        title = "Tips",
                        icon = Icons.Default.Eco,
                        backgroundColor = Color.White,
                        contentColor = Color(0xFF1E293B),
                        iconTint = Color(0xFF003D61)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier.height(110.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color(0xFF64748B),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color(0xFF64748B),
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                color = Color(0xFF1E293B),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ClimateInsightCard() {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Icon(
                imageVector = Icons.Default.TrendingUp,
                contentDescription = null,
                tint = Color(0xFFF1F5F9),
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 20.dp, y = (-10).dp)
            )

            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.WarningAmber,
                        contentDescription = "Warning",
                        tint = Color(0xFFD32F2F),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Climate Insight",
                        fontSize = 20.sp,
                        color = Color(0xFF1E293B),
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "3°C warmer than historical average",
                    fontSize = 15.sp,
                    color = Color(0xFFD32F2F),
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PillTag(
                        text = "Historical Data",
                        backgroundColor = Color(0xFFE2E8F0),
                        textColor = Color(0xFF475569)
                    )
                    PillTag(
                        text = "Anomaly Detected",
                        backgroundColor = Color(0xFFFFEBEE),
                        textColor = Color(0xFFD32F2F)
                    )
                }
            }
        }
    }
}

@Composable
private fun PillTag(
    text: String,
    backgroundColor: Color,
    textColor: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            color = textColor,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun ActionButton(
    title: String,
    icon: ImageVector,
    backgroundColor: Color,
    contentColor: Color,
    iconTint: Color = contentColor
) {
    Button(
        onClick = { /* Connect to navController */ },
        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(24.dp),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                fontSize = 16.sp,
                color = contentColor,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = contentColor
            )
        }
    }
}