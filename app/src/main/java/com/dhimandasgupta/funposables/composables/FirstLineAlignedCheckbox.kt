package com.dhimandasgupta.funposables.composables

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.dhimandasgupta.funposables.ui.theme.FunposablesTheme

@Composable
fun FirstLineAlignedCheckbox(
    modifier: Modifier = Modifier,
    windowSizeClass: WindowSizeClass
) {
    var text by remember { mutableStateOf("Hello\nWorld\nHi\nThere") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        colorScheme.primaryContainer,
                        colorScheme.secondaryContainer
                    )
                )
            )
            .padding(
                start = WindowInsets.displayCutout.union(WindowInsets.navigationBars)
                    .asPaddingValues()
                    .calculateStartPadding(
                        LayoutDirection.Ltr
                    ),
                top = WindowInsets.displayCutout.union(WindowInsets.statusBars).asPaddingValues()
                    .calculateTopPadding(),
                end = WindowInsets.displayCutout.union(WindowInsets.navigationBars)
                    .asPaddingValues()
                    .calculateEndPadding(
                        LayoutDirection.Ltr
                    ),
                bottom = WindowInsets.displayCutout.union(WindowInsets.navigationBars)
                    .asPaddingValues()
                    .calculateBottomPadding()
            ),
        verticalArrangement = Arrangement.spacedBy(space = 16.dp)
    ) {
        TextField(
            value = text,
            onValueChange = { text = it },
            modifier = modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
        )

        TextAndCheckAlignment(
            text = text,
            modifier = Modifier
        )
    }

}

@Composable
private fun TextAndCheckAlignment(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
    ) {
        var checked by remember { mutableStateOf(false) }
        var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
        var checkBoxSize by remember { mutableStateOf(IntSize.Zero) }

        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .clickable(
                    onClick = { checked = !checked }
                )
        ) {
            Checkbox(
                checked = checked,
                onCheckedChange = null,
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .alignBy {
                        textLayoutResult?.let { nonNullTextLayoutResult ->
                            checkBoxSize = IntSize(it.measuredWidth, it.measuredHeight)
                            val checkBoxHeight = it.measuredHeight
                            val lineHeight =
                                nonNullTextLayoutResult.size.height / nonNullTextLayoutResult.lineCount
                            println("Text Height: ${nonNullTextLayoutResult.size.height}")
                            println("Line Height: $lineHeight")
                            println("Check Box Height: $checkBoxHeight")
                            if (lineHeight >= checkBoxHeight) {
                                checkBoxHeight / 2 - lineHeight / 2
                            } else {
                                0
                            }
                        } ?: 0
                    }
            )

            Text(
                text = text,
                style = typography.displaySmall,
                onTextLayout = { textLayoutResultComputed ->
                    textLayoutResult = textLayoutResultComputed
                },
                modifier = Modifier
            )

            Spacer(modifier = Modifier.width(24.dp))

            Text(
                text = "Size: ${checkBoxSize.width} x ${checkBoxSize.height}",
            )
        }

        textLayoutResult?.let { nonNullTextLayoutResult ->
            val lineColor = colorScheme.error
            val baseLineColor = colorScheme.onBackground

            Canvas(modifier = Modifier.matchParentSize()) {
                drawLine(
                    color = baseLineColor,
                    start = Offset(x = 0f, y = nonNullTextLayoutResult.firstBaseline),
                    end = Offset(x = size.width, y = nonNullTextLayoutResult.firstBaseline),
                    strokeWidth = 0.5.dp.toPx()
                )

                drawLine(
                    color = baseLineColor,
                    start = Offset(x = 0f, y = nonNullTextLayoutResult.lastBaseline),
                    end = Offset(x = size.width, y = nonNullTextLayoutResult.lastBaseline),
                    strokeWidth = 0.5.dp.toPx()
                )

                drawLine(
                    color = Color.Green,
                    start = Offset(
                        x = 0f,
                        y = (nonNullTextLayoutResult.size.height / nonNullTextLayoutResult.lineCount / 2).toFloat()
                    ),
                    end = Offset(
                        x = size.width,
                        y = (nonNullTextLayoutResult.size.height / nonNullTextLayoutResult.lineCount / 2).toFloat()
                    ),
                    strokeWidth = 0.5.dp.toPx()
                )

                for (i in 0 until nonNullTextLayoutResult.lineCount + 1) {
                    val y =
                        i * nonNullTextLayoutResult.size.height / nonNullTextLayoutResult.lineCount
                    drawLine(
                        color = lineColor,
                        start = Offset(x = 0f, y = y.toFloat()),
                        end = Offset(x = size.width, y = y.toFloat()),
                        strokeWidth = 0.5.dp.toPx()
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview
@Composable
private fun FirstLineAlignedCheckboxPreview() {
    FunposablesTheme {
        FirstLineAlignedCheckbox(
            windowSizeClass = WindowSizeClass.calculateFromSize(
                size = DpSize(
                    width = 360.dp,
                    height = 780.dp
                )
            )
        )
    }
}