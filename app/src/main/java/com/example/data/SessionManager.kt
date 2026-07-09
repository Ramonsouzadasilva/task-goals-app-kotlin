package com.example.data

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("notion_session_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_LOGGED_IN_USER = "logged_in_user"
        private const val PREFIX_USER = "user_pwd_"
    }

    fun register(username: String, password: String): Boolean {
        val trimmedUser = username.trim().lowercase()
        if (trimmedUser.isEmpty() || password.isEmpty()) return false
        
        // Check if user already exists
        if (prefs.contains(PREFIX_USER + trimmedUser)) {
            return false // User already exists
        }

        prefs.edit().putString(PREFIX_USER + trimmedUser, password).apply()
        return true
    }

    fun login(username: String, password: String): Boolean {
        val trimmedUser = username.trim().lowercase()
        if (trimmedUser.isEmpty() || password.isEmpty()) return false

        val storedPassword = prefs.getString(PREFIX_USER + trimmedUser, null)
        if (storedPassword == password) {
            prefs.edit().putString(KEY_LOGGED_IN_USER, username).apply()
            return true
        }
        return false
    }

    fun logout() {
        prefs.edit().remove(KEY_LOGGED_IN_USER).apply()
    }

    fun getLoggedInUser(): String? {
        return prefs.getString(KEY_LOGGED_IN_USER, null)
    }

    fun isLoggedIn(): Boolean {
        return getLoggedInUser() != null
    }
}
