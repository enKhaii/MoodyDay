package com.example.moodyday.data.repository

import com.example.moodyday.data.local.dao.GoalDao
import com.example.moodyday.data.local.dao.UserStreakDao
import com.example.moodyday.data.local.entities.UserStreakEntity
import com.example.moodyday.data.remote.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.Serializable

@Serializable
data class UserStreakRemote(
    val id: Long? = null,
    val user_id: String,
    val current_streak: Int = 0,
    val last_completed_date: String? = null,
    val completed_goals_count: Int = 0
)

class UserStreakRepository(
    private val userStreakDao: UserStreakDao,
    private val goalDao: GoalDao? = null
) {
    fun getUserStreak(userId: String): Flow<UserStreakEntity?> =
        userStreakDao.getUserStreak(userId)

    suspend fun saveStreak(
        userId: String,
        currentStreak: Int,
        lastCompletedDate: String?,
        completedGoalsCount: Int? = null
    ) {
        // 1. Save to local Room
        val existing = userStreakDao.getUserStreak(userId).firstOrNull()
        userStreakDao.insertOrUpdateStreak(
            UserStreakEntity(
                id = existing?.id ?: 0,
                userId = userId,
                currentStreak = currentStreak,
                lastCompletedDate = lastCompletedDate
            )
        )

        // 2. Sync to Supabase in background if user is authenticated
        val authId = supabase.auth.currentUserOrNull()?.id
        if (!authId.isNullOrBlank()) {
            try {
                val goalCount = completedGoalsCount ?: run {
                    val allGoals = goalDao?.getGoalsForUser(userId)?.firstOrNull() ?: emptyList()
                    allGoals.count { it.isCompleted }
                }

                val remoteList = supabase.from("user_streaks")
                    .select { filter { eq("user_id", authId) } }
                    .decodeList<UserStreakRemote>()

                if (remoteList.isNotEmpty()) {
                    val remoteId = remoteList.first().id
                    supabase.from("user_streaks").update(
                        UserStreakRemote(
                            id = remoteId,
                            user_id = authId,
                            current_streak = currentStreak,
                            last_completed_date = lastCompletedDate,
                            completed_goals_count = goalCount
                        )
                    ) {
                        filter { eq("user_id", authId) }
                    }
                } else {
                    supabase.from("user_streaks").insert(
                        UserStreakRemote(
                            user_id = authId,
                            current_streak = currentStreak,
                            last_completed_date = lastCompletedDate,
                            completed_goals_count = goalCount
                        )
                    )
                }
            } catch (e: Exception) {
                // Offline or network failure — data remains safely saved in local Room
            }
        }
    }

    suspend fun fetchFromRemote(userId: String) {
        val authId = supabase.auth.currentUserOrNull()?.id
        if (!authId.isNullOrBlank()) {
            try {
                val remoteList = supabase.from("user_streaks")
                    .select { filter { eq("user_id", authId) } }
                    .decodeList<UserStreakRemote>()

                val remote = remoteList.firstOrNull() ?: return
                val existing = userStreakDao.getUserStreak(userId).firstOrNull()

                // If remote has a higher or more recent streak, restore it
                userStreakDao.insertOrUpdateStreak(
                    UserStreakEntity(
                        id = existing?.id ?: 0,
                        userId = userId,
                        currentStreak = maxOf(existing?.currentStreak ?: 0, remote.current_streak),
                        lastCompletedDate = remote.last_completed_date ?: existing?.lastCompletedDate
                    )
                )
            } catch (e: Exception) {
                // Offline fallback
            }
        }
    }
}
