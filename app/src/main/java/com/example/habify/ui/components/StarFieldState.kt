package com.example.habify.ui.components

import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

// Represents a normal star
data class Star(
    val x: Float,
    val y: Float,
    val radius: Float,
    val twinkleSpeed: Float,
    val twinkleOffset: Float
)

// Represents a shooting star
data class ShootingStar(
    var x: Float,
    var y: Float,
    val dx: Float,
    val dy: Float,
    val length: Float,
    val speed: Float
)

class StarFieldState(
    val stars: List<Star>,
    var shootingStar: ShootingStar? = null,
    var shootingStarTimer: Long = 0L
) {
    var twinklePhase by mutableStateOf(0f)
}

@Composable
fun rememberStarFieldState(
    starCount: Int = 120,
    screenWidthDp: Int = 360,
    screenHeightDp: Int = 640
): StarFieldState {
    val density = androidx.compose.ui.platform.LocalDensity.current

    val stars = remember {
        val wPx = with(density) { screenWidthDp.dp.toPx() }
        val hPx = with(density) { screenHeightDp.dp.toPx() }

        List(starCount) {
            Star(
                x = Random.nextFloat() * wPx,
                y = Random.nextFloat() * hPx,
                radius = Random.nextFloat() * 1.5f + 0.5f,
                twinkleSpeed = Random.nextFloat() * 0.5f + 0.5f,
                twinkleOffset = Random.nextFloat() * (2 * PI).toFloat()
            )
        }
    }

    return remember { StarFieldState(stars) }
}

fun StarFieldState.update(deltaTime: Float) {
    // Twinkle phase update
    twinklePhase += deltaTime * 2f

    // Shooting star spawn
    shootingStarTimer -= (deltaTime * 1000).toLong()
    if (shootingStar == null && shootingStarTimer <= 0) {
        val startX = Random.nextFloat() * 800f
        val startY = Random.nextFloat() * 200f
        shootingStar = ShootingStar(
            x = startX,
            y = startY,
            dx = 1f,
            dy = 0.4f,
            length = 80f,
            speed = Random.nextFloat() * 400f + 300f
        )
        shootingStarTimer = Random.nextLong(3000, 7000) // 3–7 sec
    }

    // Update shooting star position
    shootingStar?.let { s ->
        s.x += s.dx * s.speed * deltaTime
        s.y += s.dy * s.speed * deltaTime
        if (s.x > 1000f || s.y > 1000f) shootingStar = null
    }
}

fun DrawScope.drawStarField(state: StarFieldState) {
    // Draw normal stars
    state.stars.forEach { star ->
        val alpha = 0.5f + 0.5f * sin(state.twinklePhase * star.twinkleSpeed + star.twinkleOffset)
        drawCircle(
            color = Color.White.copy(alpha = alpha),
            radius = star.radius,
            center = Offset(star.x, star.y)
        )
    }
    // Draw shooting star if active
    state.shootingStar?.let { s ->
        drawLine(
            color = Color.White,
            start = Offset(s.x, s.y),
            end = Offset(s.x - s.length, s.y - s.length * (s.dy / s.dx)),
            strokeWidth = 2f
        )
    }
}
