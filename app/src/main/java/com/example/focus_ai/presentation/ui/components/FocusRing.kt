package com.example.focus_ai.presentation.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.wear.tooling.preview.devices.WearDevices

@Composable
fun FocusRing(
    progress: Float,
    modifier: Modifier = Modifier,
    ringWidth: Dp = 10.dp
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(
            durationMillis = 500,
            easing = FastOutSlowInEasing
        ),
        label = "focus_ring_progress"
    )
    
    val ringColor = when {
        progress <= 0.50f -> Color(0xFFFF4D4D) // Red
        progress <= 0.80f -> Color(0xFFFFC107) // Amber
        else -> Color(0xFF1E88E5) // Bright Blue
    }
    
    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val center = size.minDimension / 2
            val radius = center - ringWidth.toPx() / 2
            
            // Fixed inner disk
            drawCircle(
                color = Color(0xFFF5FAFF),
                radius = radius - ringWidth.toPx() / 2,
                center = androidx.compose.ui.geometry.Offset(
                    size.width / 2,
                    size.height / 2
                )
            )
            
            // Animated outer arc ring
            drawArc(
                color = ringColor,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                style = Stroke(
                    width = ringWidth.toPx(),
                    cap = StrokeCap.Round
                )
            )
        }
    }
}

@Preview(device = WearDevices.SMALL_ROUND)
@Composable
fun FocusRingPreview() {
    FocusRing(
        progress = 0.86f,
        modifier = Modifier.size(160.dp)
    )
}
