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
fun FourierFlowLoader(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "loader")

    val progress by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 8400, easing = LinearEasing), RepeatMode.Restart),
        label = "progress"
    )
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 6800, easing = LinearEasing), RepeatMode.Restart),
        label = "pulse"
    )

    Canvas(modifier = modifier.size(200.dp)) {
        val x1 = 17.0f; val x3 = 7.5f; val x5 = 3.2f
        val y1 = 15.0f; val y2 = 8.2f; val y4 = 4.2f
        val mixBase = 1.00f; val mixPulse = 0.16f
        val particleCount = 92
        val trailSpan = 0.31f
        val TWO_PI = 2f * Math.PI.toFloat()

        val pulseAngle = pulse * TWO_PI + 0.55f
        val detailScale = 0.52f + ((sin(pulseAngle) + 1f) / 2f) * 0.48f
        val mix = mixBase + detailScale * mixPulse
        val cs = size.width / 100f

        fun fourierPoint(prog: Float): Offset {
            val t = prog * TWO_PI
            val fx = x1 * cos(t) + x3 * cos(3f * t + 0.6f * mix) + x5 * sin(5f * t - 0.4f)
            val fy = y1 * sin(t) + y2 * sin(2f * t + 0.25f) - y4 * cos(4f * t - 0.5f * mix)
            return Offset((50f + fx) * cs, (50f + fy) * cs)
        }

        val path = androidx.compose.ui.graphics.Path()
        for (i in 0..480) {
            val pt = fourierPoint(i / 480f)
            if (i == 0) path.moveTo(pt.x, pt.y) else path.lineTo(pt.x, pt.y)
        }
        drawPath(path, Color.White.copy(alpha = 0.1f), style = Stroke(4.2f.dp.toPx(), cap = StrokeCap.Round))

        for (i in 0 until particleCount) {
            val tailOffset = i.toFloat() / (particleCount - 1)
            val p = ((progress - tailOffset * trailSpan) % 1f + 1f) % 1f
            val pt = fourierPoint(p)
            val fade = (1f - tailOffset).pow(0.56f)
            drawCircle(Color.White.copy(alpha = 0.04f + fade * 0.96f), radius = (0.9f + fade * 2.7f).dp.toPx(), center = pt)
        }
    }
}