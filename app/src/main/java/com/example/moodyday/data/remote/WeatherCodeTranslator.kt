package com.example.moodyday.data.remote

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.ui.graphics.vector.ImageVector

object WeatherCodeTranslator {
    fun toDescription(code: Int): String = when (code) {
        0 -> "Clear sky"
        1, 2, 3 -> "Partly cloudy"
        45, 48 -> "Foggy"
        51, 53, 55 -> "Drizzle"
        61, 63, 65 -> "Rain"
        71, 73, 75 -> "Snow"
        80, 81, 82 -> "Rain showers"
        95, 96, 99 -> "Thunderstorm"
        else -> "Unknown"
    }

    fun toEmoji(code: Int): String = when (code) {
        0 -> "☀️"
        1, 2, 3 -> "⛅"
        45, 48 -> "🌫️"
        51, 53, 55, 61, 63, 65, 80, 81, 82 -> "🌧️"
        71, 73, 75 -> "❄️"
        95, 96, 99 -> "⛈️"
        else -> "🌡️"
    }

    fun toIcon(code: Int): ImageVector = when (code) {
        0 -> Icons.Default.WbSunny
        1, 2, 3 -> Icons.Default.WbCloudy
        45, 48 -> Icons.Default.Cloud               // fog
        51, 53, 55, 61, 63, 65, 80, 81, 82 -> Icons.Default.WaterDrop  // rain or drizzle
        71, 73, 75 -> Icons.Default.AcUnit        // snow
        95, 96, 99 -> Icons.Default.Bolt          // thunderstorm
        else -> Icons.Default.WbSunny
    }
}