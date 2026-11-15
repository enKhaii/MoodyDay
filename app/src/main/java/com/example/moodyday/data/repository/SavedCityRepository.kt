package com.example.moodyday.data.repository

import com.example.moodyday.data.local.dao.SavedCityDao
import com.example.moodyday.data.local.entities.SavedCityEntity
import com.example.moodyday.data.remote.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable

@Serializable
data class SavedCityRemote(
    val id: Long? = null,
    val user_id: String,
    val city_name: String,
    val country: String? = null,
    val lat: Double,
    val lon: Double,
    val is_home: Boolean = false,
    val sort_order: Int = 0
)

class SavedCityRepository(private val dao: SavedCityDao) {
    fun getCities(userId: String): Flow<List<SavedCityEntity>> = dao.getCitiesForUser(userId)

    /** Returns the already-saved row for these coordinates, if one exists. */
    suspend fun findByCoordinates(userId: String, lat: Double, lon: Double): SavedCityEntity? =
        dao.getCityByCoordinates(userId, lat, lon)

    /** Returns the local row id and syncs the city to Supabase if authenticated. */
    suspend fun addCity(city: SavedCityEntity): Long {
        val existing = dao.getCityByCoordinates(city.userId, city.lat, city.lon)
        val localId = existing?.id ?: dao.insertCity(city)

        val authId = supabase.auth.currentUserOrNull()?.id ?: city.userId.takeIf { it.contains("-") }
        if (!authId.isNullOrBlank()) {
            try {
                val remote = supabase.from("saved_cities").insert(
                    SavedCityRemote(
                        user_id = authId,
                        city_name = city.cityName,
                        country = city.country,
                        lat = city.lat,
                        lon = city.lon,
                        is_home = city.isHome,
                        sort_order = city.sortOrder
                    )
                ) { select() }.decodeSingle<SavedCityRemote>()

                dao.updateSupabaseId(localId, remote.id)
            } catch (e: Exception) {
                // offline — stays local only
            }
        }
        return localId
    }

    suspend fun deleteCity(city: SavedCityEntity) {
        dao.deleteCity(city)
        val authId = supabase.auth.currentUserOrNull()?.id ?: city.userId.takeIf { it.contains("-") }
        try {
            if (city.supabaseId != null) {
                supabase.from("saved_cities").delete { filter { eq("id", city.supabaseId) } }
            } else if (!authId.isNullOrBlank()) {
                supabase.from("saved_cities").delete {
                    filter {
                        eq("user_id", authId)
                        eq("city_name", city.cityName)
                    }
                }
            }
        } catch (e: Exception) {
            // offline or network error
        }
    }

    /** Fetches saved cities from Supabase and populates local Room database. */
    suspend fun fetchFromRemote(userId: String) {
        val authId = supabase.auth.currentUserOrNull()?.id ?: userId.takeIf { it.contains("-") } ?: return
        try {
            val remoteCities = supabase.from("saved_cities")
                .select { filter { eq("user_id", authId) } }
                .decodeList<SavedCityRemote>()

            for (remote in remoteCities) {
                val existing = dao.getCityByCoordinates(userId, remote.lat, remote.lon)
                if (existing == null) {
                    dao.insertCity(
                        SavedCityEntity(
                            supabaseId = remote.id,
                            userId = userId,
                            cityName = remote.city_name,
                            country = remote.country,
                            lat = remote.lat,
                            lon = remote.lon,
                            isHome = remote.is_home,
                            sortOrder = remote.sort_order
                        )
                    )
                } else if (existing.supabaseId == null && remote.id != null) {
                    dao.updateSupabaseId(existing.id, remote.id)
                }
            }
        } catch (e: Exception) {
            // offline fallback
        }
    }
}