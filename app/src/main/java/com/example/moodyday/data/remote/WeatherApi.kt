package com.example.moodyday.data.remote

import com.example.moodyday.data.remote.dto.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {
    @GET("v1/forecast")
    suspend fun getWeather(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("current") current: String = "temperature_2m,apparent_temperature,relative_humidity_2m,wind_speed_10m,weather_code",
        @Query("hourly") hourly: String = "temperature_2m,wind_speed_10m,weather_code",
        @Query("daily") daily: String = "temperature_2m_max,temperature_2m_min,weather_code,uv_index_max",
        @Query("timezone") timezone: String = "auto",
        @Query("past_days") pastDays: Int = 1               // includes yesterday in the daily array
    ): WeatherResponse
}