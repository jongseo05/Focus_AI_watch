package com.example.focus_ai.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.focus_ai.data.repository.FocusRepository
import kotlinx.coroutines.launch

class MainViewModel(private val repository: FocusRepository) : ViewModel() {
    
    fun authenticateWithCode(code: String, deviceId: String = "watch-${System.currentTimeMillis()}") {
        viewModelScope.launch {
            val success = repository.authenticateWithCode(code, deviceId)
            if (success) {
                // 인증 성공
                println("Authentication successful")
                startSession()
            } else {
                // 인증 실패
                println("Authentication failed")
            }
        }
    }
    
    private fun startSession() {
        viewModelScope.launch {
            val success = repository.startFocusSession()
            if (success) {
                println("Focus session started")
            } else {
                println("Failed to start focus session")
            }
        }
    }
    
    fun stopSession() {
        repository.stopFocusSession()
        println("Focus session stopped")
    }
    
    fun isSessionActive(): Boolean {
        return repository.isSessionActive()
    }
}
