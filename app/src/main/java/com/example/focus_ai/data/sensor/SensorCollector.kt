package com.example.focus_ai.data.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.example.focus_ai.data.model.Telemetry
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlin.math.sqrt

class SensorCollector(private val context: Context) {
    
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)
    private val accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    
    private val _telemetryFlow = MutableSharedFlow<Telemetry>()
    val telemetryFlow: SharedFlow<Telemetry> = _telemetryFlow.asSharedFlow()
    
    private var collectingJob: Job? = null
    private var isRunning = false
    
    // 센서 데이터 저장소
    private val heartRateValues = mutableListOf<Float>()
    private val accelerometerRmsValues = mutableListOf<Float>()
    private val dataLock = Any()
    
    fun isRunning() = isRunning
    
    private val heartRateListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            if (event.sensor.type == Sensor.TYPE_HEART_RATE) {
                synchronized(dataLock) {
                    heartRateValues.add(event.values[0])
                }
            }
        }
        
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }
    
    private val accelerometerListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                val rms = sqrt(x * x + y * y + z * z) / 9.81f // g 단위로 변환
                
                synchronized(dataLock) {
                    accelerometerRmsValues.add(rms)
                }
            }
        }
        
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }
    
    fun start() {
        if (isRunning) return
        
        isRunning = true
        
        // 센서 등록
        heartRateSensor?.let {
            sensorManager.registerListener(
                heartRateListener,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
        
        accelerometerSensor?.let {
            sensorManager.registerListener(
                accelerometerListener,
                it,
                SensorManager.SENSOR_DELAY_GAME,
                2_000_000 // maxReportLatencyUs = 2초
            )
        }
        
        // 3초마다 집계 처리
        collectingJob = CoroutineScope(Dispatchers.IO).launch {
            while (isRunning) {
                delay(3000) // 3초 대기
                
                if (isRunning) {
                    processTelemetry()
                }
            }
        }
    }
    
    fun stop() {
        isRunning = false
        collectingJob?.cancel()
        
        sensorManager.unregisterListener(heartRateListener)
        sensorManager.unregisterListener(accelerometerListener)
        
        // 데이터 초기화
        synchronized(dataLock) {
            heartRateValues.clear()
            accelerometerRmsValues.clear()
        }
    }
    
    private suspend fun processTelemetry() {
        val currentTime = System.currentTimeMillis() / 1000 // Epoch seconds
        
        val (hrValues, accValues) = synchronized(dataLock) {
            val hr = heartRateValues.toList()
            val acc = accelerometerRmsValues.toList()
            heartRateValues.clear()
            accelerometerRmsValues.clear()
            Pair(hr, acc)
        }
        
        // 집계 계산
        val hrMean = hrValues.averageOrNaN().takeIf { !it.isNaN() }
        val hrStd = hrValues.stdOrNaN().takeIf { !it.isNaN() }
        val accRms = accValues.averageOrNaN().takeIf { !it.isNaN() }
        val accStd = accValues.stdOrNaN().takeIf { !it.isNaN() }
        
        val telemetry = Telemetry(
            timestamp = currentTime,
            heartRateMean = hrMean,
            heartRateStd = hrStd,
            accelerometerRms = accRms,
            accelerometerStd = accStd
        )
        
        _telemetryFlow.emit(telemetry)
    }
}
