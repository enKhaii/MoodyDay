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
import com.example.moodyday.ui.forecast.WeeklyBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.LocalDate
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

    /** Up to [count] candidate matches for autocomplete, as the user types. */
    suspend fun searchCities(query: String, count: Int = 5): List<GeocodingResult> =
        withContext(Dispatchers.IO) {
            if (query.isBlank()) return@withContext emptyList()
            runCatching { geocodingApi.searchCity(query, count).results.orEmpty() }
                .getOrDefault(emptyList())
        }

    /** Current + hourly + daily outlook for the forecast screen. */
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

            val hourly = weather.hourly?.let { hourlyBlock ->
                hourlyBlock.time
                    .zip(hourlyBlock.temperature_2m)
                    .zip(hourlyBlock.weather_code) { (time, temp), code -> Triple(time, temp, code) }
                    .filter { (time, _, _) -> time.startsWith(todayDateStr) }
                    .mapNotNull { (time, temp, code) ->
                        runCatching {
                            val hour = time.substring(11, 13).toInt()
                            HourlyPoint(label = formatHourLabel(hour), tempF = temp.roundToInt(), weatherCode = code)
                        }.getOrNull()
                    }
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
                        uvIndexMax = dailyBlock.uv_index_max?.getOrNull(i) ?: 0.0,
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

    /**
     * This week's forecast max temps vs the 30-year historical average for the
     * same calendar days. The archive endpoint only has *actuals*, so we average
     * the same date (+/- 3 days) across the last 30 years to build the norm.
     */
    suspend fun getHistoricalUiState(lat: Double, lon: Double): HistoricalUiState =
        withContext(Dispatchers.IO) {
            val weather = weatherApi.getWeather(lat, lon, pastDays = 0)
            val dailyBlock = weather.daily

            val bars = if (dailyBlock == null) emptyList() else coroutineScope {
                dailyBlock.time.take(6).mapIndexed { i, dateStr ->
                    async {
                        val date = runCatching { LocalDate.parse(dateStr) }.getOrNull()
                            ?: LocalDate.now().plusDays(i.toLong())
                        val thisWeekTemp = dailyBlock.temperature_2m_max.getOrNull(i) ?: 0.0
                        val histAvg = averageHistoricalMax(lat, lon, date)
                        WeeklyBar(
                            dayLabel = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.US),
                            thisWeekTemp = thisWeekTemp,
                            historicalAvgTemp = histAvg,
                            isHighlighted = date.dayOfWeek == DayOfWeek.SATURDAY
                        )
                    }
                }.map { it.await() }
            }

            val heatWarning = bars.any { it.thisWeekTemp - it.historicalAvgTemp >= 5.0 }

            HistoricalUiState(bars = bars, heatWarning = heatWarning)
        }

    /**
     * Averages temperature_2m_max for the same calendar day (+/- 3 days) across
     * the last 30 years. One archive request per bar: a 30-year span filtered
     * client-side to the matching month/day window, rather than 30 separate calls.
     */
    private suspend fun averageHistoricalMax(lat: Double, lon: Double, date: LocalDate): Double {
        val fmt = DateTimeFormatter.ISO_LOCAL_DATE
        val endYear = date.minusYears(1)   // archive data lags behind "today"
        val startYear = date.minusYears(30)

        val response = runCatching {
            archiveApi.getArchive(
                lat = lat,
                lon = lon,
                startDate = startYear.withDayOfYear(1).format(fmt),
                endDate = endYear.withMonth(12).withDayOfMonth(31).format(fmt)
            )
        }.getOrNull() ?: return 0.0

        val targetMonth = date.monthValue
        val targetDay = date.dayOfMonth

        val matches = response.daily.time.indices.filter { i ->
            val d = LocalDate.parse(response.daily.time[i])
            val dayDiff = kotlin.math.abs(d.dayOfYear - date.withYear(d.year).dayOfYear)
            (d.monthValue == targetMonth && kotlin.math.abs(d.dayOfMonth - targetDay) <= 3) || dayDiff <= 3
        }.mapNotNull { response.daily.temperature_2m_max.getOrNull(it) ?: null }

        return if (matches.isEmpty()) 0.0 else matches.filterNotNull().average()
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