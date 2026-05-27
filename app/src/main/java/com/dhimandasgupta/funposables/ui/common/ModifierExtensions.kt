package com.dhimandasgupta.funposables.ui.common

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
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

/**
 * Adds a vertical scrollbar to a composable column that is scrollable.
 *
 * @param scrollState The scroll state of the associated column.
 * @param width The width of the scrollbar track. Default is 4.dp.
 * @param thumbWidth The width of the scrollbar thumb. Default is equal to the width.
 * @param color The color of the scrollbar thumb. Default is a semi-transparent black.
 * @param minThumbHeight The minimum height of the scrollbar thumb. Default is 32.dp.
 * @param endPadding The padding at the end of the scrollbar to avoid overlap. Default is 2.dp.
 * @return A modified [Modifier] with a vertical scrollbar applied.
 */
fun Modifier.verticalScrollbarForColumn(
    scrollState: ScrollState,
    width: Dp = 4.dp,
    thumbWidth: Dp = width,
    color: Color = Color.Black.copy(alpha = 0.24f),
    minThumbHeight: Dp = 32.dp,
    endPadding: Dp = 2.dp,
): Modifier {
    return this then VerticalScrollbarElement(
        scrollState = scrollState,
        width = width,
        thumbWidth = thumbWidth,
        color = color,
        minThumbHeight = minThumbHeight,
        endPadding = endPadding,
    )
}

private data class VerticalScrollbarElement(
    val scrollState: ScrollState,
    val width: Dp,
    val thumbWidth: Dp,
    val color: Color,
    val minThumbHeight: Dp,
    val endPadding: Dp,
) : ModifierNodeElement<VerticalScrollbarNode>() {

    override fun create(): VerticalScrollbarNode {
        return VerticalScrollbarNode(
            scrollState = scrollState,
            width = width,
            thumbWidth = thumbWidth,
            color = color,
            minThumbHeight = minThumbHeight,
            endPadding = endPadding,
        )
    }

    override fun update(node: VerticalScrollbarNode) {
        node.update(
            scrollState = scrollState,
            width = width,
            thumbWidth = thumbWidth,
            color = color,
            minThumbHeight = minThumbHeight,
            endPadding = endPadding,
        )
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "verticalScrollbar"
        properties["scrollState"] = scrollState
        properties["width"] = width
        properties["thumbWidth"] = thumbWidth
        properties["color"] = color
        properties["minThumbHeight"] = minThumbHeight
        properties["endPadding"] = endPadding
    }
}

private class VerticalScrollbarNode(
    private var scrollState: ScrollState,
    private var width: Dp,
    private var thumbWidth: Dp,
    private var color: Color,
    private var minThumbHeight: Dp,
    private var endPadding: Dp,
) : Modifier.Node(), DrawModifierNode {

    private var scrollJob: Job? = null

    private var currentScrollValue by mutableIntStateOf(0)
    private var currentMaxScrollValue by mutableIntStateOf(0)

    override fun onAttach() {
        observeScrollState()
    }

    override fun onDetach() {
        scrollJob?.cancel()
        scrollJob = null
    }

    override fun ContentDrawScope.draw() {
        drawContent()

        if (currentMaxScrollValue <= 0) return

        val viewportHeight = size.height
        val contentHeight = viewportHeight + currentMaxScrollValue

        if (contentHeight <= 0f) return

        drawScrollbar(
            scrollProgress = currentScrollValue / currentMaxScrollValue.toFloat(),
            viewportToContentRatio = viewportHeight / contentHeight,
            width = width,
            thumbWidth = thumbWidth,
            color = color,
            minThumbHeight = minThumbHeight,
            endPadding = endPadding,
        )
    }

    fun update(
        scrollState: ScrollState,
        width: Dp,
        thumbWidth: Dp,
        color: Color,
        minThumbHeight: Dp,
        endPadding: Dp,
    ) {
        val scrollStateChanged = this.scrollState != scrollState

        this.scrollState = scrollState
        this.width = width
        this.thumbWidth = thumbWidth
        this.color = color
        this.minThumbHeight = minThumbHeight
        this.endPadding = endPadding

        if (scrollStateChanged) {
            observeScrollState()
        } else {
            invalidateDraw()
        }
    }

    private fun observeScrollState() {
        scrollJob?.cancel()

        scrollJob = coroutineScope.launch {
            snapshotFlow {
                scrollState.value to scrollState.maxValue
            }.collectLatest { (value, maxValue) ->
                currentScrollValue = value
                currentMaxScrollValue = maxValue
                invalidateDraw()
            }
        }
    }
}

