package com.example.moodyday.ui.forecast

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.moodyday.ui.weather.SelectedCityViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moodyday.data.remote.WeatherCodeTranslator
import kotlin.math.roundToInt

private val BarBlue = Color(0xFF1565C0)
private val BarGray = Color(0xFFB0BEC5)
private val BarRed = Color(0xFFE57373)
private val WarningBg = Color(0xFFFDEAEA)
private val WarningText = Color(0xFFC62828)

@Composable
fun ForecastScreen(
    viewModel: ForecastViewModel,
    selectedCityViewModel: SelectedCityViewModel
) {
    val selectedCity by selectedCityViewModel.selectedCity.collectAsState()

    LaunchedEffect(selectedCity) {
        viewModel.loadCity(selectedCity.lat, selectedCity.lon, selectedCity.name)
    }

    val homeState by viewModel.homeState.collectAsState()
    val notes by viewModel.notes.collectAsState()
    var noteDialogDay by remember { mutableStateOf<DailyOutlook?>(null) }
    val historicalState by viewModel.historicalState.collectAsState()
    val isScreenLoading = homeState.isLoading || (historicalState.isLoading && historicalState.bars.isEmpty())

    when {
        isScreenLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF003D61))
            }
        }

        homeState.error != null -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Couldn't load weather: ${homeState.error}")
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.refresh() }
                    ) {
                        Text(text = "Reload")
                    }
                }
            }
        }

        else -> {
            noteDialogDay?.let { day ->
                NoteOverlay(
                    day = day,
                    existingNote = notes[day.date],
                    onDismiss = { noteDialogDay = null },
                    onSave = { text ->
                        viewModel.saveNote(day.date, text)
                        noteDialogDay = null
                    },
                    onDelete = {
                        viewModel.deleteNote(day.date)
                        noteDialogDay = null
                    }
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        // City & Overview Header
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = homeState.cityLabel,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "${homeState.conditionLabel} • ${homeState.nowTempF}° • feels ${homeState.apparentTempF}°",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Now & Hourly Tiles
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                NowTile(
                    homeState.nowTempF,
                    homeState.nowWeatherCode,
                    modifier = Modifier.weight(1.3f)
                )
                homeState.hourly.take(3).forEach { hp ->
                    HourTile(hp, modifier = Modifier.weight(1f))
                }
            }
        }

        item {
            Text(
                text = "7-Day Outlook",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        val overallMin = homeState.daily.minOfOrNull { it.tempMin } ?: 0
        val overallMax = homeState.daily.maxOfOrNull { it.tempMax } ?: 1 // Fixed tempMax lookup

        items(homeState.daily) { day ->
            DailyRow(
                day = day,
                overallMin = overallMin,
                overallMax = overallMax,
                hasNote = notes.containsKey(day.date),
                onIconClick = { noteDialogDay = day }
            )
        }

        // Historical Bar Chart Header & Legends
        item { Spacer(Modifier.height(8.dp)) }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Temp vs Historical\nAverage",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        LegendDot(color = BarBlue, label = "This Week")
                        LegendDot(color = BarGray, label = "30-Yr Norm")
                    }
                }
                Text(
                    text = "Comparing this week to the 30-year climate norm.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            if (historicalState.isLoading && historicalState.bars.isEmpty()) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (historicalState.error != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Couldn't load history: ${historicalState.error}")
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { viewModel.refresh() }) { Text("Reload") }
                }
            } else {
                Card(shape = RoundedCornerShape(16.dp)) {
                    BarChart(
                        bars = historicalState.bars,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .padding(16.dp)
                    )
                }
            }
        }

        if (historicalState.heatWarning) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = WarningBg),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = WarningText)
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "Temperatures this weekend are projected to be significantly above the 30-year average. Hydration and shade recommended.",
                            color = WarningText,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
