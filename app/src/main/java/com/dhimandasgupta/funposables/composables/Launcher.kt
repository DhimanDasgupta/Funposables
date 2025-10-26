package com.dhimandasgupta.funposables.composables

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.dhimandasgupta.funposables.ui.theme.FunposablesTheme

@Composable
fun Launcher(
    modifier: Modifier = Modifier,
    windowSizeClass: WindowSizeClass,
    navigateToExpandableCollapsableItems: () -> Unit
) {
    Column(
        modifier = modifier
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        colorScheme.primaryContainer,
                        colorScheme.secondaryContainer
                    )
                )
            )
            .padding(
                start = WindowInsets
                    .displayCutout.union(insets = WindowInsets.navigationBars)
                    .asPaddingValues()
                    .calculateStartPadding(LayoutDirection.Ltr),
                top = WindowInsets
                    .displayCutout.union(insets = WindowInsets.statusBars)
                    .asPaddingValues()
                    .calculateTopPadding(),
                end = WindowInsets
                    .displayCutout.union(insets = WindowInsets.navigationBars)
                    .asPaddingValues()
                    .calculateEndPadding(LayoutDirection.Ltr),
                bottom = WindowInsets
                    .displayCutout.union(insets = WindowInsets.navigationBars)
                    .asPaddingValues()
                    .calculateBottomPadding()
            )
            .fillMaxSize()
            .verticalScroll(
                state = rememberScrollState()
            )
    ) {
        OutlinedCard(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .height(56.dp),
            onClick = navigateToExpandableCollapsableItems
        ) {
            Text(
                "Expand Collapsable Items",
                modifier = Modifier.padding(16.dp),
                color = colorScheme.error
            )
        }

        var text by remember { mutableStateOf("Hello\nWorld\nHi\nThere") }

        TextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
        )

        TextAndCheckAlignment(
            text = text
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

        var numberOfLines by remember { mutableIntStateOf(1) }
        var textHeight by remember { mutableIntStateOf(0) }
        var checkBoxSize by remember { mutableStateOf(IntSize.Zero) }

        val density = LocalDensity.current

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
                modifier = Modifier.size(
                    with(density) {
                        (textHeight/numberOfLines).toDp()
                    }
                ).onGloballyPositioned { layoutCoordinates ->
                    checkBoxSize = layoutCoordinates.size
                }
            )

            Text(
                text = text,
                onTextLayout = { textLayoutResult ->
                    numberOfLines = textLayoutResult.lineCount
                    textHeight = textLayoutResult.size.height
                },
                modifier = Modifier
            )

            Spacer(modifier = Modifier.width(24.dp))

            Text(
                text = "Size: ${checkBoxSize.width} x ${checkBoxSize.height}",
            )
        }

        val lineColor = colorScheme.error

        Canvas(modifier = Modifier.matchParentSize()) {
            for (i in 0 until numberOfLines + 1) {
                val y = i * textHeight/ numberOfLines
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

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview
@Composable
private fun LauncherPreview() {
    FunposablesTheme {
        Launcher(
            navigateToExpandableCollapsableItems = {},
            windowSizeClass = WindowSizeClass.calculateFromSize(
                size = DpSize(
                    width = 360.dp,
                    height = 780.dp
                )
            )
        )
    }
}