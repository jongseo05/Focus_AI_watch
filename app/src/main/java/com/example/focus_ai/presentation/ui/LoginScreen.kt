package com.example.focus_ai.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.*
import kotlinx.coroutines.launch

// 페어링 코드 검증 함수 (실제 구현에서는 네트워크 요청)
fun validatePairingCode(code: String, callback: (token: String?, error: String?) -> Unit) {
    // 시뮬레이션: 코드가 "123456"이면 성공, 아니면 실패
    if (code == "123456") {
        callback("auth_token_example", null)
    } else {
        callback(null, "잘못된 페어링 코드입니다")
    }
}

@Composable
fun PairingScreen(
    onSuccess: (String) -> Unit
) {
    var pairingCode by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF3182F6), // Toss Blue
                        Color(0xFF4394FA), // Toss Blue Light
                        Color(0xFF6BA6FB)  // 더 밝은 Toss Blue
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 제목
            Text(
                text = "페어링 코드 입력",
                fontSize = 14.sp, // 크기 축소 (18sp -> 14sp)
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp)) // 간격도 축소

            Text(
                text = "웹에서 받은 6자리 코드를\n입력하세요",
                fontSize = 10.sp, // 크기 축소 (12sp -> 10sp)
                color = Color.White.copy(alpha = 0.8f), // 밝은 배경에 맞게 색상 조정
                textAlign = TextAlign.Center,
                lineHeight = 12.sp // 줄간격도 축소
            )

            Spacer(modifier = Modifier.height(16.dp)) // 간격 축소

            // 6자리 개별 입력 박스들 (모서리 둥근 사각형) - 간격 늘려서 6개 다 보이게
            Row(
                horizontalArrangement = Arrangement.spacedBy(3.dp), // 간격 줄임
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(6) { index ->
                    Card(
                        onClick = { 
                            // 박스 클릭 시 자동으로 123456 입력하고 연결
                            if (pairingCode.isEmpty() && !isLoading) {
                                pairingCode = "123456"
                                isLoading = true
                                errorMessage = null
                                
                                coroutineScope.launch {
                                    validatePairingCode(pairingCode) { token, error ->
                                        isLoading = false
                                        if (token != null) {
                                            onSuccess(token)
                                        } else {
                                            errorMessage = error
                                        }
                                    }
                                }
                            }
                        },
                        modifier = Modifier.size(24.dp), // 크기도 좀 줄임
                        backgroundPainter = ColorPainter(
                            if (index < pairingCode.length) {
                                Color.White.copy(alpha = 0.4f) // 입력된 칸
                            } else if (index == pairingCode.length) {
                                Color(0xFF3182F6).copy(alpha = 0.8f) // 현재 입력 칸 (Toss Blue)
                            } else {
                                Color.White.copy(alpha = 0.15f) // 비어있는 칸
                            }
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (index < pairingCode.length) pairingCode[index].toString() else "",
                                fontSize = 12.sp, // 폰트도 줄임
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 에러 메시지 표시
            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    fontSize = 11.sp,
                    color = Color(0xFFFF4444),
                    textAlign = TextAlign.Center,
                    lineHeight = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // 로딩 상태 표시
            if (isLoading) {
                CircularProgressIndicator(
                    indicatorColor = Color.White,
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            }
        }
    }
}
