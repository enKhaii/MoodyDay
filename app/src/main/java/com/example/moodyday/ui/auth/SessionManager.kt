package com.example.moodyday.data.auth

import com.example.moodyday.data.remote.supabase
import io.github.jan.supabase.auth.auth

object SessionManager {
    val currentUserId: String?
        get() = supabase.auth.currentUserOrNull()?.id

    // throws if somehow called while logged out,
    // use in places that should never render without a session
    fun requireUserId(): String =
        currentUserId ?: throw IllegalStateException("No user is currently logged in")
}