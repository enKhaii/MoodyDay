package com.example.moodyday.ui.forecast

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.WarningAmber
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
import java.time.LocalTime
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
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(24.dp)) }

                    // City & Overview Header
                    item {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
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

                    // Now & Hourly Dynamic Scrollable Carousel
                    item {
                        val currentHour = remember { LocalTime.now().hour }
                        val isNowNight = currentHour >= 19 || currentHour < 7

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item {
                                NowTile(
                                    temp = homeState.nowTempF,
                                    weatherCode = homeState.nowWeatherCode,
                                    isNight = isNowNight,
                                    modifier = Modifier.width(90.dp)
                                )
                            }

                            items(
                                items = homeState.hourly,
                                key = { it.label }
                            ) { hp ->
                                HourTile(
                                    point = hp,
                                    modifier = Modifier.width(84.dp)
                                )
                            }
                        }
                    }

                    // 7-Day Outlook Header
                    item {
                        Text(
                            text = "7-Day Outlook",
                            fontSize = 20.sp,
                            color = Color(0xFF1E293B),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }

                    val overallMin = homeState.daily.minOfOrNull { it.tempMin } ?: 0
                    val overallMax = homeState.daily.maxOfOrNull { it.tempMax } ?: 1

                    item {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
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
                                            text = "Comparing this week to the 10-year climate norm.",
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
                                        LegendDot(color = Color(0xFFD1D5DB), label = "10-Yr\nAvg")
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
                                            Text(text = "Reload")
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

                    val (iconType, insightColor) = when (historicalState.insightType) {
                        InsightType.WARMER -> Icons.Default.WarningAmber to Color(0xFFD32F2F)
                        InsightType.COOLER -> Icons.Default.AcUnit to Color(0xFF0284C7)
                        InsightType.NEUTRAL -> Icons.Default.CheckCircle to Color(0xFF16A34A)
                    }

                    val cardBg = when (historicalState.insightType) {
                        InsightType.WARMER -> Color(0xFFFFE4E6)
                        InsightType.COOLER -> Color(0xFFE0F2FE)
                        InsightType.NEUTRAL -> Color(0xFFDCFCE7)
                    }

                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = cardBg),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                            modifier = Modifier.padding(horizontal = 20.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = iconType,
                                    contentDescription = "Insight",
                                    tint = insightColor,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    text = historicalState.insightMessage,
                                    color = insightColor,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                )
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
private fun NowTile(
    temp: Int,
    weatherCode: Int,
    modifier: Modifier = Modifier,
    isNight: Boolean = (LocalTime.now().hour >= 19 || LocalTime.now().hour < 7)
) {
    Card(
        modifier = modifier.height(130.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF005B82)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Now",
                color = Color.White,
                fontSize = 13.sp
            )
            Text(
                text = WeatherCodeTranslator.toEmoji(weatherCode, isNight = isNight),
                fontSize = 28.sp
            )
            Text(
                text = "$temp°",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun isNightTime(label: String): Boolean {
    val trimmed = label.trim()
    val parts = trimmed.split(" ")
    if (parts.size == 2) {
        val hour = parts[0].toIntOrNull() ?: return false
        val period = parts[1].uppercase()
        val hour24 = when {
            period == "AM" && hour == 12 -> 0
            period == "AM" -> hour
            period == "PM" && hour == 12 -> 12
            period == "PM" -> hour + 12
            else -> hour
        }
        return hour24 >= 19 || hour24 < 7
    }
    return false
}

@Composable
private fun HourTile(point: HourlyPoint, modifier: Modifier = Modifier) {
    val isNight = point.isNight || isNightTime(point.label)
    Card(
        modifier = modifier.height(130.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = point.label,
                fontSize = 13.sp,
                color = Color(0xFF64748B),
                maxLines = 1
            )
            Text(
                text = WeatherCodeTranslator.toEmoji(point.weatherCode, isNight = isNight),
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
        Text(
            text = day.label,
            modifier = Modifier.width(48.dp),
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1E293B),
            fontSize = 14.sp
        )

        Text(
            text = WeatherCodeTranslator.toEmoji(day.weatherCode),
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(28.dp)
        )

        // Rain Percentage
        Text(
            text = "${day.rainChancePercent}%",
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

        Text(
            text = "${day.tempMax}°",
            fontSize = 14.sp,
            textAlign = TextAlign.Start,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E293B),
            modifier = Modifier.width(32.dp)
        )

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 24.dp),
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

                val barWidth = 10.dp.toPx()
                val intraBarGap = 4.dp.toPx()

                val totalBarsWidth = (barWidth * 2) + intraBarGap
                val groupPadding = (barGroupWidth - totalBarsWidth) / 2f

                bars.forEachIndexed { index, bar ->
                    val groupX = barGroupWidth * index
                    val thisWeekColor =
                        if (bar.isHighlighted) Color(0xFFD9534F) else Color(0xFF005B82)

                    val thisWeekHeight = ((bar.thisWeekTemp.toFloat() - chartMin) / effectiveRange).coerceIn(0f, 1f) * size.height
                    val histHeight = ((bar.historicalAvgTemp.toFloat() - chartMin) / effectiveRange).coerceIn(0f, 1f) * size.height

                    drawRoundRect(
                        color = thisWeekColor,
                        topLeft = Offset(groupX + groupPadding, size.height - thisWeekHeight),
                        size = Size(barWidth, thisWeekHeight),
                        cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
                    )

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