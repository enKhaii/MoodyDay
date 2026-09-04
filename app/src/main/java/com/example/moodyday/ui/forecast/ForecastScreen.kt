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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.moodyday.ui.weather.SelectedCityViewModel
import kotlin.math.roundToInt

private val BarGray = Color(0xFFB0BEC5)
private val WarningText = Color(0xFFC62828)

@Composable
fun ForecastScreen(
    viewModel: ForecastViewModel,
    selectedCityViewModel: SelectedCityViewModel
) {
    val selectedCity by selectedCityViewModel.selectedCity.collectAsState()

    LaunchedEffect(selectedCity) {
        viewModel.loadCity(selectedCity.lat, selectedCity.lon, selectedCity.name, selectedCity.countryCode)
    }

    val homeState by viewModel.homeState.collectAsState()
    val notes by viewModel.notes.collectAsState()
    var noteDialogDay by remember { mutableStateOf<DailyOutlook?>(null) }
    val historicalState by viewModel.historicalState.collectAsState()
    val isScreenLoading =
        homeState.isLoading || (historicalState.isLoading && historicalState.bars.isEmpty())

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFEAF5FA),
            Color(0xFFF4F9FB)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
    ) {
        when {
            isScreenLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF005B82))
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
                            Text(text = "Retry")
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
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(24.dp)) }

                    // City & Overview Header
                    item {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (!homeState.countryCode.isNullOrEmpty()) {
                                    "${homeState.cityName}, ${homeState.countryCode!!.uppercase()}"
                                } else {
                                    homeState.cityName
                                },
                                fontSize = 34.sp,
                                color = Color(0xFF1E293B),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${homeState.conditionLabel} • ${homeState.nowTempF}°",
                                fontSize = 15.sp,
                                color = Color(0xFF64748B),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Now & Hourly Tiles
                    item {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
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

                    // 7-Day Outlook Header
                    item {
                        Text(
                            text = "7-Day Outlook",
                            fontSize = 20.sp,
                            color = Color(0xFF1E293B),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    val overallMin = homeState.daily.minOfOrNull { it.tempMin } ?: 0
                    val overallMax =
                        homeState.daily.maxOfOrNull { it.tempMax } ?: 1 // Fixed tempMax lookup

                    item {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                homeState.daily.forEachIndexed { index, day ->
                                    DailyRow(
                                        day = day,
                                        overallMin = overallMin,
                                        overallMax = overallMax,
                                        hasNote = notes.containsKey(day.date),
                                        onIconClick = { noteDialogDay = day }
                                    )
                                    if (index < homeState.daily.lastIndex) {
                                        HorizontalDivider(
                                            color = Color(0xFFF1F5F9),
                                            thickness = 1.dp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Historical Bar Chart Header & Legends
                    item {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Temp vs\nHistorical\nAverage",
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1E293B),
                                            lineHeight = 26.sp
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            text = "Comparing this week to the 30-year climate norm.",
                                            fontSize = 12.sp,
                                            color = Color(0xFF64748B),
                                            lineHeight = 16.sp
                                        )
                                    }

                                    // Legend
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        horizontalAlignment = Alignment.Start,
                                        modifier = Modifier.padding(top = 4.dp)
                                    ) {
                                        LegendDot(color = Color(0xFF005B82), label = "This\nWeek")
                                        LegendDot(color = Color(0xFFD1D5DB), label = "30-Yr\nAvg")
                                    }
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                // Chart Content
                                if (historicalState.isLoading && historicalState.bars.isEmpty()) {
                                    Box(
                                        Modifier
                                            .fillMaxWidth()
                                            .height(240.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(color = Color(0xFF005B82))
                                    }
                                } else if (historicalState.error != null) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(240.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = "Couldn't load history: ${historicalState.error}",
                                            color = Color(0xFF1E293B)
                                        )
                                        Button(
                                            onClick = { viewModel.refresh() }
                                        ) {
                                            Text(
                                                text = "Reload"
                                            )
                                        }
                                    }
                                } else {
                                    BarChart(
                                        bars = historicalState.bars,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(260.dp)
                                    )
                                }
                            }
                        }
                    }

                    if (historicalState.heatWarning) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFE4E6)),
                                shape = RoundedCornerShape(12.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.WarningAmber,
                                        contentDescription = "Warning",
                                        tint = WarningText,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Text(
                                        text = "Temperatures this weekend are projected to be significantly above the 30-year average. Hydration and shade recommended.",
                                        color = WarningText,
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }
                    }
                    item { Spacer(Modifier.height(32.dp)) }
                }
            }
        }
    }
}

