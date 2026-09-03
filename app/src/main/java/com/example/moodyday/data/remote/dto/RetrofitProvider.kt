package com.example.moodyday.data.remote

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

object RetrofitProvider {
    // Retrofit == Adding technology into existing systems
    private val json = Json { ignoreUnknownKeys = true }

    val weatherApi: WeatherApi = Retrofit.Builder()
        .baseUrl("https://api.open-meteo.com/")
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(WeatherApi::class.java)

    val geocodingApi: GeocodingApi = Retrofit.Builder()
        .baseUrl("https://geocoding-api.open-meteo.com/")
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(GeocodingApi::class.java)

        // Separate host for archive api
    val archiveApi: ArchiveApi = Retrofit.Builder()
        .baseUrl("https://archive-api.open-meteo.com/")
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(ArchiveApi::class.java)
}