package com.example.habify.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

val LocalStarField = staticCompositionLocalOf<StarFieldState> {
    error("No StarFieldState provided")
}

@Composable
fun StarFieldProvider(
    state: StarFieldState,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalStarField provides state) {
        content()
    }
}
