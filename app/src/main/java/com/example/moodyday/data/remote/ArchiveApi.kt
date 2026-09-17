package com.example.moodyday.data.remote

import com.example.moodyday.data.remote.dto.ArchiveResponse
import retrofit2.http.GET
import retrofit2.http.Query

// This file is to compare the recent weather for the chart
interface ArchiveApi {
    @GET("v1/archive")
    suspend fun getArchive(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String,
        @Query("daily") daily: String = "temperature_2m_max,temperature_2m_min",
        @Query("timezone") timezone: String = "auto"
    ): ArchiveResponse
}