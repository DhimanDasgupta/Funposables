package com.dhimandasgupta.funposables.composables

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp

@Composable
fun GlowingButton(
    text: String,
    glowColor: Color = Color.Green,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val infiniteTransition = rememberInfiniteTransition(label = "GlowTransition")
        // Smooth time animation
        val time by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 6.28f, // 2 * PI
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "TimeAngle"
        )

        // Compile the shader once
        val shader = remember { RuntimeShader(GLOW_SHADER_CODE) }
        val brush = ShaderBrush(shader)

        Button(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier
                .padding(96.dp)
                .requiredSizeIn(
                    minWidth = 120.dp,
                    minHeight = 48.dp,
                    maxWidth = 48.dp,
                    maxHeight = 96.dp
                )
                .drawWithCache {
                    println("Size: ${size.width}, ${size.height}")
                    shader.setFloatUniform("size", size.width, size.height)
                    shader.setColorUniform("color", glowColor.toArgb())

                    onDrawBehind {
                        shader.setFloatUniform("time", time)
                        // Draw the shader effect slightly larger than the button
                        drawRect(brush)
                    }
                },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors()
        ) {
            Text(text = text)
        }
    } else {
        Button(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier
                .padding(24.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = text)
        }
    }
}

private const val GLOW_SHADER_CODE = """
    // AGSL Glowing Function
uniform float2 size;      // Resolution of the Composable
uniform float time;      // Time in seconds (used for animation)
layout(color) uniform half4 color; // The base color of the glow

half4 main(float2 fragCoord) {
    // 1. Normalize coordinates to -1.0 to 1.0 (centering the origin)
    float2 uv = (fragCoord * 2.0 - size) / min(size.x, size.y);
    
    // 2. Calculate the distance from the center (0,0)
    float dist = length(uv);
    
    // 3. Create the pulse value using a sine wave
    // Adjust '3.0' to change speed; '0.5' to change intensity range
    float pulse = 0.5 + 0.5 * sin(time * 3.0);
    
    // 4. Calculate Glow Intensity
    // The smaller the 'dist', the higher the value. 
    // We add a 'softness' factor based on the pulse.
    float glowPower = 0.05 / (dist * (1.2 - pulse * 0.3));
    
    // 5. Smooth the edges so it doesn't look pixelated
    float alpha = smoothstep(0.0, 1.0, glowPower);
    
    return color * alpha;
}
"""