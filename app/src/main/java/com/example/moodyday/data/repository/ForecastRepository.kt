package com.example.moodyday.data.repository

import com.example.moodyday.data.remote.ArchiveApi
import com.example.moodyday.data.remote.GeocodingApi
import com.example.moodyday.data.remote.WeatherApi
import com.example.moodyday.data.remote.WeatherCodeTranslator
import com.example.moodyday.data.remote.dto.GeocodingResult
import com.example.moodyday.ui.forecast.DailyOutlook
import com.example.moodyday.ui.forecast.HistoricalUiState
import com.example.moodyday.ui.forecast.HomeUiState
import com.example.moodyday.ui.forecast.HourlyPoint
import com.example.moodyday.ui.forecast.InsightType
import com.example.moodyday.ui.forecast.WeeklyBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Wraps WeatherApi / ArchiveApi / GeocodingApi and turns their raw DTOs
 * into the UI-friendly state the forecast screen reads.
 */
class ForecastRepository(
    private val weatherApi: WeatherApi,
    private val archiveApi: ArchiveApi,
    private val geocodingApi: GeocodingApi
) {

    // Turns city name input into coordinates
    suspend fun geocode(cityQuery: String): Triple<Double, Double, String> = // Triple is the type of holding 3 values
        withContext(Dispatchers.IO) {
            val result = geocodingApi.searchCity(cityQuery, count = 1).results?.firstOrNull()
                ?: throw IllegalStateException("City not found: $cityQuery")
            val label = buildString {
                append(result.name)
                if (!result.admin1.isNullOrEmpty()) append(", ${result.admin1}")
                if (!result.countryCode.isNullOrEmpty()) append(", ${result.countryCode.uppercase()}")
            }

            Triple(result.latitude, result.longitude, label)
        }

    suspend fun searchCities(query: String, count: Int = 5): List<GeocodingResult> =
        withContext(Dispatchers.IO) {
            if (query.isBlank()) return@withContext emptyList()
            runCatching { geocodingApi.searchCity(query, count).results.orEmpty() }
                .getOrDefault(emptyList())
        }

    // Current + hourly + daily outlook for the forecast screen.
    suspend fun getHomeUiState(
        lat: Double,
        lon: Double,
        cityName: String,
        countryCode: String?
    ): HomeUiState =
        withContext(Dispatchers.IO) {
            val weather = weatherApi.getWeather(lat, lon, pastDays = 0)
            val todayDateStr = weather.daily?.time?.firstOrNull() ?: LocalDate.now().toString()
            val todayDate = runCatching { LocalDate.parse(todayDateStr) }.getOrNull() ?: LocalDate.now()

            // Round down to the current hour so "2:47 PM" still matches the
            // "2:00 PM" forecast entry instead of skipping straight to 3 PM.
            val nowDateTime = LocalDateTime.now().withMinute(0).withSecond(0).withNano(0)

            val hourly = weather.hourly?.let { hourlyBlock ->
                hourlyBlock.time
                    .zip(hourlyBlock.temperature_2m)
                    .zip(hourlyBlock.weather_code) { (time, temp), code -> Triple(time, temp, code) }
                    .mapNotNull { (time, temp, code) ->
                        runCatching {
                            val dt = LocalDateTime.parse(time)
                            if (dt < nowDateTime) return@runCatching null
                            HourlyPoint(
                                label = formatHourLabel(dt.hour),
                                tempF = temp.roundToInt(),
                                weatherCode = code
                            )
                        }.getOrNull()
                    }
                    .take(24) // cap how far ahead we carry; Meteo returns several days of hourly data
            } ?: emptyList()

            val daily = weather.daily?.let { dailyBlock ->
                dailyBlock.time.indices.mapNotNull { i ->
                    val dateStr = dailyBlock.time.getOrNull(i) ?: return@mapNotNull null
                    val date = runCatching { LocalDate.parse(dateStr) }.getOrNull() ?: return@mapNotNull null
                    DailyOutlook(
                        date = dateStr,
                        label = if (i == 0 || date == todayDate) "Today"
                        else date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.US),
                        weatherCode = dailyBlock.weather_code.getOrNull(i) ?: 0,
                        rainChancePercent = dailyBlock.precipitation_probability_max?.getOrNull(i) ?: 0,
                        tempMin = dailyBlock.temperature_2m_min.getOrNull(i)?.roundToInt() ?: 0,
                        tempMax = dailyBlock.temperature_2m_max.getOrNull(i)?.roundToInt() ?: 0
                    )
                }
            } ?: emptyList()

            HomeUiState(
                cityName = cityName,
                countryCode = countryCode,
                conditionLabel = WeatherCodeTranslator.toDescription(weather.current.weather_code),
                nowTempF = weather.current.temperature_2m.roundToInt(),
                apparentTempF = weather.current.apparent_temperature.roundToInt(),
                humidityPercent = weather.current.relative_humidity_2m.roundToInt(),
                windSpeed = weather.current.wind_speed_10m,
                nowWeatherCode = weather.current.weather_code,
                hourly = hourly,
                daily = daily
            )
        }

    suspend fun getHistoricalUiState(lat: Double, lon: Double): HistoricalUiState =
        withContext(Dispatchers.IO) {
            val weather = weatherApi.getWeather(lat, lon, pastDays = 0)
            val dailyBlock = weather.daily

            val rawBars = if (dailyBlock == null) emptyList() else coroutineScope {
                dailyBlock.time.take(7).mapIndexed { i, dateStr ->
                    async {
                        val date = runCatching { LocalDate.parse(dateStr) }.getOrNull()
                            ?: LocalDate.now().plusDays(i.toLong())
                        val thisWeekTemp = dailyBlock.temperature_2m_max.getOrNull(i) ?: 0.0
                        val histAvg = averageHistoricalMax(lat, lon, date)
                        WeeklyBar(
                            dayLabel = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.US),
                            thisWeekTemp = thisWeekTemp,
                            historicalAvgTemp = histAvg
                        )
                    }
                }.map { it.await() }
            }

            // --- Bar highlighting: still based on the single most extreme day (unchanged) ---
            val maxDeviationIndex = rawBars.indices.maxByOrNull { i ->
                kotlin.math.abs(rawBars[i].thisWeekTemp - rawBars[i].historicalAvgTemp)
            } ?: -1

            val bars = rawBars.mapIndexed { index, bar ->
                bar.copy(isHighlighted = index == maxDeviationIndex)
            }

            // --- Message/type: now based on the WEEK'S AVERAGE deviation, not just one day ---
            val avgDeviation = if (bars.isEmpty()) 0.0
            else bars.map { it.thisWeekTemp - it.historicalAvgTemp }.average()

            val (insightMessage, insightType) = when {
                avgDeviation >= 3.0 ->
                    "Temperatures this week are projected to be significantly above the 10-year average. Hydration and shade recommended." to InsightType.WARMER
                avgDeviation <= -3.0 ->
                    "Temperatures this week are projected to be significantly below the 10-year average. Dress warmly." to InsightType.COOLER
                else ->
                    "This week's temperatures are tracking close to the 10-year average." to InsightType.NEUTRAL
            }

            HistoricalUiState(
                bars = bars,
                insightMessage = insightMessage,
                insightType = insightType
            )
        }

    private suspend fun averageHistoricalMax(lat: Double, lon: Double, date: LocalDate): Double {
        val fmt = DateTimeFormatter.ISO_LOCAL_DATE
        val targetMonth = date.monthValue
        val targetDay = date.dayOfMonth

        // Fetch only a +-3 day window around this date, across 10 past years —
        val results = coroutineScope {
            (1..10).map { yearsAgo ->
                async {
                    val yearDate = date.minusYears(yearsAgo.toLong())
                    val windowStart = yearDate.minusDays(3)
                    val windowEnd = yearDate.plusDays(3)

                    runCatching {
                        archiveApi.getArchive(
                            lat = lat,
                            lon = lon,
                            startDate = windowStart.format(fmt),
                            endDate = windowEnd.format(fmt)
                        )
                    }.getOrNull()
                }
            }.awaitAll()
        }

        val allTemps = results.filterNotNull().flatMap { response ->
            response.daily.temperature_2m_max.filterNotNull()
        }

        return if (allTemps.isEmpty()) 0.0 else allTemps.average()
    }

    private fun formatHourLabel(hour24: Int): String {
        val period = if (hour24 >= 12) "PM" else "AM"
        val hour12 = when {
            hour24 == 0 -> 12
            hour24 > 12 -> hour24 - 12
            else -> hour24
        }
        return "$hour12 $period"
    }
}