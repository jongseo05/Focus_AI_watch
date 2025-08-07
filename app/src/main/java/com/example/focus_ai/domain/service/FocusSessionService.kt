package com.example.focus_ai.domain.service

import android.content.Context
import androidx.work.WorkManager
import com.example.focus_ai.data.sensor.SensorCollector
import com.example.focus_ai.data.work.TelemetryUploadWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class FocusSessionService(private val context: Context) {
    
    companion object {
        // TODO: 실제 JWT 토큰으로 교체하거나 로그인 시스템에서 가져오도록 수정
        private const val DEFAULT_JWT_TOKEN = "your_jwt_token_here"
    }
    
    private val sensorCollector = SensorCollector(context)
    private val workManager = WorkManager.getInstance(context)
    private var telemetryJob: Job? = null
    
    fun startSession(jwtToken: String = DEFAULT_JWT_TOKEN) {
        // 센서 수집 시작
        sensorCollector.start()
        
        // 텔레메트리 업로드 작업 설정
        telemetryJob = sensorCollector.telemetryFlow
            .onEach { telemetry ->
                val workRequest = TelemetryUploadWorker.createWorkRequest(telemetry, jwtToken)
                workManager.enqueue(workRequest)
            }
            .launchIn(CoroutineScope(Dispatchers.IO))
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
