package com.example.moodyday.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// NOMINATIM = Tool that uses OpenStreetMap data to find locations by name and address
@Serializable
data class NominatimResponse(
    val address: NominatimAddress? = null
)

@Serializable
data class NominatimAddress(
    val city: String? = null,
    val town: String? = null,
    val village: String? = null,
    val state: String? = null,
    val country: String ?= null,
    // Nominatim JSON uses "country_code" so need to label explicitly
    @SerialName("country_code") val countryCode: String? = null
)