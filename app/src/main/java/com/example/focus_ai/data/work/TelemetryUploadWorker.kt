package com.example.focus_ai.data.work

import android.content.Context
import androidx.work.*
import com.example.focus_ai.data.api.SupabaseApi
import com.example.focus_ai.data.model.SensorSampleRequest
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
        private const val KEY_SENSOR_DATA_JSON = "sensor_data_json"
        private const val KEY_JWT_TOKEN = "jwt_token"
        private const val BASE_URL = "https://yxlpmsolfxvxhdebtixc.supabase.co/"
        
        fun createWorkRequest(sensorData: SensorSampleRequest, jwtToken: String): OneTimeWorkRequest {
            val gson = Gson()
            val inputData = Data.Builder()
                .putString(KEY_SENSOR_DATA_JSON, gson.toJson(sensorData))
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
            .create(SupabaseApi::class.java)
    }
    
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val sensorDataJson = inputData.getString(KEY_SENSOR_DATA_JSON)
                ?: return@withContext Result.failure()
            
            val jwtToken = inputData.getString(KEY_JWT_TOKEN)
                ?: return@withContext Result.failure()
            
            val gson = Gson()
            val sensorData = gson.fromJson(sensorDataJson, SensorSampleRequest::class.java)
            
            val response = api.uploadSensorData(
                authorization = "Bearer $jwtToken",
                request = sensorData
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
