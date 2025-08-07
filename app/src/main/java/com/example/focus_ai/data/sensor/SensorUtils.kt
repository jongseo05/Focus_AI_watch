package com.example.focus_ai.data.sensor

import kotlin.math.*

// 확장 함수들
fun Collection<Float>.averageOrNaN(): Float {
    return if (this.isEmpty()) Float.NaN else this.average().toFloat()
}

fun Collection<Float>.stdOrNaN(): Float {
    if (this.isEmpty() || this.size < 2) return Float.NaN
    val mean = this.average()
    val variance = this.map { (it - mean).pow(2) }.average()
    return sqrt(variance).toFloat()
}
