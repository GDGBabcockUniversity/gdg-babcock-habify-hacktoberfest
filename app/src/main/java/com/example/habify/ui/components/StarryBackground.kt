package com.example.habify.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun StarryBackground(content: @Composable () -> Unit) {
    val state = LocalStarField.current
    var lastTime by remember { mutableStateOf(System.nanoTime()) }

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val now = System.nanoTime()
            val deltaTime = (now - lastTime) / 1_000_000_000f
            lastTime = now

            // <-- This line ensures the whole screen is dark under your UI
            drawRect(Color.Black)

            state.update(deltaTime)
            drawStarField(state)
        }
        content()
    }
}
