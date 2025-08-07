package com.example.focus_ai.presentation.ui

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.example.focus_ai.domain.service.FocusSessionService
import com.example.focus_ai.presentation.model.FocusSessionState
import com.example.focus_ai.presentation.util.VibratorWrapper
import kotlinx.coroutines.*

@Composable
fun MainScreenWrapper() {
    val sessionState = remember { mutableStateOf(FocusSessionState.Idle) }
    val elapsedTime = remember { mutableStateOf(0) }
    var timerJob by remember { mutableStateOf<Job?>(null) }
    val context = LocalContext.current
    
    // 센서 서비스 초기화
    val focusSessionService = remember { FocusSessionService(context) }
    
    // Lifecycle-aware CoroutineScope 사용
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = remember { lifecycleOwner.lifecycleScope }

    // VibratorWrapper 초기화
    LaunchedEffect(Unit) {
        VibratorWrapper.init(context)
    }

    MainScreen(
        sessionState = sessionState.value,
        elapsedTime = elapsedTime.value,
        onStart = {
            VibratorWrapper.click() // 시작 햅틱 (50ms)
            sessionState.value = FocusSessionState.Running
            elapsedTime.value = 0
            
            // 센서 수집 및 업로드 시작
            focusSessionService.startSession()
            
            timerJob?.cancel()
            timerJob = coroutineScope.launch {
                while (isActive && sessionState.value == FocusSessionState.Running) {
                    delay(1000)
                    elapsedTime.value += 1
                }
            }
        },
        onPause = {
            VibratorWrapper.click() // 일시정지 햅틱 (50ms)
            sessionState.value = FocusSessionState.Paused
            timerJob?.cancel()
        },
        onResume = {
            VibratorWrapper.click() // 재개 햅틱 (50ms)
            sessionState.value = FocusSessionState.Running
            timerJob = coroutineScope.launch {
                while (isActive && sessionState.value == FocusSessionState.Running) {
                    delay(1000)
                    elapsedTime.value += 1
                }
            }
        },
        onStop = {
            VibratorWrapper.error() // 종료 햅틱 (200ms)
            sessionState.value = FocusSessionState.Idle
            elapsedTime.value = 0
            
            // 센서 수집 및 업로드 중지
            focusSessionService.stopSession()
            
            timerJob?.cancel()
        }
    )

    DisposableEffect(Unit) {
        onDispose {
            timerJob?.cancel()
            focusSessionService.stopSession()
        }
    }
}
