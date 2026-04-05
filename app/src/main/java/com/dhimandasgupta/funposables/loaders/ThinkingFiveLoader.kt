package com.dhimandasgupta.funposables.loaders

import androidx.compose.runtime.Composable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

/**
 * From https://composeinternals.com/composeloaders
 * */
@Preview
@Composable
fun ThinkingFiveLoader(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "loader")

    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress"
    )

    val pulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse"
    )

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 28000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Canvas(modifier = modifier.size(200.dp).graphicsLayer { rotationZ = rotation }) {
        val baseRadius = 7.0f
        val detailAmplitude = 3.0f
        val petalCount = 5
        val curveScale = 3.9f
        val particleCount = 62
        val trailSpan = 0.38f

        val pulseAngle = pulse * TWO_PI + 0.55f
        val detailScale = 0.52f + ((sin(pulseAngle) + 1f) / 2f) * 0.48f

        // Draw trail path (faint)
        val path = androidx.compose.ui.graphics.Path()
        for (i in 0..480) {
            val t = (i / 480f) * TWO_PI
            val x = center.x + (baseRadius * cos(t) - detailAmplitude * detailScale * cos(petalCount * t)) * curveScale * (size.width / 200f)
            val y = center.y + (baseRadius * sin(t) - detailAmplitude * detailScale * sin(petalCount * t)) * curveScale * (size.height / 200f)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, color = Color.White.copy(alpha = 0.1f), style = Stroke(width = 5.5f.dp.toPx(), cap = StrokeCap.Round))

        // Draw particles
        for (i in 0 until particleCount) {
            val tailOffset = i.toFloat() / (particleCount - 1)
            val p = ((progress - tailOffset * trailSpan) % 1f + 1f) % 1f
            val t = p * TWO_PI
            val px = center.x + (baseRadius * cos(t) - detailAmplitude * detailScale * cos(petalCount * t)) * curveScale * (size.width / 200f)
            val py = center.y + (baseRadius * sin(t) - detailAmplitude * detailScale * sin(petalCount * t)) * curveScale * (size.height / 200f)
            val fade = (1f - tailOffset).pow(0.56f)
            val radius = (0.9f + fade * 2.7f).dp.toPx()
            drawCircle(Color.White.copy(alpha = 0.04f + fade * 0.96f), radius = radius, center = Offset(px, py))
        }
    }
}

private const val TWO_PI = 2f * Math.PI.toFloat()