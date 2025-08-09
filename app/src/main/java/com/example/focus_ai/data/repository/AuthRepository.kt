package com.example.focus_ai.data.repository

import com.example.focus_ai.data.api.SupabaseApi
import com.example.focus_ai.data.model.*
import com.example.focus_ai.presentation.util.Result
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class AuthRepository {
    
    companion object {
        private const val BASE_URL = "https://yxlpmsolfxvxhdebtixc.supabase.co/"
        // Supabase anonymous key - 공개되어도 안전한 키
        private const val SUPABASE_API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Inl4bHBtc29sZnh2eGhkZWJ0aXhjIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MjMxMTgzNjYsImV4cCI6MjAzODY5NDM2Nn0.t1oCY1keCcyD-m8KKFyj6B-j8VjbM5-X8oOvZDt5Izc"
    }
    
    private val api by lazy {
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
            
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SupabaseApi::class.java)
    }
    
    suspend fun verifyCode(code: String): Result<VerifyCodeResponse> {
        return try {
            println("DEBUG: Starting verifyCode with code: $code")
            println("DEBUG: Using BASE_URL: $BASE_URL")
            
            val request = VerifyCodeRequest(code)
            println("DEBUG: Created request: $request")
            
            val response = api.verifyCode(request)
            println("DEBUG: Got response with code: ${response.code()}")
            
            if (response.isSuccessful) {
                val body = response.body()
                println("DEBUG: Response body: $body")
                if (body != null) {
                    Result.Success(body)
                } else {
                    println("DEBUG: Response body is null")
                    Result.Failure("서버 응답이 올바르지 않습니다")
                }
            } else {
                val errorBody = response.errorBody()?.string()
                println("DEBUG: Error response body: $errorBody")
                val errorMessage = try {
                    val gson = com.google.gson.Gson()
                    val errorResponse = gson.fromJson(errorBody, ApiErrorResponse::class.java)
                    errorResponse.message ?: "알 수 없는 오류가 발생했습니다"
                } catch (e: Exception) {
                    println("DEBUG: Error parsing error response: ${e.message}")
                    "코드 검증에 실패했습니다 (HTTP ${response.code()})"
                }
                Result.Failure(errorMessage)
            }
        } catch (e: Exception) {
            println("DEBUG: Exception occurred: ${e.javaClass.simpleName} - ${e.message}")
            e.printStackTrace()
            val errorMessage = e.message ?: "알 수 없는 네트워크 오류가 발생했습니다"
            Result.Failure("네트워크 오류가 발생했습니다: $errorMessage")
        }
    }
    
    suspend fun startSession(jwtToken: String): Result<StartSessionResponse> {
        return try {
            val request = StartSessionRequest()
            val response = api.startSession("Bearer $jwtToken", request)
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.Success(body)
                } else {
                    Result.Failure("서버 응답이 올바르지 않습니다")
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = try {
                    val gson = com.google.gson.Gson()
                    val errorResponse = gson.fromJson(errorBody, ApiErrorResponse::class.java)
                    errorResponse.message ?: "세션 시작에 실패했습니다"
                } catch (e: Exception) {
                    "세션 시작에 실패했습니다 (HTTP ${response.code()})"
                }
                Result.Failure(errorMessage)
            }
        } catch (e: Exception) {
            val errorMessage = e.message ?: "알 수 없는 네트워크 오류가 발생했습니다"
            Result.Failure("네트워크 오류가 발생했습니다: $errorMessage")
        }
    }
}
