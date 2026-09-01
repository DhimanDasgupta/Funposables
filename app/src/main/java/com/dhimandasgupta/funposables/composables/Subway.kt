package com.dhimandasgupta.funposables.composables

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.dhimandasgupta.funposables.ui.theme.FunposablesTheme
import kotlin.math.max

enum class SubwayOrientation {
  Horizontal,
  Vertical,
}

enum class SubwayStepState {
  Completed,
  Active,
  Inactive,
}

@Immutable
data class SubwayStep(
  val label: String,
  val supportText: String? = null,
  val trailingLabel: String? = null,
  val state: SubwayStepState = SubwayStepState.Inactive,
)

@Immutable
data class SubwayColors(
  val activeColor: Color,
  val activeDotInnerColor: Color,
  val inactiveColor: Color,
  val activeLabelColor: Color,
  val inactiveLabelColor: Color,
  val supportTextColor: Color,
  val trailingLabelColor: Color,
)

object SubwayDefaults {
  val Indigo400 = Color(0xFF818CF8)
  val InactiveGray = Color(0xFFE5E7EB)

  @Composable
  fun colors(
    activeColor: Color = Indigo400,
    activeDotInnerColor: Color = Color.White,
    inactiveColor: Color = InactiveGray,
    activeLabelColor: Color = MaterialTheme.colorScheme.onSurface,
    inactiveLabelColor: Color = MaterialTheme.colorScheme.onSurface,
    supportTextColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    trailingLabelColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
  ): SubwayColors =
    SubwayColors(
      activeColor = activeColor,
      activeDotInnerColor = activeDotInnerColor,
      inactiveColor = inactiveColor,
      activeLabelColor = activeLabelColor,
      inactiveLabelColor = inactiveLabelColor,
      supportTextColor = supportTextColor,
      trailingLabelColor = trailingLabelColor,
    )
}

/**
 * Subway component supporting Horizontal and Vertical orientations.
 *
 * Requirements:
 * - 2 to 7 steps allowed. Less than 2 or greater than 7 throws an IllegalArgumentException
 *   preventing rendering.
 * - Horizontal: dot and texts (label, supportText) are center-aligned, all steps have equal width
 *   (fill width x hug height).
 * - Vertical: dot and label text are top-aligned, each step fills width, and all steps share the
 *   height of the tallest step when hugging content.
 */
@Composable
fun Subway(
  steps: List<SubwayStep>,
  modifier: Modifier = Modifier,
  orientation: SubwayOrientation = SubwayOrientation.Horizontal,
  colors: SubwayColors = SubwayDefaults.colors(),
) {
  require(steps.size in 2..7) {
    "Subway step count must be between 2 and 7, but was ${steps.size}."
  }

  when (orientation) {
    SubwayOrientation.Horizontal -> {
      HorizontalSubway(
        steps = steps,
        modifier = modifier,
        colors = colors,
      )
    }
    SubwayOrientation.Vertical -> {
      VerticalSubway(
        steps = steps,
        modifier = modifier,
        colors = colors,
      )
    }
  }
}

@Composable
private fun SubwayDot(
  state: SubwayStepState,
  colors: SubwayColors,
  modifier: Modifier = Modifier,
) {
  when (state) {
    SubwayStepState.Completed -> {
      Box(modifier = modifier.size(14.dp).clip(CircleShape).background(colors.activeColor))
    }
    SubwayStepState.Active -> {
      Box(
        modifier = modifier.size(14.dp).clip(CircleShape).background(colors.activeColor),
        contentAlignment = Alignment.Center,
      ) {
        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(colors.activeDotInnerColor))
      }
    }
    SubwayStepState.Inactive -> {
      Box(modifier = modifier.size(12.dp).clip(CircleShape).background(colors.inactiveColor))
    }
  }
}

@Composable
private fun HorizontalSubway(
  steps: List<SubwayStep>,
  modifier: Modifier = Modifier,
  colors: SubwayColors,
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    verticalAlignment = Alignment.Top,
  ) {
    steps.forEachIndexed { index, step ->
      val hasLeftLine = index > 0
      val hasRightLine = index < steps.lastIndex

      val leftLineColor =
        if (hasLeftLine && steps[index - 1].state == SubwayStepState.Completed) {
          colors.activeColor
        } else {
          colors.inactiveColor
        }

      val rightLineColor =
        if (hasRightLine && step.state == SubwayStepState.Completed) {
          colors.activeColor
        } else {
          colors.inactiveColor
        }

      Column(
        modifier = Modifier.weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        Box(
          modifier = Modifier.fillMaxWidth().height(14.dp),
          contentAlignment = Alignment.Center,
        ) {
          if (hasLeftLine) {
            Box(
              modifier =
                Modifier.fillMaxWidth(0.5f)
                  .height(4.dp)
                  .align(Alignment.CenterStart)
                  .background(leftLineColor)
            )
          }

          if (hasRightLine) {
            Box(
              modifier =
                Modifier.fillMaxWidth(0.5f)
                  .height(4.dp)
                  .align(Alignment.CenterEnd)
                  .background(rightLineColor)
            )
          }

          SubwayDot(
            state = step.state,
            colors = colors,
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
          text = step.label,
          textAlign = TextAlign.Center,
          fontWeight =
            if (step.state == SubwayStepState.Active) {
              FontWeight.Bold
            } else {
              FontWeight.Normal
            },
          color =
            if (step.state == SubwayStepState.Active) {
              colors.activeLabelColor
            } else {
              colors.inactiveLabelColor
            },
          style = MaterialTheme.typography.bodyMedium,
          modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
        )

        if (!step.supportText.isNullOrEmpty()) {
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = step.supportText,
            textAlign = TextAlign.Center,
            color = colors.supportTextColor,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
          )
        }
      }
    }
  }
}

