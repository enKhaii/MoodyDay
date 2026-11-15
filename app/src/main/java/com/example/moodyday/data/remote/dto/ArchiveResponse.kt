package com.example.moodyday.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ArchiveResponse(
    val daily: ArchiveDailyWeather
)

@Serializable
data class ArchiveDailyWeather(
    val time: List<String>,
    val temperature_2m_max: List<Double?>,
    val temperature_2m_min: List<Double?>
)