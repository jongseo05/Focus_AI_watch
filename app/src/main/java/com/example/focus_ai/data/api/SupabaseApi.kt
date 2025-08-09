package com.example.focus_ai.data.api

import com.example.focus_ai.data.model.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface SupabaseApi {
    
    @POST("functions/v1/verify_code")
    suspend fun verifyCode(
        @Body request: VerifyCodeRequest
    ): Response<VerifyCodeResponse>
    
    @POST("functions/v1/sensor_sample_ingest")
    suspend fun uploadSensorData(
        @Header("Authorization") authorization: String,
        @Body request: SensorSampleRequest
    ): Response<SensorSampleResponse>
    
    @POST("functions/v1/start_session")
    suspend fun startSession(
        @Header("Authorization") authorization: String,
        @Body request: StartSessionRequest
    ): Response<StartSessionResponse>
}
