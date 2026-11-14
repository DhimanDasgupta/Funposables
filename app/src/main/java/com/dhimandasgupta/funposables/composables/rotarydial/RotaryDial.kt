package com.dhimandasgupta.funposables.composables.rotarydial

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.calculateTargetValue
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitTouchSlopOrCancellation
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker1D
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.constrainWidth
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.util.fastMap
import androidx.compose.ui.util.lerp
import com.dhimandasgupta.funposables.R
import com.dhimandasgupta.funposables.ui.theme.FunposablesTheme
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch

/**
 * Demo screen: the dial placed according to [dialPosition] and given the whole pane. A small card
 * floats in a corner the dial leaves free, showing the item the dial last settled on and a dropdown
 * menu to switch the position at runtime.
 */
@Composable
fun RotaryDialPane(
  modifier: Modifier = Modifier,
  dialPosition: DialPosition = DialPosition.CENTER,
) {
  var selected by remember { mutableStateOf(PreviewDialItems.first()) }
  var currentPosition by rememberSaveable(dialPosition) { mutableStateOf(dialPosition) }
  Box(modifier = modifier.fillMaxSize()) {
    RotaryDial(
      items = PreviewDialItems,
      modifier = Modifier.align(currentPosition.alignment).dialFrame(currentPosition),
      dialPosition = currentPosition,
      onSelectedItemChange = { selected = it },
    )
    DialControls(
      selected = selected,
      position = currentPosition,
      onPositionChange = { currentPosition = it },
      modifier =
        Modifier.align(currentPosition.controlsAlignment)
          .windowInsetsPadding(WindowInsets.safeDrawing)
          .padding(16.dp),
    )
  }
}

/**
 * One entry of a [RotaryDial].
 *
 * @param id Stable, unique identifier. Also used as the accessibility description.
 * @param drawableRes Icon drawn for the item. It is tinted by the dial, so single-color vectors
 *   work best.
 * @param angle Angle in degrees, clockwise from the selection pointer at 12 o'clock, that the item
 *   occupies on the dial. The dial assigns it to every visible item and to the item it reports as
 *   selected; the value you pass in is ignored.
 */
@Immutable
data class RotaryDialItem(
  val id: String,
  @DrawableRes val drawableRes: Int,
  val angle: Float = 0f,
)

@Immutable
data class RotaryDialColors(
  val dial: Color = Color(0xFF122B28),
  val highlight: Color = Color(0xFF9CCBB7),
  val divider: Color = Color(0xFF040C0B),
  val icon: Color = Color(0xFFB4C4BE),
  val selectedIcon: Color = Color(0xFF34504A),
  val pointer: Color = Color(0xFFE4E8E6),
  /** Light caught by the crest of the dial's rounded outer edge, and by its lit side. */
  val rimHighlight: Color = Color.White.copy(alpha = 0.10f),
  /** Shade where the outer edge curves away, on its unlit side, and under the inner ring. */
  val rimShadow: Color = Color.Black.copy(alpha = 0.5f),
)

/**
 * Where a [RotaryDial] sits on screen. Apart from [CENTER], the dial is pushed against one edge of
 * its layout and only part of it is visible: the layout covers the visible part, clips the rest,
 * and puts the selection pointer in the middle of what is visible. Align the dial to the matching
 * edge of its parent with [alignment].
 *
 * How much shows is measured on the dial's disk (the dark wheel holding the items), as a fraction
 * of its diameter: half for the plain edge positions, less for the `_MEDIUM` and `_SMALL` ones (see
 * each entry). The pointer and the highlight's overhang beyond the disk are always visible too.
 *
 * Start and end follow the layout direction, so in right-to-left layouts the `START_CENTER`
 * positions are on the right edge and show the left part of the dial.
 */
