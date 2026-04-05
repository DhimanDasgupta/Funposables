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
fun RoseFourLoader(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "loader")

    val progress by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 5400, easing = LinearEasing), RepeatMode.Restart),
        label = "progress"
    )
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 4500, easing = LinearEasing), RepeatMode.Restart),
        label = "pulse"
    )
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = -360f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 28000, easing = LinearEasing), RepeatMode.Restart),
        label = "rotation"
    )

    Canvas(modifier = modifier.size(200.dp).graphicsLayer { rotationZ = rotation }) {
        val roseA = 9.2f
        val roseABoost = 0.60f
        val breathBase = 0.72f
        val breathBoost = 0.28f
        val k = 4
        val roseScale = 3.25f
        val particleCount = 78
        val trailSpan = 0.32f
        val TWO_PI = 2f * Math.PI.toFloat()

        val pulseAngle = pulse * TWO_PI + 0.55f
        val detailScale = 0.52f + ((sin(pulseAngle) + 1f) / 2f) * 0.48f
        val a = roseA + detailScale * roseABoost
        val cs = size.width / 100f

        val path = androidx.compose.ui.graphics.Path()
        for (i in 0..480) {
            val t = (i / 480f) * TWO_PI
            val r = a * (breathBase + detailScale * breathBoost) * cos(k * t)
            val x = (50f + cos(t) * r * roseScale) * cs
            val y = (50f + sin(t) * r * roseScale) * cs
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, Color.White.copy(alpha = 0.1f), style = Stroke(4.6f.dp.toPx(), cap = StrokeCap.Round))

        for (i in 0 until particleCount) {
            val tailOffset = i.toFloat() / (particleCount - 1)
            val p = ((progress - tailOffset * trailSpan) % 1f + 1f) % 1f
            val t = p * TWO_PI
            val r = a * (breathBase + detailScale * breathBoost) * cos(k * t)
            val px = (50f + cos(t) * r * roseScale) * cs
            val py = (50f + sin(t) * r * roseScale) * cs
            val fade = (1f - tailOffset).pow(0.56f)
            drawCircle(Color.White.copy(alpha = 0.04f + fade * 0.96f), radius = (0.9f + fade * 2.7f).dp.toPx(), center = Offset(px, py))
        }
    }
}