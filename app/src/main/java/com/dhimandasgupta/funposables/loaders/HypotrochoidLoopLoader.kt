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
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

/**
 * From https://composeinternals.com/composeloaders
 * */
@Preview
@Composable
fun HypotrochoidLoopLoader(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "loader")

    val progress by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 7600, easing = LinearEasing), RepeatMode.Restart),
        label = "progress"
    )
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 6200, easing = LinearEasing), RepeatMode.Restart),
        label = "pulse"
    )

    Canvas(modifier = modifier.size(200.dp)) {
        val R = 8.2f
        val rBase = 2.7f
        val rBoost = 0.45f
        val dBase = 4.8f
        val dBoost = 1.2f
        val spiroScale = 3.05f
        val particleCount = 82
        val trailSpan = 0.46f
        val TWO_PI = 2f * Math.PI.toFloat()

        val pulseAngle = pulse * TWO_PI + 0.55f
        val detailScale = 0.52f + ((sin(pulseAngle) + 1f) / 2f) * 0.48f
        val r = rBase + detailScale * rBoost
        val d = dBase + detailScale * dBoost
        val canvasScale = size.width / 100f

        val path = androidx.compose.ui.graphics.Path()
        for (i in 0..480) {
            val t = (i / 480f) * TWO_PI
            val cx = 50f + ((R - r) * cos(t) + d * cos(((R - r) / r) * t)) * spiroScale
            val cy = 50f + ((R - r) * sin(t) - d * sin(((R - r) / r) * t)) * spiroScale
            if (i == 0) path.moveTo(cx * canvasScale, cy * canvasScale) else path.lineTo(cx * canvasScale, cy * canvasScale)
        }
        drawPath(path, Color.White.copy(alpha = 0.1f), style = Stroke(4.6f.dp.toPx(), cap = StrokeCap.Round))

        for (i in 0 until particleCount) {
            val tailOffset = i.toFloat() / (particleCount - 1)
            val p = ((progress - tailOffset * trailSpan) % 1f + 1f) % 1f
            val t = p * TWO_PI
            val px = (50f + ((R - r) * cos(t) + d * cos(((R - r) / r) * t)) * spiroScale) * canvasScale
            val py = (50f + ((R - r) * sin(t) - d * sin(((R - r) / r) * t)) * spiroScale) * canvasScale
            val fade = (1f - tailOffset).pow(0.56f)
            drawCircle(Color.White.copy(alpha = 0.04f + fade * 0.96f), radius = (0.9f + fade * 2.7f).dp.toPx(), center = Offset(px, py))
        }
    }
}