enum class DialPosition(
  /** The edge the dial sits on in a left-to-right layout. */
  private val ltrEdge: DialEdge,
  /** Fraction of the disk's diameter that is visible. */
  private val visibleFraction: Float,
) {
  /** At the start edge, half visible; the selection is the end-most item. */
  START_CENTER(DialEdge.Left, 0.5f),

  /** At the start edge, 30% of the disk visible; the selection is the end-most item. */
  START_CENTER_MEDIUM(DialEdge.Left, 0.30f),

  /** At the start edge, 20% of the disk visible; the selection is the end-most item. */
  START_CENTER_SMALL(DialEdge.Left, 0.20f),

  /** At the end edge, half visible; the selection is the start-most item. */
  END_CENTER(DialEdge.Right, 0.5f),

  /** At the end edge, 30% of the disk visible; the selection is the start-most item. */
  END_CENTER_MEDIUM(DialEdge.Right, 0.30f),

  /** At the end edge, 20% of the disk visible; the selection is the start-most item. */
  END_CENTER_SMALL(DialEdge.Right, 0.20f),

  /** At the top edge, half visible; the selection is the bottom-most item. */
  TOP_CENTER(DialEdge.Top, 0.5f),

  /** At the top edge, a quarter of the disk visible; the selection is the bottom-most item. */
  TOP_CENTER_MEDIUM(DialEdge.Top, 0.25f),

  /** At the top edge, 15% of the disk visible; the selection is the bottom-most item. */
  TOP_CENTER_SMALL(DialEdge.Top, 0.15f),

  /** At the bottom edge, half visible; the selection is the top-most item. */
  BOTTOM_CENTER(DialEdge.Bottom, 0.5f),

  /** At the bottom edge, a quarter of the disk visible; the selection is the top-most item. */
  BOTTOM_CENTER_MEDIUM(DialEdge.Bottom, 0.25f),

  /** At the bottom edge, 15% of the disk visible; the selection is the top-most item. */
  BOTTOM_CENTER_SMALL(DialEdge.Bottom, 0.15f),

  /** The whole dial is shown; the selection is the top-most item. */
  CENTER(DialEdge.None, 1f);

  /** The alignment that puts the dial against its edge of the parent. */
  val alignment: Alignment
    get() =
      when (ltrEdge) {
        DialEdge.Left -> Alignment.CenterStart
        DialEdge.Right -> Alignment.CenterEnd
        DialEdge.Top -> Alignment.TopCenter
        DialEdge.Bottom -> Alignment.BottomCenter
        DialEdge.None -> Alignment.Center
      }

  /** Resolves start/end to the physical left/right edge. */
  internal fun placement(layoutDirection: LayoutDirection): DialPlacement {
    val edge =
      when {
        layoutDirection == LayoutDirection.Ltr -> ltrEdge
        ltrEdge == DialEdge.Left -> DialEdge.Right
        ltrEdge == DialEdge.Right -> DialEdge.Left
        else -> ltrEdge
      }
    return DialPlacement(edge, visibleFraction)
  }
}

/**
 * A physical edge of the layout, with the pointer angle (clockwise from 12 o'clock) facing away
 * from it.
 */
internal enum class DialEdge(val selectionAngle: Float) {
  Left(90f),
  Right(270f),
  Top(180f),
  Bottom(0f),
  None(0f),
}

/**
 * A [DialPosition] resolved for the layout direction: the physical edge and how much is visible.
 */
@Immutable
internal data class DialPlacement(val edge: DialEdge, val visibleFraction: Float) {
  val selectionAngle: Float
    get() = edge.selectionAngle

  /**
   * Visible extent across the edge, in units of the dial's half side: the visible part of the disk
   * plus the margin outside it that holds the pointer.
   */
  private val visibleExtent: Float
    get() =
      if (edge == DialEdge.None) 2f
      else 2f * OuterRadiusRatio * visibleFraction + (1f - OuterRadiusRatio)

  /** Layout width, in units of the dial's half side. */
  val widthInHalfSides: Float
    get() = if (edge == DialEdge.Left || edge == DialEdge.Right) visibleExtent else 2f

  /** Layout height, in units of the dial's half side. */
  val heightInHalfSides: Float
    get() = if (edge == DialEdge.Top || edge == DialEdge.Bottom) visibleExtent else 2f
}

/**
 * A wheel that shows [items] around a circle, with the selected item under a fixed highlight. The
 * highlight is at the top unless [dialPosition] puts it elsewhere. Each icon is tilted by its angle
 * from the selection, so only the selected one is upright.
 *
 * Drag on the outer ring (the band holding the items) to rotate the wheel with your finger. Drags
 * that start on the inner ring, the center or outside the dial are ignored. When the finger lifts,
 * the wheel flings and then snaps so that one item sits exactly under the pointer. Tapping an item
 * rotates it to the pointer. A light haptic tick plays each time an item crosses the pointer while
 * the wheel moves, and a firm one when it comes to rest.
 *
 * At most [maxVisibleItems] items are composed at a time. With more items than that, the wheel is a
 * sliding window over [items]: each time the wheel turns by one item, the item that moved furthest
 * from the pointer is dropped and the next item from [items] is inserted in its place. That swap
 * happens opposite the pointer, so it is never visible in the half of the dial around the pointer.
 * [items] wraps around, so the wheel can be turned forever in either direction.
 *
 * Everything (the wheel, the highlight, the pointer and the icons) is sized relative to the space
 * the layout gets, so sizing it through [modifier] scales the whole dial. Without a size constraint
 * it is [DefaultDialSize] square.
 *
 * @param items The full list of items. Ids must be unique.
 * @param dialPosition Which edge the dial sits against, which half of it is shown, and where the
 *   selection is. See [DialPosition].
 * @param initialSelectedIndex Index into [items] shown under the pointer on first composition.
 * @param maxVisibleItems How many items are composed at a time. The items are spaced `360 /
 *   min(items.size, maxVisibleItems)` degrees apart.
 * @param pulseSelectedItem Whether the item under the pointer keeps gently scaling up and down. The
 *   pulse fades out as the item is rotated away from the pointer.
 * @param onSelectedItemChange Called with the item under the pointer each time the wheel comes to
 *   rest after a drag or tap.
 */
