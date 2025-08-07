package com.example.focus_ai.presentation.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Text
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToLogin: () -> Unit
) {
    val animatedAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 1500),
        label = "alpha_animation"
    )

    LaunchedEffect(Unit) {
        delay(3000) // 3초 후 로그인 화면으로 이동
        onNavigateToLogin()
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

            Spacer(modifier = Modifier.height(20.dp)) // 간격 축소

            // 로딩 인디케이터 (점 3개) - 크기 축소
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp) // 간격 축소
            ) {
                repeat(3) { index ->
                    val animatedScale by animateFloatAsState(
                        targetValue = if ((System.currentTimeMillis() / 500) % 3 == index.toLong()) 1.2f else 1f,
                        animationSpec = tween(300),
                        label = "dot_animation_$index"
                    )
                    Box(
                        modifier = Modifier
                            .size(6.dp) // 크기 축소 (8dp -> 6dp)
                            .background(
                                Color.White, // 밝은 배경에 맞게 흰색으로 변경
                                shape = androidx.compose.foundation.shape.CircleShape
                            )
                            .alpha(animatedAlpha)
                    )
                }
            }
        }
    }
}
