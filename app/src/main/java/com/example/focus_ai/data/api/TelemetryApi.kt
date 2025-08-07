package com.example.focus_ai.data.api

import com.example.focus_ai.data.model.Telemetry
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface TelemetryApi {
    @POST("telemetry")
    suspend fun uploadTelemetry(
        @Header("Authorization") authorization: String,
        @Body telemetry: Telemetry
    ): Response<Unit>
}