@Composable
fun RotaryDial(
  items: ImmutableList<RotaryDialItem>,
  modifier: Modifier = Modifier,
  dialPosition: DialPosition = DialPosition.CENTER,
  initialSelectedIndex: Int = 0,
  maxVisibleItems: Int = 10,
  colors: RotaryDialColors = RotaryDialColors(),
  pulseSelectedItem: Boolean = true,
  onSelectedItemChange: (RotaryDialItem) -> Unit = {},
) {
  require(maxVisibleItems > 0) { "maxVisibleItems must be positive, was $maxVisibleItems" }

  // Extra scale for the selected item, read only in the draw phase so pulsing never recomposes.
  val pulse: () -> Float =
    if (pulseSelectedItem) {
      val pulseScale =
        rememberInfiniteTransition(label = "selectedItemPulse")
          .animateFloat(
            initialValue = 1f,
            targetValue = SelectedItemPulseScale,
            animationSpec =
              infiniteRepeatable(
                animation = tween(SelectedItemPulseMillis, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
              ),
            label = "selectedItemPulseScale",
          )
      remember(pulseScale) { { pulseScale.value } }
    } else {
      remember { { 1f } }
    }

  val placement = dialPosition.placement(LocalLayoutDirection.current)
  val itemCount = items.size
  val visibleCount = min(itemCount, maxVisibleItems)
  // Degrees between two neighbouring items.
  val step = if (visibleCount == 0) 360f else 360f / visibleCount

  // Index of the item under the pointer when the wheel last came to rest. Survives config changes.
  var settledIndex by rememberSaveable { mutableIntStateOf(initialSelectedIndex) }

  /**
   * Rotation of the wheel in item units: the (fractional) logical index under the pointer. It grows
   * as the wheel turns counter-clockwise. The logical index `i` maps to `items[i mod size]`, which
   * makes the list wrap around.
   */
  val position = remember { Animatable(settledIndex.toFloat()) }

  // The logical index nearest the pointer. Only changes when the wheel turns past half an item,
  // so the window below is only rebuilt once per item rather than once per frame.
  val centerIndex by remember { derivedStateOf { position.value.roundToInt() } }

  val slots =
    remember(items, visibleCount, step, centerIndex) {
      if (visibleCount == 0) {
        emptyList()
      } else {
        // Window of logical indices around the pointer. For an even count the extra slot is the
        // one at the bottom (180°), which is where items are inserted and dropped.
        val first = centerIndex - (visibleCount - 1) / 2
        List(visibleCount) { k ->
          val logicalIndex = first + k
          DialSlot(
            logicalIndex = logicalIndex,
            item =
              items[logicalIndex.mod(itemCount)].copy(angle = (logicalIndex - centerIndex) * step),
          )
        }
      }
    }

  val scope = rememberCoroutineScope()
  val currentOnSelectedItemChange by rememberUpdatedState(onSelectedItemChange)
  val currentItems by rememberUpdatedState(items)
  val currentHapticFeedback by rememberUpdatedState(LocalHapticFeedback.current)

  // A light tick each time an item crosses the pointer while the wheel is still moving (dragging,
  // flinging or settling). Observed outside composition so it never triggers a recomposition.
  LaunchedEffect(position) {
    var previousCount = -1
    snapshotFlow {
        val count = currentItems.size
        // Comparing modulo the item count ignores the jump when the position is normalized after
        // a long spin, which keeps the same item under the pointer.
        count to if (count == 0) 0 else centerIndex.mod(count)
      }
      .collect { (count, _) ->
        // Skip the first value and changes caused by a new item list rather than by rotation.
        if (count == previousCount) {
          currentHapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
        }
        previousCount = count
      }
  }

  /** Animates the wheel so the logical index [target] sits under the pointer. */
  suspend fun settleAt(target: Int, initialVelocity: Float) {
    position.animateTo(
      targetValue = target.toFloat(),
      animationSpec = spring(dampingRatio = 0.72f, stiffness = Spring.StiffnessMediumLow),
      initialVelocity = initialVelocity,
    )
    // Only reached when the wheel actually came to rest: a new drag cancels animateTo first.
    currentHapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
    val snapshot = currentItems
    if (snapshot.isEmpty()) return
    // Keep the position small after long spins. Shifting by a multiple of the item count does not
    // change which items are shown or where.
    val normalized = target.mod(snapshot.size)
    position.snapTo(normalized.toFloat())
    settledIndex = normalized
    currentOnSelectedItemChange(snapshot[normalized].copy(angle = 0f))
  }

  /** Live angle of a logical index, in degrees clockwise from the selection pointer. */
  fun angleOf(logicalIndex: Int): Float = (logicalIndex - position.value) * step

  Layout(
    modifier =
      modifier
        // Hides the half of the dial that is off-screen for the edge positions.
        .clipToBounds()
        .dialBackground(
          colors = colors,
          visibleCount = visibleCount,
          placement = placement,
          rotation = { -position.value * step },
        )
        .pointerInput(visibleCount, placement) {
          if (visibleCount == 0) return@pointerInput
          val decay = exponentialDecay<Float>(frictionMultiplier = 1.6f)
          val tracker = VelocityTracker1D(isDataDifferential = false)
          var dragPosition = 0f

          fun endDrag() {
            val velocity = tracker.calculateVelocity()
            val target = decay.calculateTargetValue(dragPosition, velocity).roundToInt()
            scope.launch { settleAt(target, velocity) }
          }

          detectDialDrag(
            placement = placement,
            onDragStart = {
              tracker.resetTracking()
              dragPosition = position.value
            },
            onDrag = { uptimeMillis, degrees ->
              dragPosition -= degrees / step
              tracker.addDataPoint(uptimeMillis, dragPosition)
              val snapTarget = dragPosition
              scope.launch { position.snapTo(snapTarget) }
            },
            onDragEnd = ::endDrag,
          )
        },
    content = {
      slots.fastForEach { slot ->
        key(slot.item.id) {
          // Every slot's item gets a new angle each time the wheel moves by one item. Pass only the
          // fields that are drawn, and let the lambdas capture just the index, so an icon whose
          // inputs didn't change can skip instead of all of them recomposing on every step.
          val logicalIndex = slot.logicalIndex
          DialIcon(
            id = slot.item.id,
            drawableRes = slot.item.drawableRes,
            selected = logicalIndex == centerIndex,
            colors = colors,
            step = step,
            angle = { angleOf(logicalIndex) },
            pulse = pulse,
            onClick = { scope.launch { settleAt(logicalIndex, initialVelocity = 0f) } },
          )
        }
      }
    },
  ) { measurables, constraints ->
    // Largest dial whose visible part fits the constraints.
    val widthUnits = placement.widthInHalfSides
    val heightUnits = placement.heightInHalfSides
    val halfSideForWidth =
      if (constraints.hasBoundedWidth) constraints.maxWidth / widthUnits
      else Float.POSITIVE_INFINITY
    val halfSideForHeight =
      if (constraints.hasBoundedHeight) constraints.maxHeight / heightUnits
      else Float.POSITIVE_INFINITY
    val halfSide =
      min(halfSideForWidth, halfSideForHeight).takeIf { it.isFinite() }
        ?: (DefaultDialSize.toPx() / 2f)
    val width = constraints.constrainWidth((halfSide * widthUnits).roundToInt())
    val height = constraints.constrainHeight((halfSide * heightUnits).roundToInt())
    val geometry = DialGeometry(Size(width.toFloat(), height.toFloat()), placement)
    val itemSize = geometry.itemSize.roundToInt().coerceAtLeast(0)
    val itemConstraints = Constraints.fixed(itemSize, itemSize)
    val placeables = measurables.fastMap { it.measure(itemConstraints) }

    layout(width, height) {
      // Reading the position here means rotation only re-runs placement, not measure/composition.
      // Read it once per pass rather than once per child.
      val rotation = position.value
      placeables.fastForEachIndexed { index, placeable ->
        val angle = (slots[index].logicalIndex - rotation) * step
        val radians = (placement.selectionAngle + angle).toRadians()
        val itemCenter = geometry.pointOnCircle(geometry.itemOrbitRadius, radians)
        placeable.place(
          x = (itemCenter.x - placeable.width / 2f).roundToInt(),
          y = (itemCenter.y - placeable.height / 2f).roundToInt(),
        )
      }
    }
  }
}

/** Size used when [RotaryDial] gets no size constraint from its modifier or parent. */
val DefaultDialSize = 320.dp

private const val SelectedItemScale = 1.4f

/** Peak of the selected item's pulse, on top of [SelectedItemScale]. */
private const val SelectedItemPulseScale = 1.1f

/** Duration of one half of the pulse (scale up, or scale down). */
private const val SelectedItemPulseMillis = 700

/**
 * How near an item at [angle] is to the selection pointer: 1 under it, 0 a full step or more away.
 */
private fun closenessOf(angle: Float, step: Float): Float =
  (1f - abs(angle) / step).coerceIn(0f, 1f)

@Immutable private data class DialSlot(val logicalIndex: Int, val item: RotaryDialItem)

@Composable
private fun DialIcon(
  id: String,
  @DrawableRes drawableRes: Int,
  selected: Boolean,
  colors: RotaryDialColors,
  step: Float,
  angle: () -> Float,
  pulse: () -> Float,
  onClick: () -> Unit,
) {
  val painter = painterResource(drawableRes)
  Box(
    modifier =
      Modifier.graphicsLayer {
          // One read of the wheel's position per frame; closeness is derived from it.
          val angle = angle()
          val closeness = closenessOf(angle, step)
          // Only items near the pointer read the pulse, so the others are not redrawn every frame.
          val pulseScale = if (closeness > 0f) lerp(1f, pulse(), closeness) else 1f
          val scale = lerp(1f, SelectedItemScale, closeness) * pulseScale
          scaleX = scale
          scaleY = scale
          // Tilt by the angle from the selection, so the selected item (angle 0) is upright and the
          // others lean with the wheel.
          rotationZ = angle
        }
        .clickable(
          // No indication, so no interaction source needs to be created and remembered.
          interactionSource = null,
          indication = null,
          role = Role.Button,
          onClick = onClick,
        )
        .semantics {
          contentDescription = id
          this.selected = selected
        }
        .drawWithCache {
          // The draw block runs every frame while the wheel moves, but only the icons next to the
          // pointer change color. Reuse one filter for each end of the blend instead of allocating
          // a new one per icon per frame.
          val restingFilter = ColorFilter.tint(colors.icon)
          val selectedFilter = ColorFilter.tint(colors.selectedIcon)
          val iconInset = size.minDimension * 0.12f
          onDrawBehind {
            val filter =
              when (val closeness = closenessOf(angle(), step)) {
                0f -> restingFilter
                1f -> selectedFilter
                else -> ColorFilter.tint(lerp(colors.icon, colors.selectedIcon, closeness))
              }
            inset(iconInset) { with(painter) { draw(size = size, colorFilter = filter) } }
          }
        }
  )
}

/** Disk radius as a fraction of the half side; the rest is margin for the highlight and pointer. */
private const val OuterRadiusRatio = 0.86f

/**
 * All radii of the dial, proportional to the largest dial whose visible part, as set by
 * [placement], fits in [size].
 */
private class DialGeometry(size: Size, placement: DialPlacement) {
  /** Distance from the dial's center to the outside of the pointer. */
  private val halfSide =
    min(size.width / placement.widthInHalfSides, size.height / placement.heightInHalfSides)
  /** The center sits so that exactly the visible part of the dial lies inside [size]. */
  val center =
    when (placement.edge) {
      DialEdge.Left -> Offset(size.width - halfSide, size.height / 2f)
      DialEdge.Right -> Offset(halfSide, size.height / 2f)
      DialEdge.Top -> Offset(size.width / 2f, size.height - halfSide)
      DialEdge.Bottom -> Offset(size.width / 2f, halfSide)
      DialEdge.None -> Offset(size.width / 2f, size.height / 2f)
    }
  val outerRadius = halfSide * OuterRadiusRatio
  val highlightOuterRadius = outerRadius * 1.06f
  val ringOuterRadius = outerRadius * 0.62f
  val ringInnerRadius = outerRadius * 0.48f
  val itemOrbitRadius = outerRadius * 0.81f
  /** Where the dial's rounded outer edge starts to curve away. */
  val rimStartRadius = outerRadius * 0.86f
  val itemSize = outerRadius * 0.26f
  val highlightCornerRadius = outerRadius * 0.05f
  val dividerWidth = outerRadius * 0.008f
  val pointerTipRadius = highlightOuterRadius + halfSide * 0.025f
  val pointerBaseRadius = halfSide * 0.995f
  val pointerHalfWidth = halfSide * 0.045f

  /** [radians] is measured clockwise from 12 o'clock. */
  fun pointOnCircle(radius: Float, radians: Float) =
    Offset(center.x + radius * sin(radians), center.y - radius * cos(radians))
}

/**
 * Draws the static parts of the dial; only the dividers follow [rotation] (degrees). The highlight
 * and pointer are built at 12 o'clock and turned to the [placement]'s selection angle.
 */
private fun Modifier.dialBackground(
  colors: RotaryDialColors,
  visibleCount: Int,
  placement: DialPlacement,
  rotation: () -> Float,
) = drawWithCache {
  val geometry = DialGeometry(size, placement)
  val step = if (visibleCount == 0) 360f else 360f / visibleCount
  val highlightHalfAngle = min(step / 2f, 24f).toRadians()
  val highlight = highlightPath(geometry, highlightHalfAngle)
  val pointer =
    Path().apply {
      moveTo(geometry.center.x, geometry.center.y - geometry.pointerTipRadius)
      lineTo(
        geometry.center.x - geometry.pointerHalfWidth,
        geometry.center.y - geometry.pointerBaseRadius,
      )
      lineTo(
        geometry.center.x + geometry.pointerHalfWidth,
        geometry.center.y - geometry.pointerBaseRadius,
      )
      close()
    }
  val ringWidth = geometry.ringOuterRadius - geometry.ringInnerRadius
  // Dividers are clipped to the disk so their ends never poke past its curved edge.
  val disk = Path().apply { addOval(Rect(center = geometry.center, radius = geometry.outerRadius)) }

  // Tyre-like shoulder: flat across most of the disk, catching light on a crest near the edge, then
  // darkening as the surface rolls away.
  val rimWidth = geometry.outerRadius - geometry.rimStartRadius
  val rimMidRadius = geometry.rimStartRadius + rimWidth / 2f
  val shoulder =
    Brush.radialGradient(
      0f to Color.Transparent,
      geometry.rimStartRadius / geometry.outerRadius to Color.Transparent,
      0.93f to colors.rimHighlight,
      0.97f to colors.rimShadow.copy(alpha = colors.rimShadow.alpha * 0.5f),
      1f to colors.rimShadow,
      center = geometry.center,
      radius = geometry.outerRadius,
    )
  // Fixed light from the top-left, so the rim looks tilted: lit on one side, shaded on the other.
  val lightOffset = Offset(geometry.outerRadius, geometry.outerRadius) * 0.7071f
  val tilt =
    Brush.linearGradient(
      0f to colors.rimHighlight,
      0.5f to Color.Transparent,
      1f to colors.rimShadow.copy(alpha = colors.rimShadow.alpha * 0.6f),
      start = geometry.center - lightOffset,
      end = geometry.center + lightOffset,
    )
  // Soft shadow the inner ring casts onto the disk, so the ring sits above it.
  val ringShadowWidth = geometry.outerRadius * 0.06f
  val ringShadowStart = geometry.ringOuterRadius / (geometry.ringOuterRadius + ringShadowWidth)
  val ringShadow =
    Brush.radialGradient(
      // Nothing inside the ring, so the center disk keeps its flat color.
      0f to Color.Transparent,
      ringShadowStart - 0.001f to Color.Transparent,
      ringShadowStart to colors.rimShadow,
      1f to Color.Transparent,
      center = geometry.center,
      radius = geometry.ringOuterRadius + ringShadowWidth,
    )

  onDrawBehind {
    drawCircle(color = colors.dial, radius = geometry.outerRadius, center = geometry.center)

    if (visibleCount > 1) {
      val offset = placement.selectionAngle + rotation()
      clipPath(disk) {
        repeat(visibleCount) { index ->
          // Dividers sit halfway between two items.
          val radians = (offset + (index + 0.5f) * step).toRadians()
          drawLine(
            color = colors.divider,
            start = geometry.pointOnCircle(geometry.ringOuterRadius, radians),
            end = geometry.pointOnCircle(geometry.outerRadius, radians),
            strokeWidth = geometry.dividerWidth.coerceAtLeast(1f),
            cap = StrokeCap.Butt,
          )
        }
      }
    }

    // Shading goes over the dividers so they too fade into the curved edge.
    drawCircle(brush = shoulder, radius = geometry.outerRadius, center = geometry.center)
    drawCircle(
      brush = tilt,
      radius = rimMidRadius,
      center = geometry.center,
      style = Stroke(width = rimWidth),
    )
    drawCircle(
      brush = ringShadow,
      radius = geometry.ringOuterRadius + ringShadowWidth,
      center = geometry.center,
    )

    rotate(degrees = placement.selectionAngle, pivot = geometry.center) {
      drawPath(path = highlight, color = colors.highlight)
      drawPath(path = pointer, color = colors.pointer)
    }
    drawCircle(
      color = colors.highlight,
      radius = geometry.ringInnerRadius + ringWidth / 2f,
      center = geometry.center,
      style = Stroke(width = ringWidth),
    )
  }
}

/**
 * The highlight wedge behind the selected item: an annular sector from the middle of the ring to
 * slightly past the dial's edge, with rounded outer corners.
 */
private fun highlightPath(geometry: DialGeometry, halfAngle: Float): Path {
  val inner = (geometry.ringInnerRadius + geometry.ringOuterRadius) / 2f
  val outer = geometry.highlightOuterRadius
  val corner = geometry.highlightCornerRadius
  val cornerAngle = corner / outer
  val arcRect = Rect(center = geometry.center, radius = outer)

  return Path().apply {
    geometry.pointOnCircle(inner, -halfAngle).let { moveTo(it.x, it.y) }
    geometry.pointOnCircle(outer - corner, -halfAngle).let { lineTo(it.x, it.y) }
    val leftCorner = geometry.pointOnCircle(outer, -halfAngle)
    val leftArcStart = geometry.pointOnCircle(outer, -halfAngle + cornerAngle)
    quadraticTo(leftCorner.x, leftCorner.y, leftArcStart.x, leftArcStart.y)
    arcTo(
      rect = arcRect,
      // Path angles are measured clockwise from 3 o'clock, ours from 12 o'clock.
      startAngleDegrees = (-halfAngle + cornerAngle).toDegrees() - 90f,
      sweepAngleDegrees = (2 * (halfAngle - cornerAngle)).toDegrees(),
      forceMoveTo = false,
    )
    val rightCorner = geometry.pointOnCircle(outer, halfAngle)
    val rightEdge = geometry.pointOnCircle(outer - corner, halfAngle)
    quadraticTo(rightCorner.x, rightCorner.y, rightEdge.x, rightEdge.y)
    geometry.pointOnCircle(inner, halfAngle).let { lineTo(it.x, it.y) }
    close()
  }
}

/**
 * Reports drags as rotation around the center of the layout: [onDrag] receives the pointer's uptime
 * and the angle, in degrees (clockwise positive), that the finger swept since the last event.
 *
 * A gesture is only picked up when the finger first touches down on the outer ring, the band
 * between the inner ring and the dial's edge that holds the items. Gestures that start on the inner
 * ring, the center, or outside the dial are ignored for their whole duration, even if the finger
 * later moves onto the outer ring.
 */
private suspend fun PointerInputScope.detectDialDrag(
  placement: DialPlacement,
  onDragStart: () -> Unit,
  onDrag: (uptimeMillis: Long, degrees: Float) -> Unit,
  onDragEnd: () -> Unit,
) {
  awaitEachGesture {
    // Children (the clickable icons) may consume the down, so look at it regardless.
    val down = awaitFirstDown(requireUnconsumed = false)
    val geometry = DialGeometry(size.toSize(), placement)
    val downRadius = (down.position - geometry.center).getDistance()
    if (downRadius < geometry.ringOuterRadius || downRadius > geometry.outerRadius) {
      return@awaitEachGesture
    }

    val slopChange =
      awaitTouchSlopOrCancellation(down.id) { change, _ -> change.consume() }
        ?: return@awaitEachGesture

    onDragStart()
    // Count the movement spent getting past the touch slop, so the wheel doesn't lag the finger.
    reportRotation(
      geometry.center,
      down.position,
      slopChange.position,
      slopChange.uptimeMillis,
      onDrag,
    )
    drag(slopChange.id) { change ->
      change.consume()
      reportRotation(
        geometry.center,
        change.previousPosition,
        change.position,
        change.uptimeMillis,
        onDrag,
      )
    }
    // Lifted or cancelled: either way, let the wheel settle on an item.
    onDragEnd()
  }
}

private fun PointerInputScope.reportRotation(
  center: Offset,
  from: Offset,
  to: Offset,
  uptimeMillis: Long,
  onDrag: (uptimeMillis: Long, degrees: Float) -> Unit,
) {
  val previous = from - center
  val current = to - center
  // Angles are unstable right at the center, so ignore movement there if the finger wanders in.
  val deadZone = min(size.width, size.height) * 0.05f
  if (previous.getDistance() > deadZone && current.getDistance() > deadZone) {
    val delta = current.clockwiseDegreesFromTop() - previous.clockwiseDegreesFromTop()
    onDrag(uptimeMillis, ((delta + 540f) % 360f) - 180f)
  }
}

private fun Offset.clockwiseDegreesFromTop(): Float = atan2(x, -y).toDegrees()

private fun Float.toRadians(): Float = this * (PI.toFloat() / 180f)

private fun Float.toDegrees(): Float = this * (180f / PI.toFloat())

// region Previews

internal val PreviewDialItems: ImmutableList<RotaryDialItem> =
  listOf(
      "lock" to R.drawable.ic_dial_lock,
      "face" to R.drawable.ic_dial_face,
      "alert" to R.drawable.ic_dial_gpp_maybe,
      "shield" to R.drawable.ic_dial_shield,
      "wifi" to R.drawable.ic_dial_wifi,
      "bluetooth" to R.drawable.ic_dial_bluetooth,
      "battery" to R.drawable.ic_dial_battery_full,
      "settings" to R.drawable.ic_dial_settings,
      "home" to R.drawable.ic_dial_home,
      "favorite" to R.drawable.ic_dial_favorite,
      "music" to R.drawable.ic_dial_music_note,
      "camera" to R.drawable.ic_dial_camera_alt,
      "notifications" to R.drawable.ic_dial_notifications,
      "place" to R.drawable.ic_dial_place,
      "speed" to R.drawable.ic_dial_speed,
    )
    .map { (id, drawable) -> RotaryDialItem(id = id, drawableRes = drawable) }
    .toImmutableList()

private val PreviewBackground = Color(0xFF4A7D6C)

/** The selected item and a dropdown menu listing every [DialPosition]. */
@Composable
private fun DialControls(
  selected: RotaryDialItem,
  position: DialPosition,
  onPositionChange: (DialPosition) -> Unit,
  modifier: Modifier = Modifier,
) {
  var expanded by remember { mutableStateOf(false) }
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(16.dp),
    color = Color.Black.copy(alpha = 0.25f),
    contentColor = Color.White,
  ) {
    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
      Text(text = "Selected: ${selected.id}", style = typography.titleMedium)
      // The menu anchors to this Box, so it opens right under the button.
      Box {
        TextButton(
          onClick = { expanded = true },
          contentPadding = PaddingValues(horizontal = 0.dp),
          colors = ButtonDefaults.textButtonColors(contentColor = Color.White),
        ) {
          Text(text = position.name, style = typography.labelLarge)
          Icon(
            imageVector = Icons.Filled.ArrowDropDown,
            contentDescription = "Change dial position",
          )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
          DialPosition.entries.forEach { option ->
            DropdownMenuItem(
              text = { Text(option.name) },
              onClick = {
                onPositionChange(option)
                expanded = false
              },
              leadingIcon = {
                if (option == position) {
                  Icon(imageVector = Icons.Filled.Check, contentDescription = "Current position")
                }
              },
            )
          }
        }
      }
    }
  }
}

