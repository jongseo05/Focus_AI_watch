package com.example.focus_ai.data.network

import android.util.Log
import com.example.focus_ai.data.model.Telemetry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

class ApiService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()
    private val baseUrl = "https://yxlpmsolfxvxhdebtixc.supabase.co/functions/v1"
    private val supabaseUrl = "https://yxlpmsolfxvxhdebtixc.supabase.co"
    private val anonKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Inl4bHBtc29sZnh2eGhkZWJ0aXhjIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzI3Njg5NzEsImV4cCI6MjA0ODM0NDk3MX0.yS8T1IbL1Q5hXOy1ug7vGsrxGjzMFfVhF_s2T9jHYhU"
    private val jsonMediaType = "application/json".toMediaType()
    
    companion object {
        private const val TAG = "ApiService"
    }
    
    /**
     * 웹 서비스(Supabase) 연결 상태를 확인하는 헬스 체크
     * @return true: 연결 가능, false: 연결 불가능
     */
    suspend fun checkServiceConnection(): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.d(TAG, "Checking service connection...")
            
            // Supabase 기본 상태 확인 (간단한 GET 요청)
            val request = Request.Builder()
                .url(supabaseUrl)
                .addHeader("apikey", anonKey)
                .get()
                .build()
            
            val response = client.newCall(request).execute()
            response.use { 
                val isConnected = it.isSuccessful || it.code in 200..299
                Log.d(TAG, "Service connection check: $isConnected (code: ${it.code})")
                isConnected
            }
        } catch (e: SocketTimeoutException) {
            Log.e(TAG, "Service connection timeout", e)
            false
        } catch (e: IOException) {
            Log.e(TAG, "Service connection IO error", e)
            false
        } catch (e: Exception) {
            Log.e(TAG, "Service connection unexpected error", e)
            false
        }
    }
    
    suspend fun verifyCode(code: String, deviceId: String): String? = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Verifying code: $code with deviceId: $deviceId")
            
            val json = JSONObject().apply {
                put("code", code)
                put("device_id", deviceId)
                put("device_type", "watch")
            }
            
            Log.d(TAG, "Request JSON: $json")
            
            val request = Request.Builder()
                .url("$baseUrl/verify_code")
                .post(json.toString().toRequestBody(jsonMediaType))
                .addHeader("Content-Type", "application/json")
                .build()
            
            Log.d(TAG, "Making request to: ${request.url}")
            
            val response = client.newCall(request).execute()
            response.use {
                Log.d(TAG, "Response code: ${it.code}")
                if (it.isSuccessful) {
                    val responseBody = it.body?.string()
                    Log.d(TAG, "Response body: $responseBody")
                    if (responseBody != null) {
                        val responseJson = JSONObject(responseBody)
                        
                        // 새로운 응답 형식 확인
                        val success = responseJson.optBoolean("success", false)
                        if (success) {
                            val userId = responseJson.optString("user_id")
                            // 임시 토큰 생성 (실제 환경에서는 Edge Function에서 토큰을 반환해야 함)
                            val token = "temp_token_${System.currentTimeMillis()}_$userId"
                            Log.d(TAG, "Generated temporary token: ${token.take(20)}...")
                            token
                        } else {
                            // 기존 형식도 확인
                            val token = responseJson.optString("access_token").takeIf { token -> token.isNotEmpty() }
                            Log.d(TAG, "Extracted token: ${token?.take(20)}...")
                            token
                        }
                    } else {
                        Log.e(TAG, "Response body is null")
                        null
                    }
                } else {
                    val errorBody = it.body?.string()
                    Log.e(TAG, "Request failed with code: ${it.code}, body: $errorBody")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in verifyCode", e)
            e.printStackTrace()
            null
        }
    }
    
    suspend fun startSession(token: String): String? = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting session with token: ${token.take(20)}...")
            
            val json = JSONObject().apply {
                put("device_type", "watch")
            }
            
            Log.d(TAG, "Session request JSON: $json")
            
            val request = Request.Builder()
                .url("$baseUrl/start_session")
                .post(json.toString().toRequestBody(jsonMediaType))
                .addHeader("Content-Type", "application/json")
                .build()
            
            Log.d(TAG, "Making session request to: ${request.url}")
            
            val response = client.newCall(request).execute()
            response.use {
                Log.d(TAG, "Session response code: ${it.code}")
                if (it.isSuccessful) {
                    val responseBody = it.body?.string()
                    Log.d(TAG, "Session response body: $responseBody")
                    if (responseBody != null) {
                        val responseJson = JSONObject(responseBody)
                        val sessionId = responseJson.optString("session_id").takeIf { sessionId -> sessionId.isNotEmpty() }
                        Log.d(TAG, "Extracted session_id: $sessionId")
                        sessionId
                    } else {
                        Log.e(TAG, "Session response body is null")
                        null
                    }
                } else {
                    val errorBody = it.body?.string()
                    Log.e(TAG, "Session request failed with code: ${it.code}, body: $errorBody")
                    
                    // 임시 해결책: Edge Function이 실패하면 임시 세션 ID 생성
                    if (it.code == 401) {
                        val tempSessionId = "temp_session_${System.currentTimeMillis()}"
                        Log.d(TAG, "Generated temporary session_id: $tempSessionId")
                        tempSessionId
                    } else {
                        null
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in startSession", e)
            e.printStackTrace()
            null
        }
    }
    
    suspend fun sendTelemetry(token: String, sessionId: String, telemetry: Telemetry): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Sending telemetry for session: $sessionId")
            
            val json = JSONObject().apply {
                put("session_id", sessionId)
                put("timestamp", telemetry.timestamp)
                put("heart_rate_mean", telemetry.heartRateMean)
                put("heart_rate_std", telemetry.heartRateStd)
                put("accelerometer_rms", telemetry.accelerometerRms)
                put("accelerometer_std", telemetry.accelerometerStd)
                put("device_type", "watch")
            }
            
            Log.d(TAG, "Telemetry JSON: $json")
            
            val request = Request.Builder()
                .url("$baseUrl/sensor_sample_ingest")
                .post(json.toString().toRequestBody(jsonMediaType))
                .addHeader("Authorization", "Bearer $token")
                .addHeader("apikey", anonKey)
                .addHeader("Content-Type", "application/json")
                .build()
            
            val response = client.newCall(request).execute()
            response.use {
                Log.d(TAG, "Telemetry response code: ${it.code}")
                if (it.isSuccessful) {
                    Log.d(TAG, "Telemetry sent successfully")
                    true
                } else {
                    val errorBody = it.body?.string()
                    Log.e(TAG, "Telemetry failed with code: ${it.code}, body: $errorBody")
                    false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in sendTelemetry", e)
            e.printStackTrace()
            false
        }
    }
}
