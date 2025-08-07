package com.example.focus_ai.presentation.ui

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext

enum class AppState {
    SPLASH,
    LOGIN,
    MAIN
}

@Composable
fun AppNavigation() {
    var currentState by remember { mutableStateOf(AppState.SPLASH) }

    when (currentState) {
        AppState.SPLASH -> {
            SplashScreen(
                onNavigateToLogin = {
                    currentState = AppState.LOGIN
                }
            )
        }

        AppState.LOGIN -> {
            NewPairingScreen(
                onSuccess = { token ->
                    // 토큰을 받으면 메인 화면으로 이동
                    // 실제 앱에서는 토큰을 저장하고 인증 상태를 관리해야 함
                    println("인증 토큰 받음: $token")
                    currentState = AppState.MAIN
                }
            )
        }

        AppState.MAIN -> {
            // 기존 MainScreen 컴포넌트를 여기에 통합
            MainScreenWrapper()
        }
    }
}
