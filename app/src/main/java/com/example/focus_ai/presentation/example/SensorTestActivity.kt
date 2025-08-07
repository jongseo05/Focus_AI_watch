package com.example.focus_ai.presentation.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.wear.compose.material.*
import com.example.focus_ai.domain.service.FocusSessionService
import kotlinx.coroutines.launch

/**
 * 센서 수집 기능 테스트를 위한 예시 Activity
 * 실제로는 기존의 MainScreenWrapper와 통합되어 사용됩니다.
 */
class SensorTestActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // 권한이 허용되면 센서 사용 가능
        } else {
            // 권한이 거부됨
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 센서 권한 확인 및 요청
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BODY_SENSORS
            ) == PackageManager.PERMISSION_GRANTED -> {
                // 권한이 이미 허용됨
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.BODY_SENSORS)
            }
        }

        setContent {
            SensorTestScreen()
        }
    }
}

@Composable
private fun SensorTestScreen() {
    val context = LocalContext.current
    val focusSessionService = remember { FocusSessionService(context) }
    var isSessionRunning by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isSessionRunning) "세션 진행 중..." else "세션 대기 중",
            modifier = Modifier.padding(bottom = 24.dp)
        )

        if (!isSessionRunning) {
            Button(
                onClick = {
                    coroutineScope.launch {
                        focusSessionService.startSession()
                        isSessionRunning = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text("▶ 시작")
            }
        } else {
            Button(
                onClick = {
                    coroutineScope.launch {
                        focusSessionService.stopSession()
                        isSessionRunning = false
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text("⏹ 종료")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "• 심박수와 가속도계 센서 데이터 수집\n" +
                    "• 3초마다 집계 후 서버 업로드\n" +
                    "• 배터리 절약을 위한 센서 배칭 활용"
        )
    }
}
