package com.example.focus_ai.domain.service

import android.content.Context
import androidx.work.WorkManager
import com.example.focus_ai.data.mapper.TelemetryMapper
import com.example.focus_ai.data.preferences.AuthPreferences
import com.example.focus_ai.data.repository.AuthRepository
import com.example.focus_ai.data.sensor.SensorCollector
import com.example.focus_ai.data.work.TelemetryUploadWorker
import com.example.focus_ai.presentation.util.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class FocusSessionService(private val context: Context) {
    
    private val sensorCollector = SensorCollector(context)
    private val workManager = WorkManager.getInstance(context)
    private val authPreferences = AuthPreferences(context)
    private val authRepository = AuthRepository()
    private var telemetryJob: Job? = null
    
    suspend fun startSession(): Result<String> {
        // 인증 확인
        val jwtToken = authPreferences.jwtToken
        if (jwtToken.isNullOrEmpty()) {
            return Result.Failure("인증이 필요합니다. 먼저 페어링 코드를 입력해주세요.")
        }
        
        // 기존 세션이 있으면 사용, 없으면 새로 시작
        var sessionId = authPreferences.sessionId
        if (sessionId.isNullOrEmpty()) {
            when (val sessionResult = authRepository.startSession(jwtToken)) {
                is Result.Success -> {
                    sessionId = sessionResult.value.sessionId
                    authPreferences.sessionId = sessionId
                    authPreferences.isConnected = true
                }
                is Result.Failure -> {
                    return Result.Failure("세션 시작에 실패했습니다: ${sessionResult.message}")
                }
            }
        }
        
        // 센서 수집 시작
        sensorCollector.start()
        
        // 텔레메트리 업로드 작업 설정
        telemetryJob = sensorCollector.telemetryFlow
            .onEach { telemetry ->
                // Telemetry를 SensorSampleRequest로 변환
                val sensorData = TelemetryMapper.mapToSensorSampleRequest(telemetry, sessionId!!)
                val workRequest = TelemetryUploadWorker.createWorkRequest(sensorData, jwtToken)
                workManager.enqueue(workRequest)
            }
            .launchIn(CoroutineScope(Dispatchers.IO))
        
        return Result.Success("세션이 시작되었습니다")
    }
    
    fun stopSession() {
        // 센서 수집 중지
        sensorCollector.stop()
        
        // 텔레메트리 작업 중지
        telemetryJob?.cancel()
        telemetryJob = null
        
        // 대기 중인 워크 취소
        workManager.cancelAllWork()
    }
    
    fun isRunning(): Boolean {
        return sensorCollector.isRunning()
    }
}
