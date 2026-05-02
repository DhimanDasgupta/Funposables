package com.dhimandasgupta.funposables.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * Draws a grid pattern on the background of the composable.
 *
 * This modifier uses [drawWithCache] to efficiently cache the grid path and re-draws it
 * over the existing content. The grid consists of vertical and horizontal lines spaced
 * according to the provided [gridSize].
 *
 * @param gridColor The color of the grid lines. Defaults to [Color.Red].
 * @param gridSize The size of each grid square in density-independent pixels (dp). Defaults to 10.
 */
@Composable
fun Modifier.drawBackgroundGrid(
    gridColor: Color = Color.Red,
    gridSize: Int = 10 // Default grid size
): Modifier = drawWithCache {
    val stepSizePx = gridSize.dp.toPx().roundToInt().toFloat()

    val strokeWidth = 1f
    val offset = strokeWidth / 2f

    val gridPath = Path().apply {
        // Vertical lines
        var x = 0f
        while (x <= size.width) {
            moveTo(x + offset, 0f)
            lineTo(x + offset, size.height)
            x += stepSizePx
        }
        // Horizontal lines
        var y = 0f
        while (y <= size.height) {
            moveTo(0f, y + offset)
            lineTo(size.width, y + offset)
            y += stepSizePx
        }
    }

    onDrawWithContent {
        drawContent()
        drawPath(
            path = gridPath,
            color = gridColor,
            style = Stroke(width = strokeWidth)
        )
    }
}