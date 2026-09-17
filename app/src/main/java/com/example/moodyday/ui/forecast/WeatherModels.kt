package com.example.moodyday.ui.forecast

// ---------- UI-friendly derived models for the Forecast screen ----------
// Built by ForecastRepository from WeatherResponse / ArchiveResponse.

data class HourlyPoint(
    val label: String,      // "1 PM"
    val tempF: Int,
    val weatherCode: Int,
    val isNight: Boolean = false
)

data class DailyOutlook(
    val date: String,         // ISO "2026-09-03", stable key for notes/goals
    val label: String,        // "Today", "Mon", "Tue"...
    val weatherCode: Int,
    val rainChancePercent: Int,
    val tempMin: Int,
    val tempMax: Int
)

data class WeeklyBar(
    val dayLabel: String,   // "Mon"
    val thisWeekTemp: Double,
    val historicalAvgTemp: Double,
    val isHighlighted: Boolean = false // e.g. Saturday, shown in red in the mock
)

data class HomeUiState(
    val cityName: String = "",
    val countryCode: String? = null,
    val conditionLabel: String = "",
    val nowTempF: Int = 0,
    val apparentTempF: Int = 0,
    val humidityPercent: Int = 0,
    val windSpeed: Double = 0.0,
    val nowWeatherCode: Int = 0,
    val hourly: List<HourlyPoint> = emptyList(),
    val daily: List<DailyOutlook> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

enum class InsightType { WARMER, COOLER, NEUTRAL }

data class HistoricalUiState(
    val bars: List<WeeklyBar> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val insightMessage: String = "",
    val insightType: InsightType = InsightType.NEUTRAL
)