/**
 * Adds a vertical scrollbar to a LazyColumn to improve usability for scrolling.
 * The scrollbar is visually represented and customized through parameters like width, color, and thumb size.
 *
 * @param lazyListState The state object that controls and observes the scroll position of the LazyColumn.
 * @param width The total width of the scrollbar including the track. Defaults to 4.dp.
 * @param thumbWidth The width of the scrollbar's thumb, the draggable part. Defaults to the same value as `width`.
 * @param color The color of the scrollbar thumb. Defaults to a semi-transparent black.
 * @param minThumbHeight The minimum height of the scrollbar thumb, ensuring it remains visible for large content. Defaults to 32.dp.
 * @param endPadding Padding between the end of the scrollbar and the edge of the LazyColumn content. Defaults to 2.dp.
 * @return A modified [Modifier] instance with the applied vertical scrollbar for the LazyColumn.
 */
fun Modifier.verticalScrollbarForLazyColumn(
    lazyListState: LazyListState,
    width: Dp = 4.dp,
    thumbWidth: Dp = width,
    color: Color = Color.Black.copy(alpha = 0.24f),
    minThumbHeight: Dp = 32.dp,
    endPadding: Dp = 2.dp,
): Modifier {
    return this then LazyListVerticalScrollbarElement(
        lazyListState = lazyListState,
        width = width,
        thumbWidth = thumbWidth,
        color = color,
        minThumbHeight = minThumbHeight,
        endPadding = endPadding,
    )
}

private data class LazyListVerticalScrollbarElement(
    val lazyListState: LazyListState,
    val width: Dp,
    val thumbWidth: Dp,
    val color: Color,
    val minThumbHeight: Dp,
    val endPadding: Dp,
) : ModifierNodeElement<LazyListVerticalScrollbarNode>() {

    override fun create(): LazyListVerticalScrollbarNode {
        return LazyListVerticalScrollbarNode(
            lazyListState = lazyListState,
            width = width,
            thumbWidth = thumbWidth,
            color = color,
            minThumbHeight = minThumbHeight,
            endPadding = endPadding,
        )
    }

    override fun update(node: LazyListVerticalScrollbarNode) {
        node.update(
            lazyListState = lazyListState,
            width = width,
            thumbWidth = thumbWidth,
            color = color,
            minThumbHeight = minThumbHeight,
            endPadding = endPadding,
        )
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "verticalScrollbarForLazyList"
        properties["lazyListState"] = lazyListState
        properties["width"] = width
        properties["thumbWidth"] = thumbWidth
        properties["color"] = color
        properties["minThumbHeight"] = minThumbHeight
        properties["endPadding"] = endPadding
    }
}

