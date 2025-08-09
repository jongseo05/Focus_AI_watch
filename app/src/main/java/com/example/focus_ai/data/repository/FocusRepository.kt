package com.example.focus_ai.data.repository

import android.content.Context
import android.util.Log
import com.example.focus_ai.data.network.ApiService
import com.example.focus_ai.data.preferences.AuthPreferences
import com.example.focus_ai.data.sensor.SensorCollector
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect

class FocusRepository(private val context: Context) {
    private val apiService = ApiService()
    private val sensorCollector = SensorCollector(context)
    private val authPreferences = AuthPreferences(context)
    
    private var telemetryJob: Job? = null
    
    companion object {
        private const val TAG = "FocusRepository"
    }
    
    suspend fun authenticateWithCode(code: String, deviceId: String): Boolean {
        Log.d(TAG, "Authenticating with code: $code")
        val token = apiService.verifyCode(code, deviceId)
        return if (token != null) {
            Log.d(TAG, "Authentication successful, saving token")
            
            // AuthPreferences에 토큰 저장
            authPreferences.jwtToken = token
            
            // 임시 토큰에서 userId 추출 (temp_token_timestamp_userId 형식)
            val userIdFromToken = if (token.startsWith("temp_token_")) {
                token.split("_").takeIf { it.size >= 4 }?.drop(2)?.joinToString("_")
            } else null
            
            if (userIdFromToken != null) {
                authPreferences.userId = userIdFromToken
                Log.d(TAG, "Extracted and saved userId: $userIdFromToken")
            }
            
            true
        } else {
            Log.e(TAG, "Authentication failed")
            false
        }
    }
    
    suspend fun startFocusSession(): Boolean {
        val token = authPreferences.jwtToken
        if (token == null) {
            Log.e(TAG, "No access token available")
            return false
        }
        
        Log.d(TAG, "Starting focus session with token")
        val sessionIdResult = apiService.startSession(token)
        return if (sessionIdResult != null) {
            Log.d(TAG, "Session started successfully: $sessionIdResult")
            authPreferences.sessionId = sessionIdResult
            startSensorCollection()
            true
        } else {
            Log.e(TAG, "Failed to start session")
            false
        }
    }
    
    private fun startSensorCollection() {
        val token = authPreferences.jwtToken ?: return
        val session = authPreferences.sessionId ?: return
        
        Log.d(TAG, "Starting sensor collection for session: $session")
        sensorCollector.start()
        
        telemetryJob = CoroutineScope(Dispatchers.IO).launch {
            sensorCollector.telemetryFlow.collect { telemetry ->
                Log.d(TAG, "Received telemetry: HR=${telemetry.heartRateMean}, ACC=${telemetry.accelerometerRms}")
                val success = apiService.sendTelemetry(token, session, telemetry)
                if (!success) {
                    Log.e(TAG, "Failed to send telemetry: $telemetry")
                } else {
                    Log.d(TAG, "Telemetry sent successfully")
                }
            }
        }
    }
    
    fun stopFocusSession() {
        Log.d(TAG, "Stopping focus session")
        telemetryJob?.cancel()
        sensorCollector.stop()
        authPreferences.sessionId = null
        authPreferences.isConnected = false
    }
    
    fun isSessionActive(): Boolean {
        return authPreferences.sessionId != null && sensorCollector.isRunning()
    }
    
    fun disconnect() {
        Log.d(TAG, "Disconnecting and clearing all auth data")
        stopFocusSession()
        authPreferences.clear()
    }
}
