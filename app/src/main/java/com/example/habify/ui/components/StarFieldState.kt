package com.example.habify.ui.components

import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

data class Star(
    val x: Float,
    val y: Float,
    val radius: Float,
    val twinkleSpeed: Float,
    val twinkleOffset: Float
)

data class ShootingStar(
    var x: Float,
    var y: Float,
    val dx: Float,
    val dy: Float,
    val length: Float,
    val speed: Float
)

class StarFieldState(
    val stars: MutableList<Star>,
    var shootingStar: ShootingStar? = null,
    var shootingStarTimer: Long = 0L
) {
    var twinklePhase by mutableFloatStateOf(0f)
    var screenWidth by mutableFloatStateOf(0f)
    var screenHeight by mutableFloatStateOf(0f)
}

@Composable
fun rememberStarFieldState(
    starCount: Int = 120
): StarFieldState {
    val stars = remember {
        MutableList(starCount) {
            Star(
                x = 0f,
                y = 0f,
                radius = Random.nextFloat() * 1.5f + 0.5f,
                twinkleSpeed = Random.nextFloat() * 0.5f + 0.5f,
                twinkleOffset = Random.nextFloat() * (2 * PI).toFloat()
            )
        }
    }
    return remember { StarFieldState(stars) }
}

fun StarFieldState.update(deltaTime: Float) {
    twinklePhase += deltaTime * 2f

    shootingStarTimer -= (deltaTime * 1000).toLong()
    if (shootingStar == null && shootingStarTimer <= 0 && screenWidth > 0 && screenHeight > 0) {
        val startX = Random.nextFloat() * screenWidth
        val startY = Random.nextFloat() * screenHeight
        shootingStar = ShootingStar(
            x = startX,
            y = startY,
            dx = 1f,
            dy = 0.4f,
            length = 80f,
            speed = Random.nextFloat() * 400f + 300f
        )
        shootingStarTimer = Random.nextLong(3000, 7000)
    }

    shootingStar?.let { s ->
        s.x += s.dx * s.speed * deltaTime
        s.y += s.dy * s.speed * deltaTime
        if (s.x > screenWidth || s.y > screenHeight) shootingStar = null
    }
}

fun DrawScope.drawStarField(state: StarFieldState) {
    if (state.screenWidth > 0 && state.screenHeight > 0) {
        state.stars.forEachIndexed { i, star ->
            if (star.x == 0f && star.y == 0f) {
                state.stars[i] = star.copy(
                    x = Random.nextFloat() * state.screenWidth,
                    y = Random.nextFloat() * state.screenHeight
                )
            }
        }
    }

    state.stars.forEach { star ->
        val alpha =
            0.5f + 0.5f * sin(state.twinklePhase * star.twinkleSpeed + star.twinkleOffset)
        drawCircle(
            color = Color.White.copy(alpha = alpha),
            radius = star.radius,
            center = Offset(star.x, star.y)
        )
    }

    state.shootingStar?.let { s ->
        drawLine(
            color = Color.White,
            start = Offset(s.x, s.y),
            end = Offset(s.x - s.length, s.y - s.length * (s.dy / s.dx)),
            strokeWidth = 2f
        )
    }
}
