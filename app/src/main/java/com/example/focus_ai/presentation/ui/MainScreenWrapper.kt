package com.example.focus_ai.presentation.ui

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.example.focus_ai.data.repository.FocusRepository
import com.example.focus_ai.domain.service.FocusSessionService
import com.example.focus_ai.presentation.model.FocusSessionState
import com.example.focus_ai.presentation.util.VibratorWrapper
import kotlinx.coroutines.*

@Composable
fun MainScreenWrapper(
    onDisconnect: (() -> Unit)? = null
) {
    val sessionState = remember { mutableStateOf(FocusSessionState.Idle) }
    val elapsedTime = remember { mutableStateOf(0) }
    var timerJob by remember { mutableStateOf<Job?>(null) }
    val context = LocalContext.current
    
    // 센서 서비스와 Repository 초기화
    val focusSessionService = remember { FocusSessionService(context) }
    val focusRepository = remember { FocusRepository(context) }
    
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
            coroutineScope.launch {
                focusSessionService.startSession()
            }
            
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
        },
        onDisconnect = {
            VibratorWrapper.click()
            
            // 모든 세션 정리
            sessionState.value = FocusSessionState.Idle
            elapsedTime.value = 0
            timerJob?.cancel()
            focusSessionService.stopSession()
            
            // 인증 정보 삭제 및 연결 해제
            focusRepository.disconnect()
            
            // 상위 콜백 호출 (로그인 화면으로 이동)
            onDisconnect?.invoke()
        }
    )

    DisposableEffect(Unit) {
        onDispose {
            timerJob?.cancel()
            focusSessionService.stopSession()
        }
    }
}
