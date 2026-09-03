package com.example.moodyday.data.remote

import com.example.moodyday.BuildConfig
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

const val SUPABASE_URL = BuildConfig.SUPABASE_URL
const val SUPABASE_KEY = BuildConfig.SUPABASE_ANON_KEY

// Supabase client instance, created once for the whole app
val supabase by lazy {
    createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        install(Postgrest)
        install(Auth)
    }
}