package com.dhimandasgupta.funposables.composables.kenburns

import android.graphics.drawable.BitmapDrawable
import android.util.LruCache
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowColumn
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.CarouselState
import androidx.compose.material3.carousel.HorizontalCenteredHeroCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.movableContentOf
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.palette.graphics.Palette
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.dhimandasgupta.funposables.R
import com.dhimandasgupta.funposables.ui.common.layoutAtLookaheadSize
import kotlin.random.Random
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
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

/**
 * Swatch colors per drawable resource. A resource's palette never changes, so carousel items that
 * scroll back into view, or a revisit of the screen, reuse it instead of running [Palette] again.
 * Thread-safe, as it is written from [Dispatchers.Default] and read on the main thread.
 */
private val swatchColorsCache = LruCache<Int, ImmutableList<Color>>(32)

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
  val carousalState = rememberCarouselState(initialItem = 0, itemCount = { items.size })

  val swatchColorsByItem = remember { mutableStateMapOf<Int, ImmutableList<Color>>() }
  val currentSwatchColors by
    remember(carousalState.currentItem) {
      derivedStateOf { swatchColorsByItem[carousalState.currentItem] }
    }
  var animationSpeed by remember { mutableStateOf(AnimationSpeed.NORMAL) }

  val speedSelector = remember {
    movableContentOf { selectorModifier: Modifier, useRow: Boolean, selectedSpeed: AnimationSpeed ->
      AnimationSpeedSelectorSection(
        modifier = selectorModifier,
        useRow = useRow,
        animationSpeed = selectedSpeed,
        onAnimationSpeedChange = { animationSpeed = it },
      )
    }
  }
  val carousel = remember {
    movableContentOf { carouselModifier: Modifier, carouselAnimationSpeed: AnimationSpeed ->
      KenBurnsCarousel(
        modifier = carouselModifier,
        carousalState = carousalState,
        items = items,
        animationSpeed = carouselAnimationSpeed,
        onSwatchColors = { index, swatchColors ->
          Timber.tag("KenBurnsEffectPane").d("Item received: $index")
          swatchColorsByItem[index] = swatchColors
        },
      )
    }
  }
  val palettePane = remember {
    movableContentOf { paneModifier: Modifier, useRow: Boolean, swatchColors: ImmutableList<Color>?
      ->
      CurrentImagePalettePane(modifier = paneModifier, swatchColors = swatchColors, useRow = useRow)
    }
  }

  BoxWithConstraints(modifier = modifier.fillMaxSize().layoutAtLookaheadSize()) {
    val shouldLayoutHorizontally = constraints.hasBoundedHeight && maxWidth > maxHeight
    if (shouldLayoutHorizontally) {
      LayoutHorizontally(
        Modifier,
        animationSpeed,
        currentSwatchColors,
        speedSelector,
        carousel,
        palettePane,
      )
    } else {
      LayoutVertically(
        Modifier,
        animationSpeed,
        currentSwatchColors,
        speedSelector,
        carousel,
        palettePane,
      )
    }
  }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun LayoutVertically(
  modifier: Modifier,
  animationSpeed: AnimationSpeed,
  currentSwatchColors: ImmutableList<Color>?,
  speedSelector: @Composable (Modifier, Boolean, AnimationSpeed) -> Unit,
  carousel: @Composable (Modifier, AnimationSpeed) -> Unit,
  palettePane: @Composable (Modifier, Boolean, ImmutableList<Color>?) -> Unit,
) {
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
          end =
            WindowInsets.displayCutout
              .union(WindowInsets.navigationBars)
              .asPaddingValues()
              .calculateEndPadding(LayoutDirection.Ltr),
        )
        .verticalScroll(state = rememberScrollState()),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    Spacer(
      modifier =
        Modifier.fillMaxWidth()
          .height(
            WindowInsets.displayCutout
              .union(WindowInsets.statusBars)
              .asPaddingValues()
              .calculateTopPadding()
          )
    )

    speedSelector(Modifier, true, animationSpeed)

    carousel(
      Modifier.fillMaxWidth().fillMaxHeight(0.4f).padding(horizontal = 16.dp),
      animationSpeed,
    )

    palettePane(Modifier, true, currentSwatchColors)

    Spacer(
      modifier =
        Modifier.fillMaxWidth()
          .height(
            WindowInsets.displayCutout
              .union(WindowInsets.navigationBars)
              .asPaddingValues()
              .calculateBottomPadding()
          )
    )
  }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun LayoutHorizontally(
  modifier: Modifier,
  animationSpeed: AnimationSpeed,
  currentSwatchColors: ImmutableList<Color>?,
  speedSelector: @Composable (Modifier, Boolean, AnimationSpeed) -> Unit,
  carousel: @Composable (Modifier, AnimationSpeed) -> Unit,
  palettePane: @Composable (Modifier, Boolean, ImmutableList<Color>?) -> Unit,
) {
  Row(
    modifier =
      modifier
        .fillMaxSize()
        .padding(
          start =
            WindowInsets.displayCutout
              .union(WindowInsets.navigationBars)
              .asPaddingValues()
              .calculateStartPadding(LayoutDirection.Ltr),
          end =
            WindowInsets.displayCutout
              .union(WindowInsets.navigationBars)
              .asPaddingValues()
              .calculateEndPadding(LayoutDirection.Ltr),
        ),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    speedSelector(Modifier.weight(1f), false, animationSpeed)

    carousel(
      Modifier.weight(2f).fillMaxHeight(0.75f).padding(horizontal = 16.dp),
      animationSpeed,
    )

    palettePane(Modifier.weight(1f), false, currentSwatchColors)
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun KenBurnsCarousel(
  carousalState: CarouselState,
  items: ImmutableList<Int>,
  animationSpeed: AnimationSpeed,
  onSwatchColors: (Int, ImmutableList<Color>) -> Unit,
  modifier: Modifier = Modifier,
) {
  HorizontalCenteredHeroCarousel(
    state = carousalState,
    itemSpacing = 8.dp,
    modifier = modifier,
    contentPadding = PaddingValues(horizontal = 0.dp),
  ) { itemIndex ->
    ApplyKenBurnsEffect(
      modifier = Modifier.fillMaxWidth().aspectRatio(1f).maskClip(RoundedCornerShape(16.dp)),
      drawableResourceId = items[itemIndex],
      animationSpeed = animationSpeed,
      isSelected = carousalState.currentItem == itemIndex,
      indexForPallet = itemIndex,
      updateSwatchColors = onSwatchColors,
    )
  }
}

@Composable
private fun AnimationSpeedSelectorSection(
  modifier: Modifier = Modifier,
  useRow: Boolean,
  animationSpeed: AnimationSpeed,
  onAnimationSpeedChange: (AnimationSpeed) -> Unit,
) {
  val paddedModifier =
    modifier.padding(
      horizontal = 16.dp,
      vertical = 32.dp,
    )
  val speedEntries: @Composable () -> Unit = {
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

  if (useRow) {
    FlowRow(
      modifier = paddedModifier,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalArrangement = Arrangement.Center,
      maxItemsInEachRow = Int.MAX_VALUE,
    ) {
      speedEntries()
    }
  } else {
    FlowColumn(
      modifier = paddedModifier,
      verticalArrangement = Arrangement.spacedBy(8.dp),
      horizontalArrangement = Arrangement.Start,
      maxItemsInEachColumn = Int.MAX_VALUE,
    ) {
      speedEntries()
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
  updateSwatchColors: (Int, ImmutableList<Color>) -> Unit,
) {
  key(drawableResourceId, indexForPallet) {
    val scope = rememberCoroutineScope()

    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    val geometry by remember { derivedStateOf { KenBurnsGeometry.of(containerSize) } }

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
            swatchColorsCache.get(drawableResourceId)?.let { cachedSwatchColors ->
              updateSwatchColors(indexForPallet, cachedSwatchColors)
              return@listener
            }
            scope.launch(Dispatchers.Default) {
              val bitmap = (result.drawable as? BitmapDrawable)?.bitmap ?: return@launch
              Timber.tag("KenBurnsEffectPane").d("Item produce: $indexForPallet")
              val swatchColors =
                Palette.from(bitmap).generate().swatches.map { Color(it.rgb) }.toImmutableList()
              swatchColorsCache.put(drawableResourceId, swatchColors)
              updateSwatchColors(indexForPallet, swatchColors)
            }
          }
          .build()
      }

    val scale = remember { Animatable(KenBurnsGeometry.MIN_SCALE) }
    val panX = remember { Animatable(0f) }
    val panY = remember { Animatable(0f) }

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
            val pose = geometry.randomPose()
            coroutineScope {
              launch { scale.animateTo(pose.scale, spec) }
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
                transformOrigin = TransformOrigin.Center
                val currentScale = scale.value.coerceAtLeast(1f)
                scaleX = currentScale
                scaleY = currentScale
                val maxPanX = (size.width * (currentScale - 1f)).coerceAtLeast(0f) / 2f
                val maxPanY = (size.height * (currentScale - 1f)).coerceAtLeast(0f) / 2f
                translationX = panX.value.coerceIn(-maxPanX, maxPanX)
                translationY = panY.value.coerceIn(-maxPanY, maxPanY)
              },
        )
      }
    }
  }
}