private class LazyListVerticalScrollbarNode(
    private var lazyListState: LazyListState,
    private var width: Dp,
    private var thumbWidth: Dp,
    private var color: Color,
    private var minThumbHeight: Dp,
    private var endPadding: Dp,
) : Modifier.Node(), DrawModifierNode {

    private var scrollJob: Job? = null

    private var currentScrollProgress by mutableFloatStateOf(0f)
    private var currentViewportToContentRatio by mutableFloatStateOf(1f)
    private var canScroll by mutableStateOf(false)
    private var reachedBottom by mutableStateOf(false)

    override fun onAttach() {
        observeLazyListState()
    }

    override fun onDetach() {
        scrollJob?.cancel()
        scrollJob = null
    }

    override fun ContentDrawScope.draw() {
        drawContent()

        if (!canScroll) return
        if (reachedBottom) return

        drawScrollbar(
            scrollProgress = currentScrollProgress,
            viewportToContentRatio = currentViewportToContentRatio,
            width = width,
            thumbWidth = thumbWidth,
            color = color,
            minThumbHeight = minThumbHeight,
            endPadding = endPadding,
        )
    }

    fun update(
        lazyListState: LazyListState,
        width: Dp,
        thumbWidth: Dp,
        color: Color,
        minThumbHeight: Dp,
        endPadding: Dp,
    ) {
        val lazyListStateChanged = this.lazyListState != lazyListState

        this.lazyListState = lazyListState
        this.width = width
        this.thumbWidth = thumbWidth
        this.color = color
        this.minThumbHeight = minThumbHeight
        this.endPadding = endPadding

        if (lazyListStateChanged) {
            observeLazyListState()
        } else {
            invalidateDraw()
        }
    }

    private fun observeLazyListState() {
        scrollJob?.cancel()

        scrollJob = coroutineScope.launch {
            snapshotFlow {
                val layoutInfo = lazyListState.layoutInfo
                val visibleItems = layoutInfo.visibleItemsInfo
                val totalItemsCount = layoutInfo.totalItemsCount

                if (visibleItems.isEmpty() || totalItemsCount == 0) {
                    LazyListScrollbarMetrics(
                        scrollProgress = 0f,
                        viewportToContentRatio = 1f,
                        canScroll = false,
                    )
                } else {
                    val viewportStartOffset = layoutInfo.viewportStartOffset
                    val viewportEndOffset = layoutInfo.viewportEndOffset
                    val viewportSize = viewportEndOffset - viewportStartOffset

                    val averageItemSize = visibleItems.sumOf { it.size }.toFloat() / visibleItems.size

                    val estimatedContentSize = averageItemSize * totalItemsCount

                    val estimatedScrollOffset =
                        lazyListState.firstVisibleItemIndex * averageItemSize +
                                lazyListState.firstVisibleItemScrollOffset

                    val maxScrollOffset = (estimatedContentSize - viewportSize)
                        .coerceAtLeast(1f)

                    val scrollProgress = (estimatedScrollOffset / maxScrollOffset)
                        .coerceIn(0f, 1f)

                    val viewportToContentRatio = (viewportSize / estimatedContentSize)
                        .coerceIn(0f, 1f)

                    LazyListScrollbarMetrics(
                        scrollProgress = scrollProgress,
                        viewportToContentRatio = viewportToContentRatio,
                        canScroll = estimatedContentSize > viewportSize,
                    )
                }
            }.collectLatest { metrics ->
                currentScrollProgress = metrics.scrollProgress
                currentViewportToContentRatio = metrics.viewportToContentRatio
                canScroll = metrics.canScroll
                reachedBottom = !lazyListState.canScrollForward
                invalidateDraw()
            }
        }
    }
}

private fun ContentDrawScope.drawScrollbar(
    scrollProgress: Float,
    viewportToContentRatio: Float,
    width: Dp,
    thumbWidth: Dp,
    color: Color,
    minThumbHeight: Dp,
    endPadding: Dp,
) {
    val scrollbarWidthPx = width.toPx()
    val thumbWidthPx = thumbWidth.toPx()
    val minThumbHeightPx = minThumbHeight.toPx()
    val endPaddingPx = endPadding.toPx()

    val viewportHeight = size.height

    val thumbHeight = (viewportHeight * viewportToContentRatio)
        .coerceAtLeast(minThumbHeightPx)
        .coerceAtMost(viewportHeight)

    val availableTrackHeight = viewportHeight - thumbHeight
    val thumbTop = availableTrackHeight * scrollProgress

    val trackLeft = when (layoutDirection) {
        LayoutDirection.Ltr -> size.width - scrollbarWidthPx - endPaddingPx
        LayoutDirection.Rtl -> endPaddingPx
    }

    val thumbLeft = trackLeft - ((thumbWidthPx - scrollbarWidthPx) / 2f)

    val radius = thumbWidthPx / 2f

    drawRoundRect(
        color = color,
        topLeft = Offset(
            x = thumbLeft,
            y = thumbTop,
        ),
        size = Size(
            width = thumbWidthPx,
            height = thumbHeight,
        ),
        cornerRadius = CornerRadius(
            x = radius,
            y = radius,
        ),
    )
}

