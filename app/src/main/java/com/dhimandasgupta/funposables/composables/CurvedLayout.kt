package com.dhimandasgupta.funposables.composables

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- Constants for Styling ---
val BackgroundGray = Color(0xFFF4F4F4)
val ContainerWhite = Color.White
val IconPink = Color(0xFFFFC0EB)
val TextDark = Color(0xFF1A1A1A)
val ButtonDark = Color(0xFF4A4A4A)
val AccentPurple = Color(0xFF6F52B3)
val DisclosureGray = Color(0xFF444444)

val IconSize = 80.dp
val CurveDipDepth = 24.dp
val ContentPadding = 24.dp

// --- Custom Shape for the Curved Top Container ---
class ConcaveTopShape(private val dipDepthDp: Dp) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val dipDepthPx = with(density) { dipDepthDp.toPx() }
        val path = Path().apply {
            // Start at top-left corner
            moveTo(0f, 0f)
            // Quadratic bezier curve dipping down in the middle
            // Control point is below the center, end point is top-right
            quadraticTo(size.width / 2f, -dipDepthPx, size.width, 0f)
            // Line to bottom-right
            lineTo(size.width, size.height)
            // Line to bottom-left
            lineTo(0f, size.height)
            // Close back to top-left
            close()
        }
        return Outline.Generic(path)
    }
}

@Composable
fun CurvedLayout(
    modifier: Modifier = Modifier
) {
    ResponsiveCurvedLayoutScreen(modifier)
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ResponsiveCurvedLayoutScreen(
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT
    val scrollState = rememberScrollState()

    // Outer container with gray background
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundGray)
            .padding(
                start = WindowInsets
                    .displayCutout.union(insets = WindowInsets.navigationBars)
                    .asPaddingValues()
                    .calculateStartPadding(LayoutDirection.Ltr),
                end = WindowInsets
                    .displayCutout.union(insets = WindowInsets.navigationBars)
                    .asPaddingValues()
                    .calculateEndPadding(LayoutDirection.Ltr),
                bottom = WindowInsets
                    .displayCutout.union(insets = WindowInsets.navigationBars)
                    .asPaddingValues()
                    .calculateBottomPadding()
            )
            .verticalScroll(scrollState)
    ) {
        // "Open" text visible in portrait mock
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Open / Close",
                style = typography.headlineLarge,
                color = TextDark
            )
        }

        // Main overlapping container structure
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            // 1. The White Content Container with Curved Top
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    // Important: Add padding to the top of the surface so the content
                    // starts below where the icon will sit.
                    .padding(top = IconSize / 2),
                shape = ConcaveTopShape(CurveDipDepth),
                color = ContainerWhite,
                shadowElevation = 4.dp
            ) {
                // Content inside the white card
                Box(
                    modifier = Modifier.padding(
                        top = (IconSize / 2), // Extra padding below icon area
                        start = ContentPadding,
                        end = ContentPadding,
                        bottom = ContentPadding
                    )
                ) {
                    SharedTransitionLayout {
                        AnimatedContent(
                            targetState = isPortrait,
                            label = "layout_change"
                        ) { target ->
                            if (target) {
                                PortraitContent(
                                    sharedTransitionScope = this@SharedTransitionLayout,
                                    animatedVisibilityScope = this
                                )
                            } else {
                                LandscapeContent(
                                    sharedTransitionScope = this@SharedTransitionLayout,
                                    animatedVisibilityScope = this
                                )
                            }
                        }
                    }
                }
            }

            // 2. The FPO Icon sitting on top
            // Aligned TopCenter of the Box. Because the white container below has
            // top padding = IconSize/2, this icon will sit exactly half-in, half-out.
            FPOIcon(modifier = Modifier.align(Alignment.TopCenter))
        }
    }
}

// --- Icon Composable ---
@Composable
fun FPOIcon(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(IconSize)
            .clip(CircleShape)
            .background(IconPink)
            .border(1.dp, IconPink.copy(alpha = 0.5f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "FPO",
            fontWeight = FontWeight.Bold,
            color = TextDark
        )
    }
}