/** One zoom/pan pose of the image; [panX] and [panY] are pixel translations of the container. */
private data class KenBurnsPose(val scale: Float, val panX: Float, val panY: Float)

/** Geometry of a [ContentScale.Crop]-sized image inside its container. */
private data class KenBurnsGeometry(val containerSize: Size) {
  fun maxPanX(scale: Float) = ((containerSize.width * (scale - 1f)) / 2f).coerceAtLeast(0f)

  fun maxPanY(scale: Float) = ((containerSize.height * (scale - 1f)) / 2f).coerceAtLeast(0f)

  fun randomPose(): KenBurnsPose {
    val scale = Random.nextDouble(MIN_SCALE.toDouble(), MAX_SCALE.toDouble()).toFloat()
    val maxPanX = maxPanX(scale)
    val maxPanY = maxPanY(scale)
    return KenBurnsPose(
      scale = scale,
      panX = randomWithin(maxPanX),
      panY = randomWithin(maxPanY),
    )
  }

  private fun randomWithin(limit: Float): Float =
    if (limit <= 0f) 0f else Random.nextDouble(-limit.toDouble(), limit.toDouble()).toFloat()

  companion object {
    const val MIN_SCALE = 1.15f
    const val MAX_SCALE = 1.45f

    fun of(containerSize: IntSize): KenBurnsGeometry? {
      if (containerSize.width <= 0 || containerSize.height <= 0) return null
      return KenBurnsGeometry(containerSize.toSize())
    }
  }
}

