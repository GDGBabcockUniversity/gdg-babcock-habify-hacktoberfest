package com.example.habify.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

import androidx.compose.runtime.*

@Composable
fun StarryBackground(content: @Composable () -> Unit) {
    val state = rememberStarFieldState()
    val frameTime = remember { mutableStateOf(System.nanoTime()) }

    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
        val now = System.nanoTime()
        val deltaTime = (now - frameTime.value) / 1_000_000_000f
        frameTime.value = now

        state.update(deltaTime)
        drawStarField(state)
    }

    content()
}
