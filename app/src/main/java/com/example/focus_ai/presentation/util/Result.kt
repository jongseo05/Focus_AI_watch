package com.example.focus_ai.presentation.util

sealed class Result<out T> {
    data class Success<out T>(val value: T) : Result<T>()
    data class Failure(val message: String) : Result<Nothing>()
}