private data class LazyListScrollbarMetrics(
    val scrollProgress: Float,
    val viewportToContentRatio: Float,
    val canScroll: Boolean,
    val reachedBottom: Boolean = false,
)

/**
 * Applies a bottom fade gradient to a column, based on the given scroll state and fading parameters.
 * The gradient will appear only when the column is not fully scrolled to the bottom.
 *
 * @param scrollState The scroll state of the column to determine when to apply the fade effect.
 * @param heightPercentage The percentage of the column's height over which the fade effect will be applied. The default is 0.15f.
 * @param color The color of the fade gradient. The default is Color.Unspecified.
 * @param maxAlpha The maximum alpha value of the fade gradient. Default is 0.8f.
 * @return A [Modifier] with the bottom fade effect applied.
 */
fun Modifier.bottomFadeForColumn(
    scrollState: ScrollState,
    heightPercentage: Float = 0.15f,
    color: Color = Color.Unspecified,
    maxAlpha: Float = 0.8f,
): Modifier {
    return bottomFade(
        heightPercentage = heightPercentage,
        color = color,
        maxAlpha = maxAlpha,
        shouldDrawFade = {
            scrollState.value < scrollState.maxValue
        },
    )
}

/**
 * Applies a fading effect at the bottom of a LazyColumn based on its scroll state.
 * The fade effect is controlled by customizable properties such as height percentage,
 * color, and maximum opacity, and is displayed when the LazyColumn can scroll forward.
 *
 * @param lazyListState The state object associated with the LazyColumn, which tracks its scroll position.
 * @param heightPercentage The height of the fade effect as a percentage of the LazyColumn's total height.
 *                         Must be between 0.0f and 1.0f. Defaults to 0.15f.
 * @param color The color of the fade effect. Defaults to Color.Unspecified.
 * @param maxAlpha The maximum alpha (opacity) of the fade effect. Must be between 0.0f and 1.0f. Defaults to 0.8f.
 * @return A Modifier with the applied bottom fade effect for the LazyColumn.
 */
fun Modifier.bottomFadeForLazyColumn(
    lazyListState: LazyListState,
    heightPercentage: Float = 0.15f,
    color: Color = Color.Unspecified,
    maxAlpha: Float = 0.8f,
): Modifier {
    return bottomFade(
        heightPercentage = heightPercentage,
        color = color,
        maxAlpha = maxAlpha,
        shouldDrawFade = {
            lazyListState.canScrollForward
        },
    )
}

private fun Modifier.bottomFade(
    heightPercentage: Float,
    color: Color,
    maxAlpha: Float,
    shouldDrawFade: () -> Boolean,
): Modifier {
    return drawWithCache {
        val coercedHeightPercentage = heightPercentage.coerceIn(0f, 1f)
        val coercedMaxAlpha = maxAlpha.coerceIn(0f, 1f)

        val fadeHeightPx = size.height * coercedHeightPercentage
        val fadeTop = size.height - fadeHeightPx

        val fadeBrush = Brush.verticalGradient(
            colors = listOf(
                color.copy(alpha = 0f),
                color.copy(alpha = coercedMaxAlpha/4),
                color.copy(alpha = coercedMaxAlpha/2),
                color.copy(alpha = coercedMaxAlpha),
            ),
            startY = fadeTop,
            endY = size.height,
        )

        onDrawWithContent {
            drawContent()

            if (!shouldDrawFade()) return@onDrawWithContent
            if (fadeHeightPx <= 0f) return@onDrawWithContent

            drawRect(
                brush = fadeBrush,
                topLeft = Offset(
                    x = 0f,
                    y = fadeTop,
                ),
                size = Size(
                    width = size.width,
                    height = fadeHeightPx,
                ),
            )
        }
    }
}