/** Lets the dial take all the space the parent gives it along the edge it sits on. */
private fun Modifier.dialFrame(position: DialPosition): Modifier =
  when (position) {
    DialPosition.START_CENTER,
    DialPosition.START_CENTER_MEDIUM,
    DialPosition.START_CENTER_SMALL,
    DialPosition.END_CENTER,
    DialPosition.END_CENTER_MEDIUM,
    DialPosition.END_CENTER_SMALL -> fillMaxHeight()
    DialPosition.TOP_CENTER,
    DialPosition.TOP_CENTER_MEDIUM,
    DialPosition.TOP_CENTER_SMALL,
    DialPosition.BOTTOM_CENTER,
    DialPosition.BOTTOM_CENTER_MEDIUM,
    DialPosition.BOTTOM_CENTER_SMALL -> fillMaxWidth()
    DialPosition.CENTER -> fillMaxSize()
  }

/** A corner of the pane that the dial at this position leaves empty. */
private val DialPosition.controlsAlignment: Alignment
  get() =
    when (this) {
      DialPosition.START_CENTER,
      DialPosition.START_CENTER_MEDIUM,
      DialPosition.START_CENTER_SMALL -> Alignment.TopEnd
      DialPosition.END_CENTER,
      DialPosition.END_CENTER_MEDIUM,
      DialPosition.END_CENTER_SMALL -> Alignment.TopStart
      DialPosition.TOP_CENTER,
      DialPosition.TOP_CENTER_MEDIUM,
      DialPosition.TOP_CENTER_SMALL -> Alignment.BottomStart
      DialPosition.BOTTOM_CENTER,
      DialPosition.BOTTOM_CENTER_MEDIUM,
      DialPosition.BOTTOM_CENTER_SMALL,
      DialPosition.CENTER -> Alignment.TopStart
    }

