package com.example.focus_ai.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Text

@Composable
fun CodeBoxInput(
    code: String,
    onCodeChange: (String) -> Unit,
    obscured: Boolean = false,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        // 숨겨진 텍스트 필드 (실제 키 입력 담당)
        BasicTextField(
            value = code,
            onValueChange = { new ->
                if (new.length <= 4 && new.all { it.isDigit() }) {
                    onCodeChange(new)
                }
            },
            decorationBox = { }, // 화면에는 그리지 않음
            modifier = Modifier
                .size(0.dp) // 보이지 않게
                .focusRequester(focusRequester)
        )
        
        // 시각 박스 - 밝은 배경에 맞게 색상 조정
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            modifier = Modifier.wrapContentWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(4) { index ->
                val filled = index < code.length
                val isFocused = index == code.length && code.length < 4
                
                Box(
                    modifier = Modifier
                        .size(32.dp) // 28dp → 32dp로 약간 키움
                        .clip(RoundedCornerShape(6.dp)) // 4dp → 6dp로 복원
                        .border(
                            width = 2.dp, // 1.5dp → 2dp로 복원
                            color = when {
                                isFocused -> Color.White // 현재 포커스 - 흰색
                                filled -> Color.White.copy(alpha = 0.9f) // 입력됨 - 반투명 흰색
                                else -> Color.White.copy(alpha = 0.4f) // 미입력 - 더 투명한 흰색
                            },
                            shape = RoundedCornerShape(6.dp)
                        )
                        .background(
                            color = when {
                                isFocused -> Color.White.copy(alpha = 0.3f)
                                filled -> Color.White.copy(alpha = 0.2f)
                                else -> Color.White.copy(alpha = 0.1f)
                            },
                            shape = RoundedCornerShape(6.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (filled) {
                        Text(
                            text = if (obscured) "•" else code[index].toString(),
                            fontSize = 18.sp, // 16sp → 18sp로 약간 키움
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
    
    // 자동 포커스
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}