@Composable
private fun NowTile(temp: Int, weatherCode: Int, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(130.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF005B82)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Now",
                color = Color.White,
                fontSize = 13.sp
            )
            Text(
                text = WeatherCodeTranslator.toEmoji(weatherCode),
                fontSize = 28.sp
            )
            Text(
                text = "$temp°",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun HourTile(point: HourlyPoint, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(130.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = point.label,
                fontSize = 13.sp,
                color = Color(0xFF64748B)
            )

            Text(
                text = WeatherCodeTranslator.toEmoji(point.weatherCode),
                fontSize = 28.sp
            )

            Text(
                text = "${point.tempF}°",
                fontSize = 20.sp,
                color = Color(0xFF1E293B),
                fontWeight = FontWeight.Bold
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
            color = Color(0xFFF1F5F9),
            start = Offset(0f, trackY),
            end = Offset(size.width, trackY),
            strokeWidth = size.height,
            cap = StrokeCap.Round
        )

        drawLine(
            color = Color(0xFF005B82),
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Day Label
        Text(
            text = day.label,
            modifier = Modifier.width(48.dp),
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1E293B),
            fontSize = 14.sp
        )

        // Weather Emoji
        Text(
            text = WeatherCodeTranslator.toEmoji(day.weatherCode),
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(28.dp)
        )

        // UV Index
        Text(
            text = "UV ${day.uvIndexMax.roundToInt() * 10}%",
            fontSize = 12.sp,
            color = Color(0xFF64748B),
            modifier = Modifier.width(36.dp)
        )

        Spacer(Modifier.width(8.dp))

        // Min Temp (aligned right toward the bar)
        Text(
            text = "${day.tempMin}°",
            fontSize = 14.sp,
            textAlign = TextAlign.End,
            color = Color(0xFF64748B),
            modifier = Modifier.width(32.dp)
        )

        Spacer(Modifier.width(12.dp))

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

        Spacer(Modifier.width(12.dp))

        // Max Temp (aligned left away from the bar)
        Text(
            text = "${day.tempMax}°",
            fontSize = 14.sp,
            textAlign = TextAlign.Start,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E293B),
            modifier = Modifier.width(32.dp)
        )

        // Action / Note Button
        Box(
            modifier = Modifier
                .size(24.dp)
                .clickable(onClick = onIconClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (hasNote) Icons.Default.EditNote else Icons.Default.Add,
                contentDescription = if (hasNote) "Edit note" else "Add note",
                tint = if (hasNote) Color(0xFF005B82) else Color(0xFF64748B),
                modifier = Modifier.size(18.dp)
            )
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
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color(0xFF64748B),
            lineHeight = 14.sp
        )
    }
}

@Composable
private fun BarChart(bars: List<WeeklyBar>, modifier: Modifier = Modifier) {
    if (bars.isEmpty()) return

    val dataMax = bars.maxOf { maxOf(it.thisWeekTemp.toFloat(), it.historicalAvgTemp.toFloat()) }
    val dataMin = bars.minOf { minOf(it.thisWeekTemp.toFloat(), it.historicalAvgTemp.toFloat()) }

    val chartMax = dataMax + 5f
    val chartMin = (dataMin - 5f).coerceAtLeast(0f)
    val effectiveRange = chartMax - chartMin

    val step = effectiveRange / 3
    val yAxisLabels = listOf(
        "${chartMax.roundToInt()}°",
        "${(chartMax - step).roundToInt()}°",
        "${(chartMax - step * 2).roundToInt()}°",
        "${chartMin.roundToInt()}°"
    )

    Box(modifier = modifier) {
        // 1. Grid Lines and Y-Axis Labels
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 24.dp), // Leaves room for X-axis labels below
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            yAxisLabels.forEach { label ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        color = Color(0xFF9CA3AF),
                        modifier = Modifier.width(32.dp)
                    )
                    HorizontalDivider(
                        color = Color(0xFFE5E7EB),
                        thickness = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            // Bottom baseline for the grid
            Spacer(modifier = Modifier.height(0.dp))
        }

        Column(modifier = Modifier.fillMaxSize()) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(start = 36.dp, top = 8.dp, bottom = 8.dp)
            ) {
                val barGroupWidth = size.width / bars.size

                // Use dp.toPx() so the bars are cleanly sized on all screen densities
                val barWidth = 10.dp.toPx()
                val intraBarGap = 4.dp.toPx()

                val totalBarsWidth = (barWidth * 2) + intraBarGap
                // Centers the pair of bars within each day's column slot
                val groupPadding = (barGroupWidth - totalBarsWidth) / 2f

                bars.forEachIndexed { index, bar ->
                    val groupX = barGroupWidth * index
                    val thisWeekColor =
                        if (bar.isHighlighted) Color(0xFFD9534F) else Color(0xFF005B82)

                    // Calculate heights strictly based on the dynamic range
                    val thisWeekHeight = ((bar.thisWeekTemp.toFloat() - chartMin) / effectiveRange).coerceIn(0f, 1f) * size.height
                    val histHeight = ((bar.historicalAvgTemp.toFloat() - chartMin) / effectiveRange).coerceIn(0f, 1f) * size.height

                    // This Week bar
                    drawRoundRect(
                        color = thisWeekColor,
                        topLeft = Offset(groupX + groupPadding, size.height - thisWeekHeight),
                        size = Size(barWidth, thisWeekHeight),
                        cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
                    )

                    // Historical average bar
                    drawRoundRect(
                        color = BarGray,
                        topLeft = Offset(
                            groupX + groupPadding + barWidth + intraBarGap,
                            size.height - histHeight
                        ),
                        size = Size(barWidth, histHeight),
                        cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
                    )
                }
            }

            // 3. X-Axis Labels
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 36.dp)
            ) {
                bars.forEach { bar ->
                    Text(
                        text = bar.dayLabel,
                        modifier = Modifier.weight(1f),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}