private fun NowTile(temp: Int, weatherCode: Int, modifier: Modifier = Modifier) {
    ElevatedCard(
        modifier = modifier.height(110.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Now",
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.labelMedium
            )
            Text(
                text = WeatherCodeTranslator.toEmoji(weatherCode),
                fontSize = 22.sp
            )
            Text(
                text = "$temp°",
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun HourTile(point: HourlyPoint, modifier: Modifier = Modifier) {
    ElevatedCard(
        modifier = modifier.height(110.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = point.label,
                fontSize = 12.sp
            )

            Text(
                text = WeatherCodeTranslator.toEmoji(point.weatherCode),
                fontSize = 20.sp
            )

            Text(
                text = "${point.tempF}°",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun MinMaxBar(
    min: Int,
    max: Int,
    rangeMin: Int,
    rangeMax: Int,
    modifier: Modifier = Modifier
) {
    val span = (rangeMax - rangeMin).coerceAtLeast(1)
    val startFrac = ((min - rangeMin).toFloat() / span).coerceIn(0f, 1f)
    val endFrac = ((max - rangeMin).toFloat() / span).coerceIn(0f, 1f)

    Canvas(modifier = modifier) {
        val trackY = size.height / 2
        drawLine(
            color = Color.LightGray.copy(alpha = 0.35f),
            start = Offset(0f, trackY),
            end = Offset(size.width, trackY),
            strokeWidth = size.height,
            cap = StrokeCap.Round
        )

        drawLine(
            brush = Brush.horizontalGradient(listOf(Color(0xFF64B5F6), Color(0xFF1565C0))),
            start = Offset(size.width * startFrac, trackY),
            end = Offset(size.width * endFrac, trackY),
            strokeWidth = size.height,
            cap = StrokeCap.Round
        )
    }
}

@Composable
private fun DailyRow(
    day: DailyOutlook,
    overallMin: Int,
    overallMax: Int,
    hasNote: Boolean,
    onIconClick: () -> Unit
) {
    ElevatedCard(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Day Label
            Text(
                text = day.label,
                modifier = Modifier.width(48.dp),
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )

            // Weather Emoji
            Text(
                text = WeatherCodeTranslator.toEmoji(day.weatherCode),
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(30.dp)
            )

            // UV Index
            Text(
                text = "UV ${day.uvIndexMax.roundToInt()}",
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.width(42.dp)
            )

            Spacer(Modifier.width(6.dp))

            // Min Temp (aligned right toward the bar)
            Text(
                text = "${day.tempMin}°",
                fontSize = 13.sp,
                textAlign = TextAlign.End,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(30.dp)
            )

            Spacer(Modifier.width(6.dp))

            // Min-Max Range Bar
            MinMaxBar(
                min = day.tempMin,
                max = day.tempMax,
                rangeMin = overallMin,
                rangeMax = overallMax,
                modifier = Modifier
                    .weight(1f)
                    .height(6.dp)
            )

            Spacer(Modifier.width(6.dp))

            // Max Temp (aligned left away from the bar)
            Text(
                text = "${day.tempMax}°",
                fontSize = 13.sp,
                textAlign = TextAlign.Start,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.width(30.dp)
            )

            Spacer(Modifier.width(8.dp))

            // Action / Note Button
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(
                        color = if (hasNote) Color(0xFFE3F2FD) else Color.Transparent,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable(onClick = onIconClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (hasNote) Icons.Default.EditNote else Icons.Default.Add,
                    contentDescription = if (hasNote) "Edit note" else "Add note",
                    tint = if (hasNote) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(8.dp)
                .background(color, shape = CircleShape)
        )
        Spacer(Modifier.width(6.dp))
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun BarChart(bars: List<WeeklyBar>, modifier: Modifier = Modifier) {
    if (bars.isEmpty()) return

    val maxTemp = bars.maxOf { maxOf(it.thisWeekTemp, it.historicalAvgTemp) }

    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            val barGroupWidth = size.width / bars.size
            val barWidth = barGroupWidth * 0.32f
            val intraBarGap = barGroupWidth * 0.06f
            val totalBarsWidth = (barWidth * 2) + intraBarGap
            // Centers the pair of bars within each day's column slot
            val groupPadding = (barGroupWidth - totalBarsWidth) / 2f

            bars.forEachIndexed { index, bar ->
                val groupX = barGroupWidth * index
                val thisWeekColor = if (bar.isHighlighted) BarRed else BarBlue

                val thisWeekHeight = (bar.thisWeekTemp / maxTemp).toFloat() * size.height
                val histHeight = (bar.historicalAvgTemp / maxTemp).toFloat() * size.height

                // This Week bar
                drawRoundRect(
                    color = thisWeekColor,
                    topLeft = Offset(groupX + groupPadding, size.height - thisWeekHeight),
                    size = Size(barWidth, thisWeekHeight),
                    cornerRadius = CornerRadius(4f, 4f)
                )

                // Historical average bar
                drawRoundRect(
                    color = BarGray,
                    topLeft = Offset(groupX + groupPadding + barWidth + intraBarGap, size.height - histHeight),
                    size = Size(barWidth, histHeight),
                    cornerRadius = CornerRadius(4f, 4f)
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(Modifier.fillMaxWidth()) {
            bars.forEach { bar ->
                Text(
                    text = bar.dayLabel,
                    modifier = Modifier.weight(1f),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}