package com.example.focus_ai.data.model

import com.google.gson.annotations.SerializedName

// 코드 검증 요청 모델
data class VerifyCodeRequest(
    @SerializedName("code")
    val code: String
)

// 코드 검증 응답 모델
data class VerifyCodeResponse(
    @SerializedName("jwt")
    val jwt: String,
    
    @SerializedName("user_id")
    val userId: String,
    
    @SerializedName("message")
    val message: String
)

// 센서 데이터 업로드 요청 모델 (API 명세서에 맞춤)
data class SensorSampleRequest(
    @SerializedName("session_id")
    val sessionId: String,
    
    @SerializedName("heart_rate")
    val heartRate: Int? = null,
    
    @SerializedName("steps")
    val steps: Int? = null,
    
    @SerializedName("activity_level")
    val activityLevel: String? = null, // "low", "medium", "high"
    
    @SerializedName("timestamp")
    val timestamp: String, // ISO 8601 format
    
    @SerializedName("device_type")
    val deviceType: String = "watch"
)

// 센서 데이터 업로드 응답 모델
data class SensorSampleResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("sample_id")
    val sampleId: String,
    
    @SerializedName("message")
    val message: String
)

// 세션 시작 요청 모델
data class StartSessionRequest(
    @SerializedName("device_type")
    val deviceType: String = "watch"
)

// 세션 시작 응답 모델
data class StartSessionResponse(
    @SerializedName("session_id")
    val sessionId: String,
    
    @SerializedName("started_at")
    val startedAt: String,
    
    @SerializedName("message")
    val message: String
)

// API 에러 응답 모델 (Supabase 형식)
data class ApiErrorResponse(
    @SerializedName("message")
    val message: String,
    
    @SerializedName("code")
    val code: Int? = null
)
