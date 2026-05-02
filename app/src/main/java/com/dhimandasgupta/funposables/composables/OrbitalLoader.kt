package com.dhimandasgupta.funposables.composables


import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random


/**
 * Cosmic color palette
 */
private object CosmicColors {
    // Background
    val deepSpace = Color(0xFF0F0F1A)
    val spaceGradientEnd = Color(0xFF1A1A2E)

    // Core sun
    val coreCenter = Color(0xFFFFE4B5)    // Warm white
    val coreMiddle = Color(0xFFFFB347)    // Orange
    val coreEdge = Color(0xFFFF6B35)      // Deep orange
    val coreGlow = Color(0xFFFF8C42)

    // Planets with complementary colors
    val planet1 = Color(0xFF00D9FF)       // Cyan
    val planet1Glow = Color(0xFF00A8CC)

    val planet2 = Color(0xFFBF5AF2)       // Purple
    val planet2Glow = Color(0xFF9D4EDD)

    val planet3 = Color(0xFF32D74B)       // Green
    val planet3Glow = Color(0xFF28A745)

    // Accent
    val starColor = Color(0xFFFFFFFF)
    val orbitLine = Color(0xFF2A2A4A)

    // Energy rings
    val energyRing = Color(0xFFFF6B35)
}

/**
 * Star data class for a background
 */
private data class Star(
    val x: Float,
    val y: Float,
    val radius: Float,
    val alpha: Float,
    val twinkleSpeed: Float
)

/**
 * Enhanced Orbital Loader with cosmic aesthetics
 */
