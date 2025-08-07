package com.example.focus_ai.data.model

import com.google.gson.annotations.SerializedName

data class Telemetry(
    @SerializedName("ts")
    val timestamp: Long, // Epoch seconds
    
    @SerializedName("hr_mean")
    val heartRateMean: Float? = null,
    
    @SerializedName("hr_std")
    val heartRateStd: Float? = null,
    
    @SerializedName("acc_rms")
    val accelerometerRms: Float? = null,
    
    @SerializedName("acc_std")
    val accelerometerStd: Float? = null
)
