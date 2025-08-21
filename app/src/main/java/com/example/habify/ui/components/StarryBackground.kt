package com.example.habify.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged

@Composable
fun StarryBackground(content: @Composable () -> Unit) {
    val state = LocalStarField.current
    var lastTime by remember { mutableStateOf(System.nanoTime()) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { size ->
                state.screenWidth = size.width.toFloat()
                state.screenHeight = size.height.toFloat()
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val now = System.nanoTime()
            val deltaTime = (now - lastTime) / 1_000_000_000f
            lastTime = now

            // Fill background with black
            drawRect(Color.Black)

            // Update + draw stars
            state.update(deltaTime)
            drawStarField(state)
        }
        content()
    }
}