@Composable
fun OrbitalLoader(
    modifier: Modifier = Modifier
) {
    // Generate stars at once and remember them
    val stars = remember {
        List(50) {
            Star(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                radius = Random.nextFloat() * 1.5f + 0.5f,
                alpha = Random.nextFloat() * 0.5f + 0.3f,
                twinkleSpeed = Random.nextFloat() * 2000f + 1000f
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "cosmic")

    // ORBITAL ROTATIONS
    val orbit1Angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing)
        ),
        label = "orbit1"
    )

    val orbit2Angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing)
        ),
        label = "orbit2"
    )

    val orbit3Angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(9000, easing = LinearEasing)
        ),
        label = "orbit3"
    )

    val moonAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing)
        ),
        label = "moon"
    )

    // EFFECTS ANIMATIONS
    val corePulse by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val starTwinkle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing)
        ),
        label = "twinkle"
    )

    // Energy ring expansion (0 to 1, then resets)
    val ringExpansion by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearOutSlowInEasing)
        ),
        label = "ring"
    )

    val ringExpansion2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearOutSlowInEasing),
            initialStartOffset = StartOffset(1500) // Offset for staggered effect
        ),
        label = "ring2"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        CosmicColors.spaceGradientEnd,
                        CosmicColors.deepSpace
                    )
                )
            )
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val center = Offset(size.width / 2, size.height / 2)

            // Orbital radii
            val orbit1Radius = size.minDimension * 0.15f
            val orbit2Radius = size.minDimension * 0.27f
            val orbit3Radius = size.minDimension * 0.40f
            val moonOrbitRadius = size.minDimension * 0.06f

            // Body sizes
            val coreRadius = size.minDimension * 0.07f * corePulse
            val planetRadius = size.minDimension * 0.025f
            val moonRadius = size.minDimension * 0.012f

            // Number of trail segments
            val trailCount = 12

            // LAYER 0: STARFIELD BACKGROUND
            drawStarfield(stars, starTwinkle)

            // LAYER 1: ORBIT PATHS (subtle guide lines)
            listOf(orbit1Radius, orbit2Radius, orbit3Radius).forEach { radius ->
                drawCircle(
                    color = CosmicColors.orbitLine,
                    radius = radius,
                    center = center,
                    style = Stroke(width = 1f)
                )
            }

            // LAYER 2: EXPANDING ENERGY RINGS FROM CORE
            drawEnergyRing(center, coreRadius, orbit1Radius * 1.5f, ringExpansion)
            drawEnergyRing(center, coreRadius, orbit1Radius * 1.5f, ringExpansion2)

            // LAYER 3: PLANET TRAILS (drawn before planets for correct z-order)
            // Using the SAME transformation logic, just at previous angles

            // Trail for Planet 1
            drawOrbitalTrail(
                center = center,
                orbitRadius = orbit1Radius,
                currentAngle = orbit1Angle,
                trailCount = trailCount,
                planetRadius = planetRadius,
                color = CosmicColors.planet1
            )

            // Trail for Planet 2
            drawOrbitalTrail(
                center = center,
                orbitRadius = orbit2Radius,
                currentAngle = orbit2Angle,
                trailCount = trailCount,
                planetRadius = planetRadius * 1.1f,
                color = CosmicColors.planet2
            )

            // Trail for Planet 3
            drawOrbitalTrail(
                center = center,
                orbitRadius = orbit3Radius,
                currentAngle = orbit3Angle,
                trailCount = trailCount + 4, // Longer trail for an outer planet
                planetRadius = planetRadius * 1.2f,
                color = CosmicColors.planet3
            )

            // LAYER 4: THE CORE (Central Star)
            drawGlowingCore(center, coreRadius)

            // LAYER 5: PLANETS WITH GLOW
            // Same transformation principles as before!

            // Planet 1 - Inner orbit
            withTransform({
                rotate(degrees = orbit1Angle, pivot = center)
                translate(left = center.x + orbit1Radius, top = center.y)
            }) {
                drawGlowingPlanet(
                    color = CosmicColors.planet1,
                    glowColor = CosmicColors.planet1Glow,
                    radius = planetRadius
                )
            }

            // Planet 2 - Middle orbit (counter-rotating) with the moon
            withTransform({
                rotate(degrees = orbit2Angle, pivot = center)
                translate(left = center.x + orbit2Radius, top = center.y)
            }) {
                drawGlowingPlanet(
                    color = CosmicColors.planet2,
                    glowColor = CosmicColors.planet2Glow,
                    radius = planetRadius * 1.1f
                )

                // Moon orbiting this planet
                withTransform({
                    rotate(degrees = moonAngle, pivot = Offset.Zero)
                    translate(left = moonOrbitRadius, top = 0f)
                }) {
                    // Moon glow
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                CosmicColors.planet2.copy(alpha = 0.4f),
                                Color.Transparent
                            ),
                            center = Offset.Zero,
                            radius = moonRadius * 3f
                        ),
                        radius = moonRadius * 3f,
                        center = Offset.Zero
                    )
                    drawCircle(
                        color = CosmicColors.planet2.copy(alpha = 0.8f),
                        radius = moonRadius,
                        center = Offset.Zero
                    )
                }
            }

            // Planet 3 - Outer orbit
            withTransform({
                rotate(degrees = orbit3Angle, pivot = center)
                translate(left = center.x + orbit3Radius, top = center.y)
            }) {
                drawGlowingPlanet(
                    color = CosmicColors.planet3,
                    glowColor = CosmicColors.planet3Glow,
                    radius = planetRadius * 1.2f
                )

                // Two moons for the outer planet
                withTransform({
                    rotate(degrees = -moonAngle, pivot = Offset.Zero)
                    translate(left = moonOrbitRadius * 1.2f, top = 0f)
                }) {
                    drawCircle(
                        color = CosmicColors.planet3.copy(alpha = 0.7f),
                        radius = moonRadius,
                        center = Offset.Zero
                    )
                }

                withTransform({
                    rotate(degrees = moonAngle + 180f, pivot = Offset.Zero)
                    translate(left = moonOrbitRadius * 0.8f, top = 0f)
                }) {
                    drawCircle(
                        color = CosmicColors.planet3.copy(alpha = 0.5f),
                        radius = moonRadius * 0.7f,
                        center = Offset.Zero
                    )
                }
            }
        }
    }
}

/**
 * Draws twinkling stars in the background
 */
