package com.dhimandasgupta.funposables.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateRotation
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.dhimandasgupta.funposables.ui.theme.FunposablesTheme
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun DragOrTransformBox(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(
                start = WindowInsets.displayCutout.union(WindowInsets.navigationBars)
                    .asPaddingValues()
                    .calculateStartPadding(
                        LayoutDirection.Ltr
                    ),
                top = WindowInsets.displayCutout.union(WindowInsets.statusBars).asPaddingValues()
                    .calculateTopPadding(),
                end = WindowInsets.displayCutout.union(WindowInsets.navigationBars)
                    .asPaddingValues()
                    .calculateEndPadding(
                        LayoutDirection.Ltr
                    ),
                bottom = WindowInsets.displayCutout.union(WindowInsets.navigationBars)
                    .asPaddingValues()
                    .calculateBottomPadding()
            ),
        contentAlignment = Alignment.Center
    ) {
        var offset by remember { mutableStateOf(Offset.Zero) }
        var scale by remember { mutableFloatStateOf(1f) }
        var rotation by remember { mutableFloatStateOf(0f) }

        DragOrTransformBox(
            modifier = Modifier.fillMaxSize(),
            onDrag = { delta ->
                offset += delta
            },
            onTransform = { zoom, rot, pan ->
                scale *= zoom
                rotation += rot
                offset += pan
            }
        ) {
            Box(
                modifier = Modifier
                    .offset { IntOffset(offset.x.roundToInt(), offset.y.roundToInt()) }
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        rotationZ = rotation
                    )
                    .size(150.dp)
                    .background(Color.Red, RoundedCornerShape(12.dp))
            )
        }
    }
}

@Composable
private fun DragOrTransformBox(
    modifier: Modifier = Modifier,
    onDrag: (Offset) -> Unit = {},
    onTransform: (zoom: Float, rotation: Float, pan: Offset) -> Unit = { _, _, _ -> },
    content: @Composable BoxScope.() -> Unit
) {
    var transformMode by remember { mutableStateOf(false) }

    Box(
        modifier = modifier.pointerInput(Unit) {
            val viewConfig = viewConfiguration

            awaitEachGesture {
                var pastTouchSlop = false
                var zoom = 1f
                var rotation = 0f
                var pan = Offset.Zero

                do {
                    //Explicitly pass the event phase
                    val event = awaitPointerEvent(PointerEventPass.Main)
                    val pressed = event.changes.any { it.pressed }

                    if (event.changes.size > 1) {
                        transformMode = true
                    }

                    if (transformMode && event.changes.size > 1) {
                        // Multi-touch transform mode
                        val zoomChange = event.calculateZoom()
                        val rotationChange = event.calculateRotation()
                        val panChange = event.calculatePan()

                        if (!pastTouchSlop) {
                            zoom *= zoomChange
                            rotation += rotationChange
                            pan += panChange

                            val zoomMotion = abs(1 - zoom)
                            val rotationMotion = abs(rotation * PI.toFloat() / 180f)
                            val panMotion = pan.getDistance()

                            pastTouchSlop =
                                zoomMotion > viewConfig.touchSlop ||
                                        rotationMotion > viewConfig.touchSlop ||
                                        panMotion > viewConfig.touchSlop
                        }

                        if (pastTouchSlop) {
                            onTransform(zoomChange, rotationChange, panChange)
                            event.changes.forEach { it.consume() }
                        }
                    } else if (!transformMode && event.changes.size == 1) {
                        //Single-finger drag
                        val change = event.changes.first()
                        val dragAmount = change.positionChange()

                        if (dragAmount != Offset.Zero) {
                            onDrag(dragAmount)
                            change.consume()
                        }
                    }

                    if (!pressed) transformMode = false
                } while (pressed)
            }
        }
    ) {
        content()
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview
@Composable
private fun DragOrTransformBoxPreview() {
    FunposablesTheme {
        DragOrTransformBox()
    }
}