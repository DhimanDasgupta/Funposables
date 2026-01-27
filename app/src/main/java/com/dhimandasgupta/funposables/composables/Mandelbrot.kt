package com.dhimandasgupta.funposables.composables

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.toArgb

val MANDELBROT_SHADER_CODE = """
    uniform float2 size;
    uniform float zoom;
    uniform float2 center;

    layout(color) uniform half4 color1;
    layout(color) uniform half4 color2;
    layout(color) uniform half4 color3;
    layout(color) uniform half4 color4;

    half4 main(float2 fragCoord) {
        // Standardizing coordinates: setting (0,0) to center and adjusting for aspect ratio
        float2 uv = (fragCoord - 0.5 * size) / min(size.y, size.x);
        
        // Mapping screen pixels to the Complex Plane (c = x + iy)
        float2 c = uv * zoom + center;
        
        // Initial value of Z (z0 = 0)
        float2 z = float2(0.0);
        float iter = 0.0;
        const float maxIter = 200.0;

        // Iterating the Mandelbrot formula: z(n+1) = z(n)^2 + c
        for (float i = 0.0; i < maxIter; i++) {
            // Complex multiplication: (x + iy)^2 = (x^2 - y^2) + i(2xy)
            float x_new = z.x * z.x - z.y * z.y + c.x;
            float y_new = 2.0 * z.x * z.y + c.y;
            z = float2(x_new, y_new);

            // If the magnitude |z| > 2, the point will escape to infinity
            if (dot(z, z) > 4.0) break;
            iter++;
        }

        // Points that remain bounded after maxIter belong to the Mandelbrot Set
        if (iter == maxIter) return half4(0.0, 0.0, 0.0, 1.0);

        // Color interpolation based on escape time (number of iterations)
        float t = iter / maxIter;
        
        // Multi-stage linear gradient interpolation
        if (t < 0.33) {
            return mix(color1, color2, t / 0.33);
        } else if (t < 0.66) {
            return mix(color2, color3, (t - 0.33) / 0.33);
        } else {
            return mix(color3, color4, (t - 0.66) / 0.34);
        }
    }
""".trimIndent()

@Composable
fun Mandelbrot(
    modifier: Modifier
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        // State management for navigation
        var zoom by remember { mutableFloatStateOf(2.5f) }
        var center by remember { mutableStateOf(Offset(-0.5f, 0f)) }

        // Pre-initialize shader to avoid object allocation during draw phase
        val shader = remember { RuntimeShader(MANDELBROT_SHADER_CODE) }

        val state = rememberTransformableState { zoomChange, offsetChange, _ ->
            // Adjust zoom level (clamped to prevent floating point artifacts)
            zoom = (zoom / zoomChange).coerceIn(0.00001f, 5f)

            // Pan sensitivity must scale with zoom level to feel natural
            val sensitivity = zoom / 1000f
            center = Offset(
                center.x - offsetChange.x * sensitivity,
                center.y + offsetChange.y * sensitivity
            )
        }

        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black)
                .transformable(state = state)
                .drawWithCache {
                    // Static uniforms: Set once per cache invalidation (e.g., on size change)
                    shader.setFloatUniform("size", size.width, size.height)
                    shader.setColorUniform("color1", Color(0xFF000000).toArgb())
                    shader.setColorUniform("color2", Color(0xFF206BCB).toArgb())
                    shader.setColorUniform("color3", Color(0xFFEDFFFF).toArgb())
                    shader.setColorUniform("color4", Color(0xFFFFB000).toArgb())

                    val brush = ShaderBrush(shader)

                    onDrawBehind {
                        // Dynamic uniforms: Updated every frame
                        shader.setFloatUniform("zoom", zoom)
                        shader.setFloatUniform("center", center.x, center.y)

                        drawRect(brush = brush)
                    }
                }
        )
    }
}