@Composable
private fun CurrentImagePalettePane(
  swatchColors: ImmutableList<Color>?,
  useRow: Boolean,
  modifier: Modifier = Modifier,
) {
  val paddedModifier =
    modifier.padding(
      horizontal = 16.dp,
      vertical = 32.dp,
    )
  val swatches: @Composable () -> Unit = {
    val swatchShape = RoundedCornerShape(8.dp)
    val borderColor = colorScheme.onSurface
    swatchColors?.forEach { swatchColor ->
      Box(
        modifier =
          Modifier.size(36.dp)
            .background(
              color = swatchColor,
              shape = swatchShape,
            )
            .border(
              width = 0.5.dp,
              color = borderColor,
              shape = swatchShape,
            )
      )
    }
  }

  if (useRow) {
    FlowRow(
      modifier = paddedModifier,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalArrangement = Arrangement.spacedBy(4.dp),
      maxItemsInEachRow = Int.MAX_VALUE,
    ) {
      swatches()
    }
  } else {
    FlowColumn(
      modifier = paddedModifier,
      verticalArrangement = Arrangement.spacedBy(8.dp),
      horizontalArrangement = Arrangement.spacedBy(4.dp),
      maxItemsInEachColumn = Int.MAX_VALUE,
    ) {
      swatches()
    }
  }
}
