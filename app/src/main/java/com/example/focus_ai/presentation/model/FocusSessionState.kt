package com.example.focus_ai.presentation.model

enum class FocusSessionState(val label: String) {
    Idle("집중 시작 전"),
    Running("집중 세션 진행 중"),
    Paused("일시정지됨")
}
