package com.dhimandasgupta.funposables.ui.common

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils

fun Color.toDarkModeSuitable(): Color {
    val hsl = FloatArray(3)
    ColorUtils.colorToHSL(this.toArgb(), hsl)

    // Reduce saturation: 0.0f to 1.0f range
    hsl[1] = (hsl[1] * 0.6f)
    // Increase lightness: so it pops against black
    hsl[2] = (hsl[2] + 0.2f).coerceAtMost(0.9f)

    return Color(ColorUtils.HSLToColor(hsl))
}

@Composable
fun getNormalizedColorForCurrentTheme(color: Color) =
    if (isSystemInDarkTheme()) color.toDarkModeSuitable() else color