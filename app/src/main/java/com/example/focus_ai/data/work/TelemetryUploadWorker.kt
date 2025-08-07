package com.example.focus_ai.data.work

import android.content.Context
import androidx.work.*
import com.example.focus_ai.data.api.TelemetryApi
import com.example.focus_ai.data.model.Telemetry
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

class TelemetryUploadWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    
    companion object {
        private const val KEY_TELEMETRY_JSON = "telemetry_json"
        private const val KEY_JWT_TOKEN = "jwt_token"
        private const val BASE_URL = "https://your-api-server.com/api/" // TODO: 실제 서버 URL로 변경
        
        fun createWorkRequest(telemetry: Telemetry, jwtToken: String): OneTimeWorkRequest {
            val gson = Gson()
            val inputData = Data.Builder()
                .putString(KEY_TELEMETRY_JSON, gson.toJson(telemetry))
                .putString(KEY_JWT_TOKEN, jwtToken)
                .build()
            
            return OneTimeWorkRequestBuilder<TelemetryUploadWorker>()
                .setInputData(inputData)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    5, // 5초부터 시작
                    TimeUnit.SECONDS
                )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()
        }
    }
    
    private val api by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TelemetryApi::class.java)
    }
    
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val telemetryJson = inputData.getString(KEY_TELEMETRY_JSON)
                ?: return@withContext Result.failure()
            
            val jwtToken = inputData.getString(KEY_JWT_TOKEN)
                ?: return@withContext Result.failure()
            
            val gson = Gson()
            val telemetry = gson.fromJson(telemetryJson, Telemetry::class.java)
            
            val response = api.uploadTelemetry(
                authorization = "Bearer $jwtToken",
                telemetry = telemetry
            )
            
            if (response.isSuccessful) {
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: IOException) {
            Result.retry()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