private fun DrawScope.drawStarfield(stars: List<Star>, twinklePhase: Float) {
    stars.forEach { star ->
        // Calculate twinkle: each star has its own phase based on twinkleSpeed
        val twinkle = (sin((twinklePhase * 2 * PI + star.twinkleSpeed).toFloat()) + 1f) / 2f
        val currentAlpha = star.alpha * (0.5f + 0.5f * twinkle)

        drawCircle(
            color = CosmicColors.starColor.copy(alpha = currentAlpha),
            radius = star.radius,
            center = Offset(star.x * size.width, star.y * size.height)
        )
    }
}

/**
 * Draws an expanding energy ring from the core
 */
private fun DrawScope.drawEnergyRing(
    center: Offset,
    startRadius: Float,
    endRadius: Float,
    expansion: Float
) {
    val currentRadius = startRadius + (endRadius - startRadius) * expansion
    val alpha = (1f - expansion) * 0.6f // Fade out as it expands

    drawCircle(
        color = CosmicColors.energyRing.copy(alpha = alpha),
        radius = currentRadius,
        center = center,
        style = Stroke(width = 2f * (1f - expansion * 0.5f)) // Thinner as it expands
    )
}

/**
 * Draws the central glowing core/star
 */
private fun DrawScope.drawGlowingCore(center: Offset, radius: Float) {
    // Outer glow (largest, most transparent)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                CosmicColors.coreGlow.copy(alpha = 0.3f),
                CosmicColors.coreGlow.copy(alpha = 0.1f),
                Color.Transparent
            ),
            center = center,
            radius = radius * 4f
        ),
        radius = radius * 4f,
        center = center
    )

    // Middle glow
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                CosmicColors.coreGlow.copy(alpha = 0.5f),
                CosmicColors.coreGlow.copy(alpha = 0.2f),
                Color.Transparent
            ),
            center = center,
            radius = radius * 2.5f
        ),
        radius = radius * 2.5f,
        center = center
    )

    // Core body with gradient
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                CosmicColors.coreCenter,
                CosmicColors.coreMiddle,
                CosmicColors.coreEdge
            ),
            center = center,
            radius = radius
        ),
        radius = radius,
        center = center
    )
}

/**
 * Draws an orbital trail behind a planet
 *
 * The trail is just multiple circles drawn at PREVIOUS positions.
 * We use the same rotate+translate pattern, just with earlier angles.
 * This demonstrates that transformation logic can be reused!
 */
private fun DrawScope.drawOrbitalTrail(
    center: Offset,
    orbitRadius: Float,
    currentAngle: Float,
    trailCount: Int,
    planetRadius: Float,
    color: Color
) {
    for (i in 1..trailCount) {
        // Each trail segment is at a previous angle
        val trailAngle = currentAngle - (i * 3f) // 3 degrees apart
        val alpha = (1f - (i.toFloat() / trailCount)) * 0.4f
        val trailRadius = planetRadius * (1f - (i.toFloat() / trailCount) * 0.5f)

        withTransform({
            rotate(degrees = trailAngle, pivot = center)
            translate(left = center.x + orbitRadius, top = center.y)
        }) {
            drawCircle(
                color = color.copy(alpha = alpha),
                radius = trailRadius,
                center = Offset.Zero
            )
        }
    }
}

/**
 * Draws a planet with a glow effect at the current (transformed) origin
 */
private fun DrawScope.drawGlowingPlanet(
    color: Color,
    glowColor: Color,
    radius: Float
) {
    // Outer glow
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                glowColor.copy(alpha = 0.4f),
                glowColor.copy(alpha = 0.1f),
                Color.Transparent
            ),
            center = Offset.Zero,
            radius = radius * 4f
        ),
        radius = radius * 4f,
        center = Offset.Zero
    )

    // Inner glow
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                color.copy(alpha = 0.6f),
                Color.Transparent
            ),
            center = Offset.Zero,
            radius = radius * 2f
        ),
        radius = radius * 2f,
        center = Offset.Zero
    )

    // Planet body
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                color,
                glowColor
            ),
            center = Offset(-radius * 0.3f, -radius * 0.3f), // Off-center for 3D effect
            radius = radius * 1.5f
        ),
        radius = radius,
        center = Offset.Zero
    )
}