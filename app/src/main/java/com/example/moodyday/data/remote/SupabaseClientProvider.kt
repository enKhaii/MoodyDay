package com.example.moodyday.data.remote

import com.example.moodyday.BuildConfig
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

// TODO: Paste your Supabase Project URL and anon key below
const val SUPABASE_URL = "https://vejnapuitqyzrwigzprr.supabase.co/"
const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InZlam5hcHVpdHF5enJ3aWd6cHJyIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODgyNDQxMDYsImV4cCI6MjEwMzgyMDEwNn0.Wbqm-Pwn0Sfc9V2USLal77Hs-8LyMYNThtWrwhkZ0zs"

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