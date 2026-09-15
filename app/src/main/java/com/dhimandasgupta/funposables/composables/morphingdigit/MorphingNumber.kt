package com.dhimandasgupta.funposables.composables.morphingdigit

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.math.absoluteValue
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MorphingNumber(
  number: Long,
  durationMillis: Long = 500,
  color: Color = colorScheme.onSurface,
  style: DrawStyle = Stroke(5f),
) {
  // Keyed on the number so the digit list is not rebuilt (twice, counting the
  // reversal) on every recomposition. asReversed() is a view, not a copy.
  val digits =
    remember(number) {
      number.absoluteValue
        .toString(10)
        .map { c ->
          when (c) {
            '1' -> Digit.ONE
            '2' -> Digit.TWO
            '3' -> Digit.THREE
            '4' -> Digit.FOUR
            '5' -> Digit.FIVE
            '6' -> Digit.SIX
            '7' -> Digit.SEVEN
            '8' -> Digit.EIGHT
            '9' -> Digit.NINE
            else -> Digit.ZERO
          }
        }
        .asReversed()
    }

  Row(verticalAlignment = Alignment.CenterVertically) {
    AnimatedVisibility(
      visible = number < 0,
      enter = slideInHorizontally { it } + scaleIn(),
      exit = slideOutHorizontally { it } + scaleOut(),
    ) {
      Text(
        text = AnnotatedString("—"),
        style = typography.displayMedium,
      )
    }

    LazyRow(reverseLayout = true) {
      itemsIndexed(items = digits) { _, item ->
        MorphingDigit(
          digit = item,
          modifier = Modifier.animateItem(),
          durationMillis = durationMillis,
          color = color,
          style = style,
        )
      }
    }
  }
}

@Preview
@Composable
private fun MorphingNumberPreview() {
  var count by remember { mutableLongStateOf(1000) }
  LaunchedEffect(true) {
    (0 until 10).forEach { _ ->
      count += 1
      delay(1000.milliseconds)
    }
  }

  Surface(
    modifier = Modifier.width(300.dp).height(110.dp),
    onClick = { count += 1 },
  ) {
    MorphingNumber(number = count)
  }
}
