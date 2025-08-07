package com.example.focus_ai.presentation.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.*

@Composable
fun FocusButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .height(40.dp)
            .defaultMinSize(minWidth = 80.dp),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = Color(0xFF2F80ED),
            contentColor = Color.White
        )
    ) {
        Text(
            text = text,
            fontSize = 12.sp
        )
    }
}
