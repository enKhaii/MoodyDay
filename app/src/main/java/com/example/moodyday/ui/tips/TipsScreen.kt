package com.example.moodyday.ui.tips

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moodyday.ui.weather.SelectedCityViewModel

@Composable
fun TipsScreen(
    viewModel: TipsViewModel = viewModel(),
    selectedCityViewModel: SelectedCityViewModel
) {
    val state by viewModel.uiState.collectAsState()
    val selectedCity by selectedCityViewModel.selectedCity.collectAsState()

    // Trigger loadTipsForCity whenever the tracked city changes
    LaunchedEffect(key1 = selectedCity) {
        viewModel.loadTipsForCity(selectedCity.lat, selectedCity.lon)
    }

    TipsScreenContent(
        state = state,
        onMarkTipDone = { viewModel.markTipAsDone(it) }
    )
}

@Composable
fun TipsScreenContent(
    state: TipsUiState,
    onMarkTipDone: (ClimateTip) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        when {
            state.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFF003D61)
                )
            }
            state.error != null -> {
                Column(
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Error loading tips", fontWeight = FontWeight.Bold, color = Color(0xFF003D61))
                    Text(state.error ?: "Unknown error", textAlign = TextAlign.Center)
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text("Daily Actions", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Weather-linked suggestions to reduce your environmental impact today.", fontSize = 14.sp, color = Color(0xFF475569))
                    }

                    item {
                        WeeklyGoalCard(progress = state.weeklyGoalProgress, total = state.weeklyGoalTotal)
                    }

                    // Outfit Recommendation Card
                    item {
                        OutfitRecommendationCard(
                            title = state.outfitTitle,
                            recommendation = state.outfitRecommendation
                        )
                    }

                    items(state.tips) { tip ->
                        TipCard(
                            tip = tip,
                            onMarkDone = { onMarkTipDone(tip) }
                        )
                    }
                    
                    item { Spacer(modifier = Modifier.height(80.dp).navigationBarsPadding()) }
                }
            }
        }
    }
}

@Composable
fun WeeklyGoalCard(progress: Int, total: Int) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Weekly Goal", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Text(
                "Complete $total actions to maintain your streak.",
                fontSize = 12.sp,
                color = Color.Gray
            )
            
            Spacer(Modifier.height(12.dp))
            
            val progressPercent = (progress.toFloat() / total).coerceIn(0f, 1f)
            Row(verticalAlignment = Alignment.CenterVertically) {
                LinearProgressIndicator(
                    progress = { progressPercent },
                    modifier = Modifier.weight(1f).height(8.dp).background(Color(0xFFE2E8F0), RoundedCornerShape(4.dp)),
                    color = Color(0xFF003D61),
                    strokeCap = StrokeCap.Round
                )
                Spacer(Modifier.width(12.dp))
                Text("${(progressPercent * 100).toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF003D61))
            }
            Text("$progress/$total Actions", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF003D61))
        }
    }
}

@Composable
fun TipCard(tip: ClimateTip, onMarkDone: () -> Unit) {
    var isExpanded by remember { mutableStateOf(false) }
    val arrowRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "ArrowRotation"
    )

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val icon = when (tip.icon) {
                            "sunny" -> Icons.Default.WbSunny
                            "temp" -> Icons.Default.Thermostat
                            "rain" -> Icons.Default.WaterDrop
                            else -> Icons.Default.Eco
                        }
                        Icon(icon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(tip.category, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    }
                }
                if (tip.isCompleted) {
                    Spacer(Modifier.weight(1f))
                    Icon(Icons.Default.CheckCircle, contentDescription = "Done", tint = Color(0xFF003D61), modifier = Modifier.size(20.dp))
                }
            }
            
            Spacer(Modifier.height(12.dp))
            Text(tip.title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
            Spacer(Modifier.height(4.dp))
            Text(tip.description, fontSize = 13.sp, color = Color(0xFF64748B), lineHeight = 18.sp)

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp)
                ) {
                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    if (tip.impact.isNotBlank()) {
                        Text(
                            text = "WHY IT MATTERS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF006494),
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = tip.impact,
                            fontSize = 12.sp,
                            color = Color(0xFF475569),
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    if (tip.steps.isNotEmpty()) {
                        Text(
                            text = "HOW TO DO IT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF006494),
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        tip.steps.forEach { step ->
                            Row(
                                modifier = Modifier.padding(vertical = 2.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text("• ", fontSize = 12.sp, color = Color(0xFF006494), fontWeight = FontWeight.Bold)
                                Text(step, fontSize = 12.sp, color = Color(0xFF475569), lineHeight = 16.sp)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            
            if (!tip.isCompleted) {
                Button(
                    onClick = onMarkDone,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF003D61))
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Mark as Done",
                        color = Color.White
                    )
                }
                
                TextButton(
                    onClick = {
                        isExpanded = !isExpanded
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = if (isExpanded) "Show Less" else "Learn More",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        modifier = Modifier
                            .size(14.dp)
                            .rotate(arrowRotation),
                        tint = Color.Gray)
                }
            } else {
                Text(
                    "Completed earlier today. You helped reduce strain during peak hours.",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
fun OutfitRecommendationCard(title: String, recommendation: String) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F2FE)), // Light blue background
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Checkroom, contentDescription = null, tint = Color(0xFF003D61), modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF003D61))
            }
            Spacer(Modifier.height(8.dp))
            Text(recommendation, fontSize = 14.sp, color = Color(0xFF1E293B))
        }
    }
}