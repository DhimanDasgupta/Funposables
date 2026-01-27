package com.dhimandasgupta.funposables.composables

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput

val JULIA_SHADER_CODE = """
    uniform float2 size;
    uniform float2 mouse;
    layout(color) uniform half4 color1;
    layout(color) uniform half4 color2;
    layout(color) uniform half4 color3;
    layout(color) uniform half4 color4;

    half4 main(float2 fragCoord) {
        // Standardizing coordinates to center (0,0) and applying 3x zoom
        float2 uv = (fragCoord - 0.5 * size) / min(size.y, size.x);
        uv *= 3.0; 

        // Deriving the Julia constant 'c' from interactive touch/mouse position
        float2 c = (mouse - 0.5 * size) / min(size.y, size.x) * 2.0;
        
        // In Julia Sets, Z starts at the pixel's coordinate
        float2 z = uv;
        float iter = 0.0;
        const float maxIter = 120.0; 

        for (float i = 0.0; i < maxIter; i++) {
            // Applying the iterative function: z = z^2 + c
            // Real part: x^2 - y^2 + cx, Imaginary part: 2xy + cy
            float x = z.x * z.x - z.y * z.y + c.x;
            float y = 2.0 * z.x * z.y + c.y;
            z = float2(x, y);
            
            // Escape condition: if magnitude |z| > 2 (squared magnitude > 4)
            if (dot(z, z) > 4.0) break;
            iter++;
        }

        // Points that do not escape are part of the Julia Set (colored black)
        if (iter == maxIter) return half4(0.0, 0.0, 0.0, 1.0);

        // Exponential smoothing for a more aesthetic gradient
        float t = pow(iter / maxIter, 0.8);

        // Linear interpolation between the four defined colors
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
fun InteractiveJulia(
    modifier: Modifier
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        // State to track user interaction (touch/mouse position)
        var mousePos by remember { mutableStateOf(Offset(500f, 500f)) }

        // Pre-initialize shader to avoid re-allocation in the draw loop
        val shader = remember { RuntimeShader(JULIA_SHADER_CODE) }

        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black)
                .pointerInput(Unit) {
                    // Tracking drag gestures to update the 'c' constant dynamically
                    detectDragGestures { change, _ ->
                        mousePos = change.position
                    }
                }
                .drawWithCache {
                    // Set static uniforms once or when size/colors change
                    shader.setFloatUniform("size", size.width, size.height)
                    shader.setColorUniform("color1", Color(0xFF000000).toArgb())
                    shader.setColorUniform("color2", Color(0xFF206BCB).toArgb())
                    shader.setColorUniform("color3", Color(0xFF00BCD4).toArgb())
                    shader.setColorUniform("color4", Color(0xFFF85A8A).toArgb())

                    val brush = ShaderBrush(shader)

                    onDrawBehind {
                        // Updating only the interactive coordinate per frame
                        shader.setFloatUniform("mouse", mousePos.x, mousePos.y)
                        drawRect(brush = brush)
                    }
                }
        )
    }
}