package com.dhimandasgupta.funposables.composables

import android.app.Activity
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.carousel.HorizontalCenteredHeroCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.dhimandasgupta.funposables.R
import com.dhimandasgupta.funposables.ui.common.DeviceLayoutType
import com.dhimandasgupta.funposables.ui.common.getDeviceLayoutType
import timber.log.Timber
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun KenBurnsEffectPane(
    modifier: Modifier = Modifier
) {
    val items = listOf(
        R.drawable.wallpaper_01,
        R.drawable.wallpaper_02,
        R.drawable.wallpaper_03,
        R.drawable.wallpaper_04,
        R.drawable.wallpaper_05,
        R.drawable.wallpaper_06,
        R.drawable.wallpaper_07,
        R.drawable.wallpaper_08,
        R.drawable.wallpaper_09,
    )
    val activity: Activity? = LocalActivity.current
    requireNotNull(activity)
    val deviceLayoutType = getDeviceLayoutType(windowSizeClass = calculateWindowSizeClass(activity))

    val carousalModifier = when (deviceLayoutType) {
        DeviceLayoutType.PHONE_PORTRAIT -> Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.25f)
        else -> Modifier
            .fillMaxSize(0.75f)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(
                state = rememberScrollState()
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        HorizontalCenteredHeroCarousel(
            state = rememberCarouselState(
                initialItem = items.size / 2,
                itemCount = { items.size }
            ),
            itemSpacing = 8.dp,
            modifier = carousalModifier,
            contentPadding = PaddingValues(horizontal = 32.dp),
        ) { itemIndex ->
            ApplyKenBurnsEffect(
                modifier = Modifier,
                drawableResourceId = items[itemIndex]
            )
        }
    }
}

@Composable
fun ApplyKenBurnsEffect(
    modifier: Modifier = Modifier,
    drawableResourceId: Int,
) {
    key(drawableResourceId) {

        val painter = painterResource(id = drawableResourceId)
        val imageSize = painter.intrinsicSize
        var containerSize by remember { mutableStateOf(IntSize.Zero) }

        val imageRequest = ImageRequest.Builder(LocalContext.current)
            .data(drawableResourceId)
            .crossfade(true)
            .build()

        LaunchedEffect(key1 = imageSize, key2 = containerSize) {
            Timber.tag("KenBurnsEffectPane").d("Image size: $imageSize")
            Timber.tag("KenBurnsEffectPane").d("Container size: $containerSize")
        }

        val infiniteTransition = rememberInfiniteTransition(label = "KenBurns")

        val scale by infiniteTransition.animateFloat(
            initialValue = Random.nextDouble(1.5, 2.9).toFloat(),
            targetValue = Random.nextDouble(3.0, 5.0).toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 10000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "scale"
        )

        val panningX by infiniteTransition.animateFloat(
            initialValue = Random.nextDouble(-0.01, 0.0).toFloat(),
            targetValue = Random.nextDouble(0.0, 0.01).toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 12000, easing = LinearOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "panningX"
        )

        val panningY by infiniteTransition.animateFloat(
            initialValue = Random.nextDouble(-0.01, 0.0).toFloat(),
            targetValue = Random.nextDouble(0.0, 0.01).toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 8000, easing = FastOutLinearInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "panningY"
        )

        Box(
            modifier = modifier
                .fillMaxSize()
                .clipToBounds()
                .border(
                    width = 1.dp,
                    color = colorScheme.onBackground
                ),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = imageRequest,
                contentDescription = "kenburns image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(200.dp)
                    .onSizeChanged { intSize -> containerSize = intSize }
                    .graphicsLayer {
                        this.transformOrigin = TransformOrigin.Center

                        if (containerSize != IntSize.Zero) {
                            this.clip = false
                            this.scaleX = scale
                            this.scaleY = scale

                            val scaledImageWidth = imageSize.width * scale
                            val scaledImageHeight = imageSize.height * scale

                            val maxTranslationX = (scaledImageWidth - containerSize.width)
                                .coerceAtLeast(0f) / 2f
                            val maxTranslationY = (scaledImageHeight - containerSize.height)
                                .coerceAtLeast(0f) / 2f

                            this.translationX = panningX * maxTranslationX
                            this.translationY = panningY * maxTranslationY
                        }
                    }
            )
        }
    }
}