@Composable
private fun VerticalSubway(
  steps: List<SubwayStep>,
  modifier: Modifier = Modifier,
  colors: SubwayColors,
) {
  val minStepHeightDp = 54.dp

  Layout(
    content = {
      steps.forEachIndexed { index, step ->
        VerticalSubwayStepItem(
          index = index,
          step = step,
          steps = steps,
          colors = colors,
        )
      }
    },
    modifier = modifier.fillMaxWidth(),
  ) { measurables, constraints ->
    val minHeightPx = minStepHeightDp.roundToPx()
    val maxIntrinsicHeight =
      measurables.maxOfOrNull { it.maxIntrinsicHeight(constraints.maxWidth) } ?: minHeightPx
    val uniformStepHeight = max(minHeightPx, maxIntrinsicHeight)

    // Measure each step with exact uniform height
    val placeables = measurables.map { measurable ->
      measurable.measure(
        Constraints.fixed(
          width = constraints.maxWidth,
          height = uniformStepHeight,
        )
      )
    }

    val totalHeight = uniformStepHeight * steps.size

    layout(constraints.maxWidth, totalHeight) {
      placeables.forEachIndexed { index, placeable ->
        placeable.placeRelative(
          x = 0,
          y = index * uniformStepHeight,
        )
      }
    }
  }
}

@Composable
private fun VerticalSubwayStepItem(
  index: Int,
  step: SubwayStep,
  steps: List<SubwayStep>,
  colors: SubwayColors,
  modifier: Modifier = Modifier,
) {
  val isFirst = index == 0
  val isLast = index == steps.lastIndex
  val isInactive = step.state == SubwayStepState.Inactive

  // Top line: 4x20px for active/completed, 4x22px for inactive
  val topLineHeight = if (isInactive) 22.dp else 20.dp
  val topLineColor =
    if (!isFirst && steps[index - 1].state == SubwayStepState.Completed) {
      colors.activeColor
    } else {
      colors.inactiveColor
    }

  val bottomLineColor =
    if (!isLast && step.state == SubwayStepState.Completed) {
      colors.activeColor
    } else {
      colors.inactiveColor
    }

  Row(
    modifier = modifier.fillMaxWidth(),
    verticalAlignment = Alignment.Top,
  ) {
    // Indicator column: Top line, Dot, Bottom line
    Column(
      modifier = Modifier.width(24.dp).fillMaxHeight(),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      if (isFirst) {
        Spacer(modifier = Modifier.height(topLineHeight))
      } else {
        Box(modifier = Modifier.width(4.dp).height(topLineHeight).background(topLineColor))
      }

      SubwayDot(
        state = step.state,
        colors = colors,
      )

      if (isLast) {
        Spacer(modifier = Modifier.weight(1f))
      } else {
        Box(modifier = Modifier.width(4.dp).weight(1f).background(bottomLineColor))
      }
    }

    Spacer(modifier = Modifier.width(12.dp))

    // Label and support text column - top aligned with the dot at 20.dp
    Column(modifier = Modifier.weight(1f).padding(top = 20.dp, bottom = 8.dp)) {
      Text(
        text = step.label,
        fontWeight =
          if (step.state == SubwayStepState.Active) {
            FontWeight.Bold
          } else {
            FontWeight.Normal
          },
        color =
          if (step.state == SubwayStepState.Active) {
            colors.activeLabelColor
          } else {
            colors.inactiveLabelColor
          },
        style = MaterialTheme.typography.bodyMedium,
      )

      if (!step.supportText.isNullOrEmpty()) {
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = step.supportText,
          color = colors.supportTextColor,
          style = MaterialTheme.typography.bodySmall,
        )
      }
    }

    // Trailing label if present
    if (!step.trailingLabel.isNullOrEmpty()) {
      Text(
        text = step.trailingLabel,
        color = colors.trailingLabelColor,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(top = 20.dp, start = 8.dp, end = 16.dp),
      )
    }
  }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview(showBackground = true)
