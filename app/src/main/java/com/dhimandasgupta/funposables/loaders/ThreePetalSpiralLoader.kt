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
fun ThreePetalSpiralLoader(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "loader")

    val progress by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 4600, easing = LinearEasing), RepeatMode.Restart),
        label = "progress"
    )
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 4200, easing = LinearEasing), RepeatMode.Restart),
        label = "pulse"
    )
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = -360f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 28000, easing = LinearEasing), RepeatMode.Restart),
        label = "rotation"
    )

    Canvas(modifier = modifier.size(200.dp).graphicsLayer { rotationZ = rotation }) {
        val R = 3.0f
        val r = 1.0f
        val d = 3.0f
        val spiralScale = 2.20f
        val spiralBreath = 0.45f
        val particleCount = 82
        val trailSpan = 0.34f
        val TWO_PI = 2f * Math.PI.toFloat()

        val pulseAngle = pulse * TWO_PI + 0.55f
        val detailScale = 0.52f + ((sin(pulseAngle) + 1f) / 2f) * 0.48f
        val dAnim = d + detailScale * 0.25f
        val scale = spiralScale + detailScale * spiralBreath
        val cs = size.width / 100f

        val path = androidx.compose.ui.graphics.Path()
        for (i in 0..480) {
            val t = (i / 480f) * TWO_PI
            val bx = (R - r) * cos(t) + dAnim * cos(((R - r) / r) * t)
            val by = (R - r) * sin(t) - dAnim * sin(((R - r) / r) * t)
            val cx = (50f + bx * scale) * cs
            val cy = (50f + by * scale) * cs
            if (i == 0) path.moveTo(cx, cy) else path.lineTo(cx, cy)
        }
        drawPath(path, Color.White.copy(alpha = 0.1f), style = Stroke(4.4f.dp.toPx(), cap = StrokeCap.Round))

        for (i in 0 until particleCount) {
            val tailOffset = i.toFloat() / (particleCount - 1)
            val p = ((progress - tailOffset * trailSpan) % 1f + 1f) % 1f
            val t = p * TWO_PI
            val bx = (R - r) * cos(t) + dAnim * cos(((R - r) / r) * t)
            val by = (R - r) * sin(t) - dAnim * sin(((R - r) / r) * t)
            val px = (50f + bx * scale) * cs
            val py = (50f + by * scale) * cs
            val fade = (1f - tailOffset).pow(0.56f)
            drawCircle(Color.White.copy(alpha = 0.04f + fade * 0.96f), radius = (0.9f + fade * 2.7f).dp.toPx(), center = Offset(px, py))
        }
    }
}