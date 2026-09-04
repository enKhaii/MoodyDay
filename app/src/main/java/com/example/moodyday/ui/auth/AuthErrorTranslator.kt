package com.example.moodyday.ui.auth

object AuthErrorTranslator {
    fun parseRegistrationError(e: Throwable): String {
        val raw = (e.message ?: e.localizedMessage ?: "").lowercase()
        return when {
            raw.contains("already registered") || raw.contains("user_already_exists") || raw.contains("already exists") ->
                "This email is already registered. Please proceed to login."
            raw.contains("at least 6 characters") || (raw.contains("password") && (raw.contains("short") || raw.contains("weak") || raw.contains("least 6"))) ->
                "Password must be at least 6 characters long."
            raw.contains("invalid email") || raw.contains("valid email") ->
                "Please enter a valid email address."
            raw.contains("rate limit") || raw.contains("too many requests") || raw.contains("over_email_send_rate_limit") ->
                "Too many attempts. Please wait a moment and try again."
            raw.contains("unable to resolve host") || raw.contains("no address associated with hostname") || raw.contains("timeout") || raw.contains("connect") ->
                "Unable to connect to the server. Please check your internet connection."
            else ->
                "Registration failed. Please verify your details and try again."
        }
    }

    fun parseLoginError(e: Throwable): String {
        val raw = (e.message ?: e.localizedMessage ?: "").lowercase()
        return when {
            raw.contains("invalid login credentials") || raw.contains("invalid_credentials") ->
                "Incorrect email or password. Please try again."
            raw.contains("email not confirmed") ->
                "Please verify your email address before signing in."
            raw.contains("rate limit") || raw.contains("too many requests") ->
                "Too many failed login attempts. Please wait a moment and try again."
            raw.contains("unable to resolve host") || raw.contains("no address associated with hostname") || raw.contains("timeout") || raw.contains("connect") ->
                "Unable to connect to the server. Please check your internet connection."
            else ->
                "Login failed. Please check your credentials and try again."
        }
    }
}