@Composable
private fun HorizontalSubwayPreview() {
  FunposablesTheme {
    val steps =
      listOf(
        SubwayStep(
          label = "Label",
          supportText = "Support text",
          state = SubwayStepState.Completed,
        ),
        SubwayStep(label = "Label", supportText = "Support text", state = SubwayStepState.Active),
        SubwayStep(label = "Label", supportText = "Support text", state = SubwayStepState.Inactive),
        SubwayStep(label = "Label", supportText = "Support text", state = SubwayStepState.Inactive),
      )
    Subway(
      steps = steps,
      orientation = SubwayOrientation.Horizontal,
      modifier = Modifier.padding(16.dp),
    )
  }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview(showBackground = true)
@Composable
private fun VerticalSubwayPreview() {
  FunposablesTheme {
    val steps =
      listOf(
        SubwayStep(
          label = "Long label that wraps\nto a second line",
          supportText = "Support text",
          trailingLabel = "Trailing label",
          state = SubwayStepState.Completed,
        ),
        SubwayStep(
          label = "Label",
          supportText = "Support text",
          trailingLabel = "Trailing label",
          state = SubwayStepState.Active,
        ),
        SubwayStep(
          label = "Label",
          supportText = "Support text",
          trailingLabel = "Trailing label",
          state = SubwayStepState.Inactive,
        ),
        SubwayStep(
          label = "Label",
          supportText = "Support text",
          trailingLabel = "Trailing label",
          state = SubwayStepState.Inactive,
        ),
      )
    Subway(
      steps = steps,
      orientation = SubwayOrientation.Vertical,
      modifier = Modifier.padding(16.dp),
    )
  }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun SubwayPane(modifier: Modifier = Modifier) {
  var orientation by remember { mutableStateOf(SubwayOrientation.Horizontal) }
  var stepCount by remember { mutableIntStateOf(4) }
  var activeStepIndex by remember { mutableIntStateOf(1) }
  var hasMultilineLabel by remember { mutableStateOf(true) }

  val steps =
    remember(stepCount, activeStepIndex, hasMultilineLabel) {
      (0 until stepCount).map { i ->
        val label =
          if (i == 0 && hasMultilineLabel) {
            "Long label that wraps\nto a second line"
          } else {
            "Label"
          }
        val state =
          when {
            i < activeStepIndex -> SubwayStepState.Completed
            i == activeStepIndex -> SubwayStepState.Active
            else -> SubwayStepState.Inactive
          }
        SubwayStep(
          label = label,
          supportText = "Support text",
          trailingLabel = "Trailing label",
          state = state,
        )
      }
    }

  val scrollState = rememberScrollState()

  Column(
    modifier =
      modifier
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
        .fillMaxSize()
        .verticalScroll(scrollState)
        .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    Text(
      text = "Subway Component",
      style = MaterialTheme.typography.headlineMedium,
      fontWeight = FontWeight.Bold,
    )

    // Controls Card
    Card(modifier = Modifier.fillMaxWidth()) {
      Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Text(
          "Controls",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.SemiBold,
        )

        // Orientation
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text("Orientation:")
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
              onClick = { orientation = SubwayOrientation.Horizontal },
              colors =
                if (orientation == SubwayOrientation.Horizontal) {
                  ButtonDefaults.buttonColors()
                } else {
                  ButtonDefaults.outlinedButtonColors()
                },
            ) {
              Text("Horizontal")
            }
            Button(
              onClick = { orientation = SubwayOrientation.Vertical },
              colors =
                if (orientation == SubwayOrientation.Vertical) {
                  ButtonDefaults.buttonColors()
                } else {
                  ButtonDefaults.outlinedButtonColors()
                },
            ) {
              Text("Vertical")
            }
          }
        }

        // Step count
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text("Step Count (2-7): $stepCount")
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
              onClick = { if (stepCount > 2) stepCount-- },
              enabled = stepCount > 2,
            ) {
              Text("-")
            }
            Button(
              onClick = { if (stepCount < 7) stepCount++ },
              enabled = stepCount < 7,
            ) {
              Text("+")
            }
          }
        }

        // Active Step
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text("Active Step: ${activeStepIndex + 1}")
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
              onClick = { if (activeStepIndex > 0) activeStepIndex-- },
              enabled = activeStepIndex > 0,
            ) {
              Text("Prev")
            }
            Button(
              onClick = { if (activeStepIndex < stepCount - 1) activeStepIndex++ },
              enabled = activeStepIndex < stepCount - 1,
            ) {
              Text("Next")
            }
          }
        }

        // Multiline first step toggle
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text("Multiline Label (Step 1)")
          Button(onClick = { hasMultilineLabel = !hasMultilineLabel }) {
            Text(if (hasMultilineLabel) "Enabled" else "Disabled")
          }
        }
      }
    }

    // Subway Display Card
    Card(modifier = Modifier.fillMaxWidth()) {
      Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Subway(
          steps = steps,
          orientation = orientation,
          modifier = Modifier.fillMaxWidth(),
        )
      }
    }
  }
}
