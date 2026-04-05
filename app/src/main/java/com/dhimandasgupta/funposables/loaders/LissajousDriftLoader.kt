package com.dhimandasgupta.funposables.loaders

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.math.pow
import kotlin.math.sin

/**
 * From https://composeinternals.com/composeloaders
 * */
@Preview
@Composable
fun LissajousDriftLoader(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "loader")

    val progress by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(durationMillis = 6000, easing = LinearEasing),
            RepeatMode.Restart
        ),
        label = "progress"
    )
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(durationMillis = 5400, easing = LinearEasing),
            RepeatMode.Restart
        ),
        label = "pulse"
    )

    Canvas(modifier = modifier.size(200.dp)) {
        val amp = 24.0f
        val ampBoost = 6.0f
        val aX = 3
        val bY = 4
        val phase = 1.57f
        val yScale = 0.92f
        val particleCount = 68
        val trailSpan = 0.34f
        val TWO_PI = 2f * Math.PI.toFloat()

        val pulseAngle = pulse * TWO_PI + 0.55f
        val detailScale = 0.52f + ((sin(pulseAngle) + 1f) / 2f) * 0.48f
        val a = amp + detailScale * ampBoost
        val scale = size.width / 100f

        // Draw trail path
        val path = androidx.compose.ui.graphics.Path()
        for (i in 0..480) {
            val t = (i / 480f) * TWO_PI
            val x = 50f + sin(aX * t + phase) * a
            val y = 50f + sin(bY * t) * (a * yScale)
            if (i == 0) path.moveTo(x * scale, y * scale) else path.lineTo(x * scale, y * scale)
        }
        drawPath(
            path,
            color = Color.White.copy(alpha = 0.1f),
            style = Stroke(width = 4.7f.dp.toPx(), cap = StrokeCap.Round)
        )

        // Draw particles
        for (i in 0 until particleCount) {
            val tailOffset = i.toFloat() / (particleCount - 1)
            val p = ((progress - tailOffset * trailSpan) % 1f + 1f) % 1f
            val t = p * TWO_PI
            val px = (50f + sin(aX * t + phase) * a) * scale
            val py = (50f + sin(bY * t) * (a * yScale)) * scale
            val fade = (1f - tailOffset).pow(0.56f)
            drawCircle(
                Color.White.copy(alpha = 0.04f + fade * 0.96f),
                radius = (0.9f + fade * 2.7f).dp.toPx(),
                center = Offset(px, py)
            )
        }
    }
}