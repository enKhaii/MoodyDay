package com.example.moodyday.data.auth

import com.example.moodyday.data.remote.supabase
import io.github.jan.supabase.auth.auth

object SessionManager {
    val currentUserId: String?
        get() = supabase.auth.currentUserOrNull()?.id

    fun getActiveUserId(): String =
        supabase.auth.currentUserOrNull()?.id?.takeIf { it.isNotBlank() } ?: "chongwc"

    // throws if somehow called while logged out,
    // use in places that should never render without a session
    fun requireUserId(): String =
        currentUserId ?: throw IllegalStateException("No user is currently logged in")
}