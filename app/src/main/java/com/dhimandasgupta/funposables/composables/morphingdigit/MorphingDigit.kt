package com.dhimandasgupta.funposables.composables.morphingdigit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.PathNode
import androidx.compose.ui.graphics.vector.toPath
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun MorphingDigit(
  digit: Digit,
  modifier: Modifier = Modifier,
  durationMillis: Long = 500,
  color: Color = colorScheme.onSurface,
  style: DrawStyle = Stroke(5f),
) {
  val pz = updateTransitionData(digit = digit, durationMillis.toInt())

  Spacer(
    modifier =
      modifier.height(108.dp).width(76.dp).drawWithCache {
        val path =
          listOf(
              PathNode.MoveTo(pz.p0, pz.p1),
              PathNode.CurveTo(pz.p2, pz.p3, pz.p4, pz.p5, pz.p6, pz.p7),
              PathNode.CurveTo(pz.p8, pz.p9, pz.p10, pz.p11, pz.p12, pz.p13),
              PathNode.CurveTo(pz.p14, pz.p15, pz.p16, pz.p17, pz.p18, pz.p19),
              PathNode.CurveTo(pz.p20, pz.p21, pz.p22, pz.p23, pz.p24, pz.p25),
            )
            .toPath()
        onDrawBehind {
          drawPath(path, color = color, style = style)
        }
      }
  )
}

@Preview
@Composable
private fun MorphingDigitPreview() {
  Row(
    modifier = Modifier,
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.Center,
  ) {
    MorphingDigit(digit = Digit.ZERO)
  }
}
