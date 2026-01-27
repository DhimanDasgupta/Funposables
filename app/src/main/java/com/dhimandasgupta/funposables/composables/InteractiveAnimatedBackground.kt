package com.dhimandasgupta.funposables.composables

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.toArgb

val ANIMATED_BACKGROUND_SHADER_CODE = """
    uniform float2 size;
    uniform float time;
    layout(color) uniform half4 color1;
    layout(color) uniform half4 color2;
    layout(color) uniform half4 color3;

    half4 main(float2 fragCoord) {
        // Normalized coordinates (0.0 to 1.0)
        float2 uv = fragCoord / size;
        float t = time * 0.5;

        // Wave 1: Horizontal movement using sine
        // sin(x * frequency + time) * amplitude + offset
        float wave1 = sin(uv.x * 4.0 + t) * 0.5 + 0.5;

        // Wave 2: Diagonal movement for more organic feel
        float wave2 = sin((uv.x + uv.y) * 3.0 - t * 1.2) * 0.5 + 0.5;

        // Wave 3: A moving spotlight/radial gradient
        // Calculating distance from a point that orbits the center
        float2 spotlightPos = float2(0.5 + 0.3 * cos(t * 0.5), 0.5 + 0.3 * sin(t * 0.5));
        float dist = length(uv - spotlightPos);
        
        // smoothstep creates a soft edge for the radial gradient
        float wave3 = 1.0 - smoothstep(0.0, 0.8, dist);

        // Blending colors using calculated wave patterns
        half4 mix1 = mix(color1, color2, wave1);
        
        // Averaging Wave 2 and Wave 3 for the final blend factor
        float finalMixFactor = (wave2 + wave3) / 2.0;
        half4 finalColor = mix(mix1, color3, finalMixFactor);

        return finalColor;
    }
""".trimIndent()

@Composable
fun Modifier.animatedBackground(
    color1: Color,
    color2: Color,
    color3: Color
): Modifier {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val infiniteTransition = rememberInfiniteTransition(label = "aurora_transition")

        // Smooth time animation
        val timeState = infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 100f, // Higher value for longer continuous play
            animationSpec = infiniteRepeatable(
                animation = tween(50_000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "time_animation"
        )

        // Initializing shader once and remembering it
        val shader = remember { RuntimeShader(ANIMATED_BACKGROUND_SHADER_CODE) }

        return this.drawWithCache {
            // Uniforms that only depend on size or initial colors are set here
            shader.setFloatUniform("size", size.width, size.height)
            shader.setColorUniform("color1", color1.toArgb())
            shader.setColorUniform("color2", color2.toArgb())
            shader.setColorUniform("color3", color3.toArgb())

            val brush = ShaderBrush(shader)

            onDrawBehind {
                // Updating time on every frame
                shader.setFloatUniform("time", timeState.value)
                drawRect(brush = brush)
            }
        }
    }

    return this.background(
        brush = Brush.verticalGradient(
            colors = listOf(
                color1,
                color2,
                color3
            )
        )
    )
}