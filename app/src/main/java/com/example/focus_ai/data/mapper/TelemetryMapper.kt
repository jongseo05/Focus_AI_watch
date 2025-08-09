package com.example.focus_ai.data.mapper

import com.example.focus_ai.data.model.SensorSampleRequest
import com.example.focus_ai.data.model.Telemetry
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

object TelemetryMapper {
    
    private val isoDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    
    /**
     * Telemetry 데이터를 SensorSampleRequest로 변환
     */
    fun mapToSensorSampleRequest(
        telemetry: Telemetry,
        sessionId: String
    ): SensorSampleRequest {
        return SensorSampleRequest(
            sessionId = sessionId,
            heartRate = telemetry.heartRateMean?.roundToInt(),
            steps = calculateSteps(telemetry), // 가속도계 데이터로부터 걸음수 추정
            activityLevel = calculateActivityLevel(telemetry),
            timestamp = convertTimestampToIso(telemetry.timestamp),
            deviceType = "watch"
        )
    }
    
    /**
     * 가속도계 데이터로부터 걸음수를 추정
     * 실제 구현에서는 더 정교한 알고리즘이 필요할 수 있습니다.
     */
    private fun calculateSteps(telemetry: Telemetry): Int? {
        val accRms = telemetry.accelerometerRms ?: return null
        
        // 간단한 걸음수 추정: RMS 값이 일정 임계값을 넘으면 걸음으로 간주
        // 3초 집계 주기에서 보통 0-10걸음 정도 예상
        return when {
            accRms > 15.0f -> (accRms / 2.0f).roundToInt().coerceIn(0, 10)
            accRms > 10.0f -> (accRms / 3.0f).roundToInt().coerceIn(0, 5)
            accRms > 5.0f -> 1
            else -> 0
        }
    }
    
    /**
     * 가속도계 데이터로부터 활동량 레벨을 계산
     */
    private fun calculateActivityLevel(telemetry: Telemetry): String {
        val accRms = telemetry.accelerometerRms ?: return "low"
        val accStd = telemetry.accelerometerStd ?: return "low"
        
        // RMS와 표준편차를 조합하여 활동량 레벨 결정
        val activityScore = accRms + (accStd * 2)
        
        return when {
            activityScore > 20.0f -> "high"
            activityScore > 10.0f -> "medium"
            else -> "low"
        }
    }
    
    /**
     * Epoch seconds를 ISO 8601 형식으로 변환
     */
    private fun convertTimestampToIso(epochSeconds: Long): String {
        val date = Date(epochSeconds * 1000) // milliseconds로 변환
        return isoDateFormat.format(date)
    }
}
