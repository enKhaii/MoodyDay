package com.example.moodyday.data.repository

import com.example.moodyday.data.local.dao.SavedCityDao
import com.example.moodyday.data.local.entities.SavedCityEntity
import com.example.moodyday.data.remote.supabase
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

    suspend fun addCity(city: SavedCityEntity) {
        val localId = dao.insertCity(city)
        try {
            val remote = supabase.from("saved_cities").insert(
                SavedCityRemote(
                    user_id = city.userId,
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
            // offline — stays local only, supabaseId remains null
        }
    }

    suspend fun deleteCity(city: SavedCityEntity) {
        dao.deleteCity(city)
        city.supabaseId?.let { id ->
            try {
                supabase.from("saved_cities").delete { filter { eq("id", id) } }
            } catch (e: Exception) { /* will retry later */ }
        }
    }
}