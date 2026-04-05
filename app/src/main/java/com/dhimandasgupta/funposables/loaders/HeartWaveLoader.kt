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
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * From https://composeinternals.com/composeloaders
 * */
@Preview
@Composable
fun HeartWaveLoader(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "loader")

    val progress by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 8400, easing = LinearEasing), RepeatMode.Restart),
        label = "progress"
    )
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 5600, easing = LinearEasing), RepeatMode.Restart),
        label = "pulse"
    )

    Canvas(modifier = modifier.size(200.dp)) {
        val b = 6.4f
        val heartWaveRoot = 3.30f
        val waveAmp = 0.90f
        val scaleX = 23.2f
        val scaleYBase = 24.5f
        val particleCount = 104
        val trailSpan = 0.18f
        val TWO_PI = 2f * Math.PI.toFloat()
        val PI = Math.PI.toFloat()

        val pulseAngle = pulse * TWO_PI + 0.55f
        val detailScale = 0.52f + ((sin(pulseAngle) + 1f) / 2f) * 0.48f
        val scaleY = scaleYBase + detailScale * 1.5f
        val xLimit = sqrt(heartWaveRoot)
        val cs = size.width / 100f

        fun heartPoint(prog: Float): Offset {
            val x = -xLimit + prog * xLimit * 2f
            val safeRoot = maxOf(0f, heartWaveRoot - x * x)
            val wave = waveAmp * sqrt(safeRoot) * sin(b * PI * x)
            val curve = abs(x).pow(2f / 3f)
            val y = curve + wave
            return Offset((50f + x * scaleX) * cs, (18f + (1.75f - y) * scaleY) * cs)
        }

        val path = androidx.compose.ui.graphics.Path()
        for (i in 0..480) {
            val pt = heartPoint(i / 480f)
            if (i == 0) path.moveTo(pt.x, pt.y) else path.lineTo(pt.x, pt.y)
        }
        drawPath(path, Color.White.copy(alpha = 0.1f), style = Stroke(3.9f.dp.toPx(), cap = StrokeCap.Round))

        for (i in 0 until particleCount) {
            val tailOffset = i.toFloat() / (particleCount - 1)
            val p = ((progress - tailOffset * trailSpan) % 1f + 1f) % 1f
            val pt = heartPoint(p)
            val fade = (1f - tailOffset).pow(0.56f)
            drawCircle(Color.White.copy(alpha = 0.04f + fade * 0.96f), radius = (0.9f + fade * 2.7f).dp.toPx(), center = pt)
        }
    }
}