package com.dhimandasgupta.funposables.composables.kenburns

import android.graphics.drawable.BitmapDrawable
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.HorizontalCenteredHeroCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.palette.graphics.Palette
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.dhimandasgupta.funposables.R
import com.dhimandasgupta.funposables.ui.common.DeviceLayoutType
import com.dhimandasgupta.funposables.ui.common.getDeviceLayoutType
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.random.Random
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber

enum class AnimationSpeed(val durationType: Float) {
  VERY_FAST(durationType = 0.25f),
  FAST(durationType = 0.5f),
  NORMAL(durationType = 1f),
  SLOW(durationType = 1.5f),
  VERY_SLOW(durationType = 2f),
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun KenBurnsEffectPane(modifier: Modifier = Modifier) {
  val items =
    persistentListOf(
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
  val deviceLayoutType = getDeviceLayoutType()

  val carousalModifier =
    when (deviceLayoutType) {
      DeviceLayoutType.PHONE_PORTRAIT -> Modifier.fillMaxWidth().fillMaxHeight(0.4f)
      else -> Modifier.fillMaxSize(0.75f)
    }

  val carousalState = rememberCarouselState(initialItem = 0, itemCount = { items.size })

  val palettes = remember { mutableStateMapOf<Int, Palette>() }
  // Derived rather than copied through an effect: it tracks both the settled item and a palette
  // arriving later for that item, without restarting anything on each change.
  val currentPalette by remember { derivedStateOf { palettes[carousalState.currentItem] } }
  var animationSpeed by remember { mutableStateOf(AnimationSpeed.NORMAL) }

  Column(
    modifier =
      modifier
        .fillMaxSize()
        .padding(
          start =
            WindowInsets.displayCutout
              .union(WindowInsets.navigationBars)
              .asPaddingValues()
              .calculateStartPadding(LayoutDirection.Ltr),
          top =
            WindowInsets.displayCutout
              .union(WindowInsets.statusBars)
              .asPaddingValues()
              .calculateTopPadding(),
          end =
            WindowInsets.displayCutout
              .union(WindowInsets.navigationBars)
              .asPaddingValues()
              .calculateEndPadding(LayoutDirection.Ltr),
          bottom =
            WindowInsets.displayCutout
              .union(WindowInsets.navigationBars)
              .asPaddingValues()
              .calculateBottomPadding(),
        )
        .verticalScroll(state = rememberScrollState()),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    AnimationSpeedSelectorSection(
      animationSpeed = animationSpeed,
      onAnimationSpeedChange = { selectedAnimationSpeed ->
        animationSpeed = selectedAnimationSpeed
      },
    )

    HorizontalCenteredHeroCarousel(
      state = carousalState,
      itemSpacing = 8.dp,
      modifier = carousalModifier.padding(horizontal = 16.dp),
      contentPadding = PaddingValues(horizontal = 0.dp),
    ) { itemIndex ->
      ApplyKenBurnsEffect(
        modifier = Modifier.fillMaxWidth().aspectRatio(1f).maskClip(RoundedCornerShape(16.dp)),
        drawableResourceId = items[itemIndex],
        animationSpeed = animationSpeed,
        isSelected = carousalState.currentItem == itemIndex,
        indexForPallet = itemIndex,
        updatePalette = { index, newPalette ->
          Timber.tag("KenBurnsEffectPane").d("Item received: $index")
          palettes[itemIndex] = newPalette
        },
      )
    }

    CurrentImagePalettePane(currentPalette)
  }
}

@Composable
private fun AnimationSpeedSelectorSection(
  animationSpeed: AnimationSpeed,
  onAnimationSpeedChange: (AnimationSpeed) -> Unit,
) {
  FlowRow(
    modifier =
      Modifier.padding(
        horizontal = 16.dp,
        vertical = 32.dp,
      ),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalArrangement = Arrangement.Center,
    maxItemsInEachRow = Int.MAX_VALUE,
  ) {
    AnimationSpeed.entries.forEach { animationSpeedEntry ->
      Text(
        text = animationSpeedEntry.name.toUpperCase(LocalLocale.current).replace("_", " "),
        style =
          if (animationSpeed.durationType != animationSpeedEntry.durationType) typography.titleSmall
          else typography.titleLarge,
        modifier =
          Modifier.padding(horizontal = 8.dp)
            .clickable(
              onClick = {
                if (animationSpeed.durationType != animationSpeedEntry.durationType)
                  onAnimationSpeedChange(animationSpeedEntry)
              }
            ),
      )
    }
  }
}

@Composable
private fun ApplyKenBurnsEffect(
  modifier: Modifier = Modifier,
  drawableResourceId: Int,
  animationSpeed: AnimationSpeed,
  isSelected: Boolean,
  indexForPallet: Int,
  updatePalette: (Int, Palette) -> Unit,
) {
  key(drawableResourceId, indexForPallet) {
    val scope = rememberCoroutineScope()

    val painter = painterResource(id = drawableResourceId)
    val imageSize = painter.intrinsicSize
    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    // Derived so the layout-phase size write is only ever read from the effect and the layer
    // below, never from composition.
    val geometry by
      remember(imageSize) { derivedStateOf { KenBurnsGeometry.of(imageSize, containerSize) } }

    // Remembered so recomposition does not hand AsyncImage a new (non-equal) request and restart
    // the load, crossfade, and palette generation.
    val context = LocalContext.current
    val imageRequest =
      remember(drawableResourceId) {
        ImageRequest.Builder(context)
          .memoryCacheKey(drawableResourceId.toString())
          .diskCacheKey(drawableResourceId.toString())
          .data(drawableResourceId)
          .allowHardware(false) // Required for the Palette API
          .crossfade(true)
          .listener { _, result ->
            scope.launch(Dispatchers.Default) {
              val bitmap = (result.drawable as? BitmapDrawable)?.bitmap ?: return@launch
              Timber.tag("KenBurnsEffectPane").d("Item produce: $indexForPallet")
              updatePalette(indexForPallet, Palette.from(bitmap).generate())
            }
          }
          .build()
      }

    // The resting pose is the plain crop, which is inside the bounds for every geometry.
    val zoom = remember { Animatable(KenBurnsGeometry.MIN_ZOOM) }
    val panX = remember { Animatable(0f) }
    val panY = remember { Animatable(0f) }

    // Only the carousel's current item animates. Deselecting cancels the in-flight animation and
    // freezes the Animatables where they are; reselecting resumes from that position.
    LaunchedEffect(key1 = drawableResourceId, key2 = animationSpeed, key3 = isSelected) {
      if (!isSelected) return@LaunchedEffect
      snapshotFlow { geometry }
        .collectLatest { geometry ->
          if (geometry == null) return@collectLatest
          Timber.tag("KenBurnsEffectPane").d("Geometry: $geometry")
          val spec =
            tween<Float>(
              durationMillis = (10000 * animationSpeed.durationType).toInt(),
              easing = FastOutSlowInEasing,
            )
          while (isActive) {
            // All three properties share one spec, so every frame is a plain lerp between two
            // in-bounds poses; the pan limit is linear in zoom, so the lerp stays in bounds too.
            val pose = geometry.randomPose()
            coroutineScope {
              launch { zoom.animateTo(pose.zoom, spec) }
              launch { panX.animateTo(pose.panX, spec) }
              launch { panY.animateTo(pose.panY, spec) }
            }
          }
        }
    }

    Card(
      modifier = modifier,
      shape = RoundedCornerShape(16.dp),
    ) {
      Box(
        modifier = Modifier.fillMaxSize().clipToBounds(),
        contentAlignment = Alignment.Center,
      ) {
        AsyncImage(
          model = imageRequest,
          contentDescription = "kenburns image",
          contentScale = ContentScale.Crop,
          modifier =
            Modifier.fillMaxSize()
              .onSizeChanged { intSize -> containerSize = intSize }
              .graphicsLayer {
                val bounds = geometry ?: return@graphicsLayer
                transformOrigin = TransformOrigin.Center
                val currentZoom = zoom.value.coerceAtLeast(KenBurnsGeometry.MIN_ZOOM)
                scaleX = currentZoom
                scaleY = currentZoom
                // The animation already targets in-bounds poses; this clamp is the safety net
                // for a resize mid-segment, so the container behind the image is never exposed.
                val maxPanX = bounds.maxPanX(currentZoom)
                val maxPanY = bounds.maxPanY(currentZoom)
                translationX = panX.value.coerceIn(-maxPanX, maxPanX)
                translationY = panY.value.coerceIn(-maxPanY, maxPanY)
              }
              .layoutAsCrop(imageSize),
        )
      }
    }
  }
}

/** One zoom/pan pose of the image; [panX] and [panY] are pixel translations of the container. */
private data class KenBurnsPose(val zoom: Float, val panX: Float, val panY: Float)

/**
 * Geometry of a [ContentScale.Crop]-sized image inside its container. [displayedWidth] and
 * [displayedHeight] are the crop-scaled image dimensions: at least one equals the container and the
 * other carries the crop overflow, which panning can reveal instead of it being clipped away.
 */
private data class KenBurnsGeometry(val imageSize: Size, val containerSize: Size) {
  private val cropScale =
    max(containerSize.width / imageSize.width, containerSize.height / imageSize.height)
  val displayedWidth = imageSize.width * cropScale
  val displayedHeight = imageSize.height * cropScale

  /** Zooming past 1:1 image-to-screen pixels only blurs, so cap there when the image allows it. */
  val maxZoom = (1f / cropScale).coerceIn(MIN_MAX_ZOOM, MAX_MAX_ZOOM)

  /** Largest horizontal translation at [zoom] that keeps the image covering the container. */
  fun maxPanX(zoom: Float) = ((displayedWidth * zoom - containerSize.width) / 2f).coerceAtLeast(0f)

  /** Largest vertical translation at [zoom] that keeps the image covering the container. */
  fun maxPanY(zoom: Float) =
    ((displayedHeight * zoom - containerSize.height) / 2f).coerceAtLeast(0f)

  fun randomPose(): KenBurnsPose {
    val zoom = Random.nextDouble(MIN_ZOOM.toDouble(), maxZoom.toDouble()).toFloat()
    return KenBurnsPose(
      zoom = zoom,
      panX = randomWithin(maxPanX(zoom)),
      panY = randomWithin(maxPanY(zoom)),
    )
  }

  private fun randomWithin(limit: Float): Float =
    if (limit <= 0f) 0f else Random.nextDouble(-limit.toDouble(), limit.toDouble()).toFloat()

  companion object {
    const val MIN_ZOOM = 1f
    private const val MIN_MAX_ZOOM = 1.25f
    private const val MAX_MAX_ZOOM = 1.6f

    fun of(imageSize: Size, containerSize: IntSize): KenBurnsGeometry? {
      if (containerSize.width <= 0 || containerSize.height <= 0) return null
      if (!imageSize.isSpecified || imageSize.width <= 0f || imageSize.height <= 0f) return null
      return KenBurnsGeometry(imageSize, containerSize.toSize())
    }
  }
}

/**
 * Measures the content at its [ContentScale.Crop] size for the incoming constraints and centres it,
 * so the crop overflow spills past the container (where the parent clips it) instead of being cut
 * off inside the image. Reports the container size to the parent.
 */
private fun Modifier.layoutAsCrop(imageSize: Size): Modifier = layout { measurable, constraints ->
  val geometry =
    if (constraints.hasBoundedWidth && constraints.hasBoundedHeight)
      KenBurnsGeometry.of(imageSize, IntSize(constraints.maxWidth, constraints.maxHeight))
    else null
  if (geometry == null) {
    val placeable = measurable.measure(constraints)
    return@layout layout(placeable.width, placeable.height) { placeable.place(0, 0) }
  }
  val width = ceil(geometry.displayedWidth).roundToInt()
  val height = ceil(geometry.displayedHeight).roundToInt()
  val placeable = measurable.measure(Constraints.fixed(width, height))
  layout(constraints.maxWidth, constraints.maxHeight) {
    placeable.place((constraints.maxWidth - width) / 2, (constraints.maxHeight - height) / 2)
  }
}

@Suppress("ParamsComparedByRef")
@Composable
private fun CurrentImagePalettePane(currentPalette: Palette?) {
  FlowRow(
    modifier =
      Modifier.padding(
        horizontal = 16.dp,
        vertical = 32.dp,
      ),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalArrangement = Arrangement.spacedBy(4.dp),
    maxItemsInEachRow = Int.MAX_VALUE,
  ) {
    currentPalette?.swatches?.asSequence()?.filterNotNull()?.forEach { swatch ->
      Box(
        modifier =
          Modifier.size(36.dp)
            .background(
              color = Color(swatch.rgb),
              shape = RoundedCornerShape(8.dp),
            )
      )
    }
  }
}
