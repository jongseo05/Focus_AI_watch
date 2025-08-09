package com.example.focus_ai.presentation.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.*
import com.example.focus_ai.data.repository.AuthRepository
import com.example.focus_ai.presentation.util.Result
import kotlinx.coroutines.launch

/**
 * 네트워크 연결 테스트를 위한 Activity
 */
class NetworkTestActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            NetworkTestScreen()
        }
    }
}

@Composable
private fun NetworkTestScreen() {
    var testResult by remember { mutableStateOf("테스트 준비 중...") }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val authRepository = remember { AuthRepository() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = testResult,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Button(
            onClick = {
                coroutineScope.launch {
                    isLoading = true
                    testResult = "네트워크 테스트 중..."
                    
                    // 테스트 코드로 API 호출
                    when (val result = authRepository.verifyCode("0000")) {
                        is Result.Success -> {
                            testResult = "✅ 연결 성공!\nJWT: ${result.value.jwt.take(20)}..."
                        }
                        is Result.Failure -> {
                            testResult = "❌ 연결 실패:\n${result.message}"
                        }
                    }
                    isLoading = false
                }
            },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text(if (isLoading) "테스트 중..." else "🔄 네트워크 테스트")
        }
    }
}
