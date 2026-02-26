package com.dhimandasgupta.funposables.composables

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.constrainWidth
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CircularLayoutPane(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition("rotation")

    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 10000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation"
    )

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularLayout(
            modifier = modifier.graphicsLayer {
                rotationZ = angle
            }
        ) {
            repeat(10) { index ->
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(colorScheme.onPrimary)
                        .graphicsLayer {
                            alpha = 0.75f
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = index.toString(),
                        style = typography.bodySmall,
                        modifier = Modifier.graphicsLayer {
                            /**
                             * 0 if the text also needs to rotate or -angle if the text always be straight
                             * */
                            rotationZ = -angle
                        }
                    )
                }
            }
        }
    }

}

@Composable
private fun CircularLayout(
    modifier: Modifier = Modifier,
    radius: Dp = 100.dp,
    startAngle: Float = 0f,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    Layout(
        content = content,
        modifier = modifier 
    ) { messarables, constraints ->
        val radiusInPixel = with(density) { radius.toPx() }
        val placesables = messarables.map { messarable ->
            messarable.measure(constraints.copy(minWidth = 0, minHeight = 0))
        }

        val maxChildWidth = placesables.maxOf { it.width }
        val maxChildHeight = placesables.maxOf { it.height }

        val desiredWith = (2 * radiusInPixel + maxChildWidth).toInt()
        val desiredHeight = (2 * radiusInPixel + maxChildHeight).toInt()

        val layoutWidth = constraints.constrainWidth(desiredWith)
        val layoutHeight = constraints.constrainHeight(desiredHeight)

        val centerX = layoutWidth / 2f
        val centerY = layoutHeight / 2f

        val angleBetween = 360f / placesables.size

        layout(width = layoutWidth, height = layoutHeight) {
            placesables.forEachIndexed { index, placeable ->
                val angle = startAngle + (index * angleBetween)
                val angleInRadians = Math.toRadians(angle.toDouble())

                val x = centerX + (radiusInPixel * cos(angleInRadians)).toFloat() - placeable.width / 2
                val y = centerY + (radiusInPixel * sin(angleInRadians)).toFloat() - placeable.height / 2

                placeable.placeRelative(
                    x = x.toInt(),
                    y = y.toInt()
                )
            }
        }
    }
}