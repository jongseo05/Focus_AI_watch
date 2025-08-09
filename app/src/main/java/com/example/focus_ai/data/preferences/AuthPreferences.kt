package com.example.focus_ai.data.preferences

import android.content.Context
import android.content.SharedPreferences

class AuthPreferences(context: Context) {
    
    companion object {
        private const val PREF_NAME = "auth_preferences"
        private const val KEY_JWT_TOKEN = "jwt_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_SESSION_ID = "session_id"
        private const val KEY_IS_CONNECTED = "is_connected"
    }
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    
    var jwtToken: String?
        get() = prefs.getString(KEY_JWT_TOKEN, null)
        set(value) = prefs.edit().putString(KEY_JWT_TOKEN, value).apply()
    
    var userId: String?
        get() = prefs.getString(KEY_USER_ID, null)
        set(value) = prefs.edit().putString(KEY_USER_ID, value).apply()
    
    var sessionId: String?
        get() = prefs.getString(KEY_SESSION_ID, null)
        set(value) = prefs.edit().putString(KEY_SESSION_ID, value).apply()
    
    var isConnected: Boolean
        get() = prefs.getBoolean(KEY_IS_CONNECTED, false)
        set(value) = prefs.edit().putBoolean(KEY_IS_CONNECTED, value).apply()
    
    fun clear() {
        prefs.edit().clear().apply()
    }
    
    fun isAuthenticated(): Boolean {
        return !jwtToken.isNullOrEmpty() && !userId.isNullOrEmpty()
    }
    
    fun hasActiveSession(): Boolean {
        return isAuthenticated() && !sessionId.isNullOrEmpty() && isConnected
    }
}