private class DialPositionProvider : PreviewParameterProvider<DialPosition> {
  override val values = DialPosition.entries.asSequence()
}

/** One preview per [DialPosition], each dial aligned to its edge of a phone-sized frame. */
@Preview(widthDp = 360, heightDp = 440)
@Composable
private fun RotaryDialPositionPreview(
  @PreviewParameter(DialPositionProvider::class) position: DialPosition
) {
  FunposablesTheme {
    Box(Modifier.fillMaxSize().background(PreviewBackground)) {
      RotaryDial(
        items = PreviewDialItems,
        modifier = Modifier.align(position.alignment).dialFrame(position),
        dialPosition = position,
      )
    }
  }
}

@Preview(name = "Full dial, 15 items", widthDp = 360, heightDp = 360)
@Composable
private fun RotaryDialPreview() {
  FunposablesTheme {
    Box(Modifier.fillMaxSize().background(PreviewBackground), contentAlignment = Alignment.Center) {
      RotaryDial(items = PreviewDialItems, modifier = Modifier.fillMaxSize())
    }
  }
}

/** The same dial at three sizes: every part scales with the size given by the modifier. */
@Preview(name = "Scaling", widthDp = 560, heightDp = 260)
@Composable
private fun RotaryDialScalingPreview() {
  FunposablesTheme {
    Row(
      modifier = Modifier.fillMaxSize().background(PreviewBackground).padding(8.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      RotaryDial(items = PreviewDialItems, modifier = Modifier.size(100.dp))
      RotaryDial(
        items = PreviewDialItems,
        modifier = Modifier.size(160.dp),
        initialSelectedIndex = 3,
      )
      RotaryDial(
        items = PreviewDialItems,
        modifier = Modifier.size(240.dp),
        initialSelectedIndex = 7,
      )
    }
  }
}

@Preview(name = "Fewer items than slots", widthDp = 300, heightDp = 300)
@Composable
private fun RotaryDialFewItemsPreview() {
  FunposablesTheme {
    Box(Modifier.fillMaxSize().background(PreviewBackground), contentAlignment = Alignment.Center) {
      RotaryDial(
        items = PreviewDialItems.take(5).toImmutableList(),
        modifier = Modifier.fillMaxSize(),
      )
    }
  }
}

@Preview(name = "Empty", widthDp = 200, heightDp = 200)
@Composable
private fun RotaryDialEmptyPreview() {
  FunposablesTheme {
    Box(Modifier.fillMaxSize().background(PreviewBackground)) {
      RotaryDial(items = persistentListOf(), modifier = Modifier.fillMaxSize())
    }
  }
}

/** Run in interactive mode to drag, fling, tap and pick a position from the menu. */
@Preview(widthDp = 400, heightDp = 720)
@Composable
private fun RotaryDialPanePreview(
  @PreviewParameter(DialPositionProvider::class) position: DialPosition
) {
  FunposablesTheme { RotaryDialPane(dialPosition = position) }
}

// endregion
