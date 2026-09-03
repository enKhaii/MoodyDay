package com.example.moodyday.ui.tips

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moodyday.ui.theme.MoodyDayTheme

@Composable
fun TipsScreen(
    viewModel: TipsViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
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
            Text("Weekly Goal", fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
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
            Text(tip.title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(tip.description, fontSize = 13.sp, color = Color.DarkGray)
            
            Spacer(Modifier.height(16.dp))
            
            if (!tip.isCompleted) {
                Button(
                    onClick = onMarkDone,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF003D61))
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Mark as Done")
                }
                
                TextButton(onClick = { /* Learn more */ }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Text("Learn More", fontSize = 12.sp, color = Color.Gray)
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Gray)
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

@Preview(showBackground = true)
@Composable
fun TipsScreenPreview() {
    val sampleState = TipsUiState(
        weeklyGoalProgress = 3,
        weeklyGoalTotal = 5,
        outfitTitle = "Cool Breeze Ahead",
        outfitRecommendation = "A light jacket or sweater over your t-shirt would be a good idea.",
        tips = listOf(
            ClimateTip(
                id = "1",
                title = "Skip the dryer today",
                description = "With clear skies and low humidity expected, line-drying clothes is highly efficient today.",
                icon = "sunny",
                category = "HIGH SOLAR POTENTIAL",
                isCompleted = false
            ),
            ClimateTip(
                id = "2",
                title = "Pre-cool your home",
                description = "Temperatures will peak at 32°C by 3 PM. Open windows now while it's cooler.",
                icon = "temp",
                category = "TEMPERATURE ALERT",
                isCompleted = true
            )
        )
    )
    MoodyDayTheme {
        TipsScreenContent(
            state = sampleState,
            onMarkTipDone = {}
        )
    }
}
