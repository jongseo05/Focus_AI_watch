package com.example.focus_ai.presentation.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.*
import com.example.focus_ai.presentation.model.FocusSessionState
import com.example.focus_ai.presentation.ui.components.FocusRing

@Composable
fun MainScreen(
    sessionState: FocusSessionState,
    elapsedTime: Int,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit,
    onDisconnect: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF2F80ED), // Toss Blue
                        Color(0xFF4394FA), 
                        Color(0xFF6BA6FB)
                    )
                )
            )
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize().padding(16.dp)
        ) {
            // 중심 - 원형 진행률과 버튼들이 포함된 영역
            Box(
                modifier = Modifier.size(160.dp),
                contentAlignment = Alignment.Center
            ) {
                // 집중도를 표시하는 원형 진행률 링
                val focusProgress = when(sessionState) {
                    FocusSessionState.Running -> {
                        // 예시: 60초마다 한 바퀴 (실제로는 목표 시간 기준)
                        (elapsedTime % 60) / 60f
                    }
                    FocusSessionState.Paused -> {
                        (elapsedTime % 60) / 60f // 일시정지 상태에서도 현재 진행률 유지
                    }
                    else -> 0f
                }
                
                // FocusRing 컴포넌트 사용
                FocusRing(
                    progress = focusProgress,
                    modifier = Modifier.size(160.dp),
                    ringWidth = 10.dp
                )
                
                // 중앙 내용 영역 (깔끔한 흰색 원)
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.95f)), // 약간 더 불투명하게
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // 타이머 텍스트
                        Text(
                            text = formatTime(elapsedTime),
                            color = Color(0xFF2F80ED),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        // 상태별 버튼 배치
                        when (sessionState) {
                            FocusSessionState.Idle -> {
                                // 시작 버튼
                                Button(
                                    onClick = onStart,
                                    modifier = Modifier.size(48.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        backgroundColor = Color(0xFF2F80ED)
                                    )
                                ) {
                                    Icon(
                                        Icons.Default.PlayArrow,
                                        contentDescription = "시작",
                                        tint = Color.White,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }
                            
                            FocusSessionState.Running -> {
                                // 일시정지와 중지 버튼
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = onPause,
                                        modifier = Modifier.size(38.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            backgroundColor = Color(0xFFFF6B35)
                                        )
                                    ) {
                                        Icon(
                                            Icons.Default.Pause,
                                            contentDescription = "일시정지",
                                            tint = Color.White,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    
                                    Button(
                                        onClick = onStop,
                                        modifier = Modifier.size(38.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            backgroundColor = Color(0xFF6C757D)
                                        )
                                    ) {
                                        Icon(
                                            Icons.Default.Stop,
                                            contentDescription = "중지",
                                            tint = Color.White,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                            }
                            
                            FocusSessionState.Paused -> {
                                // 재개와 중지 버튼
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = onResume,
                                        modifier = Modifier.size(38.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            backgroundColor = Color(0xFF2F80ED)
                                        )
                                    ) {
                                        Icon(
                                            Icons.Default.PlayArrow,
                                            contentDescription = "재개",
                                            tint = Color.White,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    
                                    Button(
                                        onClick = onStop,
                                        modifier = Modifier.size(38.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            backgroundColor = Color(0xFF6C757D)
                                        )
                                    ) {
                                        Icon(
                                            Icons.Default.Stop,
                                            contentDescription = "중지",
                                            tint = Color.White,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // 하단 연결 해제 버튼 - 세션이 중지된 상태에서만 표시
        if (sessionState == FocusSessionState.Idle) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = onDisconnect,
                    modifier = Modifier.padding(4.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color.Transparent
                    )
                ) {
                    Text(
                        text = "연결 해제",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

private fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val secs = seconds % 60
    return String.format("%02d:%02d", minutes, secs)
}
