package com.example.moodyday.data.repository

import com.example.moodyday.data.local.dao.AlertRuleDao
import com.example.moodyday.data.local.entities.AlertRuleEntity
import com.example.moodyday.data.remote.supabase
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable

@Serializable
data class AlertRuleRemote(
    val id: Long? = null,
    val user_id: String,
    val city_id: Long,
    val condition: String,
    val threshold: Double,
    val is_enabled: Boolean = true
)

class AlertRuleRepository(private val dao: AlertRuleDao) {
    fun getRules(userId: String): Flow<List<AlertRuleEntity>> = dao.getRulesForUser(userId)

    suspend fun addRule(rule: AlertRuleEntity) {
        val localId = dao.insertRule(rule)
        try {
            val remote = supabase.from("alert_rules").insert(
                AlertRuleRemote(
                    user_id = rule.userId,
                    city_id = rule.cityId,
                    condition = rule.condition,
                    threshold = rule.threshold,
                    is_enabled = rule.isEnabled
                )
            ) { select() }.decodeSingle<AlertRuleRemote>()

            dao.updateSupabaseId(localId, remote.id)
        } catch (e: Exception) {
            // stays local only
        }
    }

    suspend fun updateRule(rule: AlertRuleEntity) {
        dao.updateRule(rule)
        rule.supabaseId?.let { id ->
            try {
                supabase.from("alert_rules").update(
                    AlertRuleRemote(
                        id = id,
                        user_id = rule.userId,
                        city_id = rule.cityId,
                        condition = rule.condition,
                        threshold = rule.threshold,
                        is_enabled = rule.isEnabled
                    )
                ) { filter { eq("id", id) } }
            } catch (e: Exception) { /* retry later */ }
        }
    }

    suspend fun deleteRule(rule: AlertRuleEntity) {
        dao.deleteRule(rule)
        rule.supabaseId?.let { id ->
            try {
                supabase.from("alert_rules").delete { filter { eq("id", id) } }
            } catch (e: Exception) { /* retry later */ }
        }
    }
}