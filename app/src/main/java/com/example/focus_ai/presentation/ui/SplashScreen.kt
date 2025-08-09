package com.example.focus_ai.presentation.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.Button
import com.example.focus_ai.data.network.ApiService
import com.example.focus_ai.data.preferences.AuthPreferences
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToMain: () -> Unit
) {
    val context = LocalContext.current
    var connectionStatus by remember { mutableStateOf("서비스 연결 확인 중...") }
    val coroutineScope = rememberCoroutineScope()
    val apiService = remember { ApiService() }
    val authPreferences = remember { AuthPreferences(context) }
    
    val animatedAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 1500),
        label = "alpha_animation"
    )

    // 서비스 연결 및 인증 확인 함수
    fun checkConnectionAndAuth() {
        coroutineScope.launch {
            connectionStatus = "서비스 연결 확인 중..."
            
            val connected = apiService.checkServiceConnection()
            
            if (!connected) {
                // 서비스 연결 불가 - 로그인 화면으로
                connectionStatus = "오프라인 모드로 시작합니다."
                delay(1500)
                onNavigateToLogin()
                return@launch
            }
            
            // 서비스 연결됨 - 저장된 인증 정보 확인
            if (!authPreferences.isAuthenticated()) {
                // 로컬 인증 정보 없음 - 로그인 화면으로
                connectionStatus = "로그인이 필요합니다."
                delay(1500)
                onNavigateToLogin()
                return@launch
            }
            
            // 로컬 인증 정보는 있음 - 서버에서 세션 시작 시도로 유효성 확인
            connectionStatus = "인증 정보 확인 중..."
            val token = authPreferences.jwtToken ?: ""
            
            val sessionId = apiService.startSession(token)
            
            if (sessionId != null) {
                // 세션 시작 성공 - 인증 유효, 메인 화면으로
                connectionStatus = "인증 확인됨! 메인으로 이동합니다."
                authPreferences.sessionId = sessionId // 세션 ID 저장
                delay(1500)
                onNavigateToMain()
            } else {
                // 세션 시작 실패 - 인증 무효, 로컬 정보 삭제 후 로그인 화면으로
                connectionStatus = "인증이 만료되었습니다. 다시 로그인해주세요."
                authPreferences.clear() // 로컬 인증 정보 삭제
                delay(1500)
                onNavigateToLogin()
            }
        }
    }

    // 컴포넌트 시작 시 연결 및 인증 확인
    LaunchedEffect(Unit) {
        delay(1000) // 스플래시 애니메이션을 위한 지연
        checkConnectionAndAuth()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF3182F6), // Toss Blue
                        Color(0xFF4394FA), // Toss Blue Light
                        Color(0xFF6BA6FB)  // 더 밝은 Toss Blue
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 메인 로고 텍스트 - 웨어러블에 맞게 크기 축소
            Text(
                text = "Focus AI",
                fontSize = 24.sp, // 48sp에서 24sp로 축소
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(animatedAlpha)
            )

            Spacer(modifier = Modifier.height(8.dp)) // 간격 축소

            // 서브 텍스트 - 크기 축소
            Text(
                text = "AI와 함께하는 집중 시간",
                fontSize = 12.sp, // 16sp에서 12sp로 축소
                color = Color.White.copy(alpha = 0.8f), // 밝은 배경에 맞게 텍스트 색상 조정
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(animatedAlpha * 0.8f)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 상태 메시지
            Text(
                text = connectionStatus,
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .alpha(animatedAlpha * 0.9f)
                    .padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 로딩 인디케이터 (점 3개)
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                repeat(3) { index ->
                    val animatedScale by animateFloatAsState(
                        targetValue = if ((System.currentTimeMillis() / 500) % 3 == index.toLong()) 1.2f else 1f,
                        animationSpec = tween(300),
                        label = "dot_animation_$index"
                    )
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(
                                Color.White,
                                shape = androidx.compose.foundation.shape.CircleShape
                            )
                            .alpha(animatedAlpha)
                    )
                }
            }
        }
    }
}
