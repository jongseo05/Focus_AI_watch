package com.example.focus_ai.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.*
import com.example.focus_ai.data.preferences.AuthPreferences
import com.example.focus_ai.data.repository.FocusRepository
import com.example.focus_ai.presentation.ui.components.CodeBoxInput
import com.example.focus_ai.presentation.util.Result
import com.example.focus_ai.presentation.util.VibratorWrapper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// 페어링 코드 검증 함수 (실제 Supabase API와 연동)
suspend fun validatePairingCode(code: String, context: android.content.Context): Result<String> {
    return try {
        println("DEBUG NewPairingScreen: Starting validation for code: $code")
        
        val authPreferences = AuthPreferences(context)
        val focusRepository = FocusRepository(context)
        val deviceId = "watch-${System.currentTimeMillis()}"
        
        println("DEBUG NewPairingScreen: Created FocusRepository, deviceId: $deviceId")
        
        val success = focusRepository.authenticateWithCode(code, deviceId)
        
        if (success) {
            println("DEBUG NewPairingScreen: Authentication successful")
            
            // 인증 정보 저장
            authPreferences.isConnected = true
            
            // 세션도 바로 시작
            val sessionStarted = focusRepository.startFocusSession()
            println("DEBUG NewPairingScreen: Session start result: $sessionStarted")
            
            Result.Success("authentication_successful")
        } else {
            println("DEBUG NewPairingScreen: Authentication failed")
            Result.Failure("잘못된 코드입니다")
        }
    } catch (e: Exception) {
        println("DEBUG NewPairingScreen: Exception caught: ${e.message}")
        e.printStackTrace()
        Result.Failure("예상치 못한 오류: ${e.message}")
    }
}

@Composable
fun NewPairingScreen(
    onSuccess: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var code by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    
    // VibratorWrapper 초기화
    LaunchedEffect(Unit) {
        VibratorWrapper.init(context)
    }

    // 4자리 입력 시 자동 검증
    LaunchedEffect(code) {
        if (code.length == 4 && !loading) {
            loading = true
            errorMsg = null
            VibratorWrapper.click()
            
            when (val result = validatePairingCode(code, context)) {
                is Result.Success -> {
                    VibratorWrapper.success()
                    onSuccess(result.value)
                }
                is Result.Failure -> {
                    VibratorWrapper.error()
                    loading = false
                    errorMsg = result.message
                    delay(1500) // 에러 메시지 표시 시간
                    code = "" // 실패 시 코드 초기화
                    errorMsg = null
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF2F80ED), // Toss Blue
                        Color(0xFF4394FA), // Toss Blue Light  
                        Color(0xFF6BA6FB)  // 더 밝은 Toss Blue
                    )
                )
            )
            .pointerInput(Unit) { 
                detectTapGestures { 
                    // 키패드 유지를 위한 터치 감지
                } 
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp) // 상단 패딩 줄임
                .wrapContentHeight()
                .align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp) // 간격 줄임
        ) {
            // 헤더 텍스트 - 한줄에 다 들어가게 축소
            Text(
                text = "4자리 페어링 코드",
                color = Color.White,
                fontSize = 14.sp, // 16sp → 14sp로 축소
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "웹에서 받은 코드를 입력하세요",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 10.sp, // 11sp → 10sp로 축소
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp)) // 간격 줄임

            // 코드 입력 박스
            CodeBoxInput(
                code = code,
                onCodeChange = { newCode ->
                    if (!loading) { // 로딩 중이 아닐 때만 입력 허용
                        code = newCode
                        errorMsg = null // 입력 시 에러 메시지 제거
                        VibratorWrapper.click() // 입력 피드백
                    }
                },
                obscured = false
            )

            // 상태 표시 영역
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp), // 높이 줄임
                contentAlignment = Alignment.Center
            ) {
                when {
                    loading -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp) // 간격 줄임
                        ) {
                            CircularProgressIndicator(
                                indicatorColor = Color.White,
                                modifier = Modifier.size(18.dp), // 크기 줄임
                                strokeWidth = 2.dp
                            )
                            Text(
                                text = "확인 중...",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 9.sp // 폰트 크기 줄임
                            )
                        }
                    }
                    errorMsg != null -> {
                        Text(
                            text = errorMsg!!,
                            color = Color(0xFFFFE5E5),
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    code.isNotEmpty() -> {
                        Text(
                            text = "${code.length}/4", // 4자리로 변경
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 9.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NewPairingScreenPreview() {
    NewPairingScreen(
        onSuccess = { }
    )
}