// --- Content Layouts ---

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun PortraitContent(
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    with(sharedTransitionScope) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            HeaderText(
                modifier = Modifier.sharedElement(
                    sharedContentState = rememberSharedContentState(key = "header"),
                    animatedVisibilityScope = animatedVisibilityScope
                ),
                center = true
            )
            SubheaderText(
                modifier = Modifier.sharedElement(
                    sharedContentState = rememberSharedContentState(key = "subHeaderText"),
                    animatedVisibilityScope = animatedVisibilityScope
                ),
                center = true
            )
            BodyText(
                modifier = Modifier.sharedElement(
                    sharedContentState = rememberSharedContentState(key = "bodyText"),
                    animatedVisibilityScope = animatedVisibilityScope
                ),
                center = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            MainButton(
                modifier = Modifier.sharedElement(
                    sharedContentState = rememberSharedContentState(key = "mainButton"),
                    animatedVisibilityScope = animatedVisibilityScope
                ),
            )
            SecondaryButton(
                modifier = Modifier.sharedElement(
                    sharedContentState = rememberSharedContentState(key = "secondaryButton"),
                    animatedVisibilityScope = animatedVisibilityScope
                ),
            )
            Spacer(modifier = Modifier.height(16.dp))
            DisclosureSection(
                modifier = Modifier.sharedElement(
                    sharedContentState = rememberSharedContentState(key = "disclosureSection"),
                    animatedVisibilityScope = animatedVisibilityScope
                ),
                center = true
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun LandscapeContent(
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    with(sharedTransitionScope) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // Left Column (Header & Buttons)
            Column(
                modifier = Modifier.weight(2f), // Takes 2/5 space
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                HeaderText(
                    modifier = Modifier.sharedElement(
                        sharedContentState = rememberSharedContentState(key = "header"),
                        animatedVisibilityScope = animatedVisibilityScope
                    ),
                    center = false
                )
                Spacer(modifier = Modifier.height(4.dp))
                MainButton(
                    modifier = Modifier.sharedElement(
                        sharedContentState = rememberSharedContentState(key = "mainButton"),
                        animatedVisibilityScope = animatedVisibilityScope
                    ),
                )
                SecondaryButton(
                    modifier = Modifier.sharedElement(
                        sharedContentState = rememberSharedContentState(key = "secondaryButton"),
                        animatedVisibilityScope = animatedVisibilityScope
                    ),
                )
            }

            // Right Column (Subheader, Body, Disclosures)
            Column(
                modifier = Modifier.weight(3f), // Takes 3/5 space
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SubheaderText(
                    modifier = Modifier.sharedElement(
                        sharedContentState = rememberSharedContentState(key = "subHeaderText"),
                        animatedVisibilityScope = animatedVisibilityScope
                    ),
                    center = false
                )
                BodyText(
                    modifier = Modifier.sharedElement(
                        sharedContentState = rememberSharedContentState(key = "bodyText"),
                        animatedVisibilityScope = animatedVisibilityScope
                    ),
                    center = false
                )
                Spacer(modifier = Modifier.weight(1f)) // Push disclosure to bottom if space allows
                DisclosureSection(
                    modifier = Modifier.sharedElement(
                        sharedContentState = rememberSharedContentState(key = "disclosureSection"),
                        animatedVisibilityScope = animatedVisibilityScope
                    ),
                    center = false
                )
            }
        }
    }
}

// --- Reusable Text & Button Components ---

@Composable
fun HeaderText(
    modifier: Modifier = Modifier,
    center: Boolean
) {
    Text(
        text = "Header",
        fontSize = 34.sp,
        lineHeight = 40.sp,
        color = TextDark,
        textAlign = if (center) TextAlign.Center else TextAlign.Start,
        modifier = modifier
    )
}

@Composable
fun SubheaderText(
    modifier: Modifier = Modifier,
    center: Boolean
) {
    Text(
        text = "Subheader text",
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = TextDark,
        textAlign = if (center) TextAlign.Center else TextAlign.Start,
        modifier = modifier
    )
}

@Composable
fun BodyText(
    modifier: Modifier = Modifier,
    center: Boolean
) {
    Text(
        text = "Body text lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod.",
        fontSize = 16.sp,
        color = TextDark.copy(alpha = 0.8f),
        textAlign = if (center) TextAlign.Center else TextAlign.Start,
        modifier = modifier
    )
}

@Composable
fun MainButton(
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = { /* TODO */ },
        colors = ButtonDefaults.buttonColors(
            containerColor = ButtonDark,
            contentColor = ContainerWhite
        ),
        shape = CircleShape,
        modifier = modifier.height(48.dp)
    ) {
        Text(
            text = "Button label",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}

@Composable
fun SecondaryButton(
    modifier: Modifier = Modifier,
) {
    TextButton(
        onClick = { /* TODO */ },
        modifier = modifier
    ) {
        Text(
            text = "Dismiss CTA",
            color = AccentPurple,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.width(4.dp))
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = AccentPurple
        )
    }
}

@Composable
fun DisclosureSection(
    modifier: Modifier = Modifier,
    center: Boolean
) {
    Column(
        modifier = modifier,
        horizontalAlignment = if (center) Alignment.CenterHorizontally else Alignment.Start
    ) {
        Text(
            text = "Disclosures",
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = TextDark
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Terms and conditions apply. For more information, view Wells Fargo's Online Access Agreement.",
            fontSize = 12.sp,
            color = DisclosureGray,
            textAlign = if (center) TextAlign.Center else TextAlign.Start
        )
    }
}

// --- Previews ---

@Preview(name = "Portrait Mode", device = "spec:width=411dp,height=891dp,dpi=420")
@Composable
fun PreviewPortrait() {
    MaterialTheme {
        ResponsiveCurvedLayoutScreen()
    }
}

@Preview(name = "Landscape Mode", device = "spec:width=891dp,height=411dp,dpi=420,orientation=landscape")
@Composable
fun PreviewLandscape() {
    MaterialTheme {
        ResponsiveCurvedLayoutScreen()
    }
}