package com.example.moodyday.data.remote

import com.example.moodyday.data.remote.dto.NominatimResponse
import retrofit2.http.GET
import retrofit2.http.Query

// NOMINATIM = Tool that uses OpenStreetMap data to find locations by name and address
interface NominatimApi {
    @GET("reverse")
    suspend fun reverseGeocode(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("format") format: String = "json",
        @Query("addressdetails") addressDetails: Int = 1
    ): NominatimResponse
}