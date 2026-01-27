package com.dhimandasgupta.funposables.composables

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.dhimandasgupta.funposables.ui.theme.FunposablesTheme

@Composable
fun Launcher(
    modifier: Modifier = Modifier,
    navigateToExpandableCollapsableItems: () -> Unit,
    navigateToFirstLineAlignedCheckBox: () -> Unit,
    navigateToDragOrTransformBox: () -> Unit,
    navigateToKenBurnsEffect: () -> Unit,
    navigateToSearchExpander: () -> Unit,
    navigateToCurvedScreen: () -> Unit,
    navigateToCounter: () -> Unit,
    navigateToToJulia: () -> Unit,
    navigateToMandelbrot: () -> Unit
) {
    Column(
        modifier = modifier
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
            ),
        verticalArrangement = Arrangement.spacedBy(space = 8.dp)
    ) {
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(height = 8.dp)
        )

        OutlinedCard(
            modifier = Modifier
                .padding(horizontal = 16.dp)
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

        OutlinedCard(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .height(56.dp),
            onClick = navigateToFirstLineAlignedCheckBox
        ) {
            Text(
                "Checkbox aligned to First line of the text",
                modifier = Modifier.padding(16.dp),
                color = colorScheme.error
            )
        }

        OutlinedCard(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .height(56.dp),
            onClick = navigateToDragOrTransformBox
        ) {
            Text(
                "Transformable Box",
                modifier = Modifier.padding(16.dp),
                color = colorScheme.error
            )
        }

        OutlinedCard(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .height(56.dp),
            onClick = navigateToKenBurnsEffect
        ) {
            Text(
                "Kenburns Effect",
                modifier = Modifier.padding(16.dp),
                color = colorScheme.error
            )
        }

        OutlinedCard(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .height(56.dp),
            onClick = navigateToSearchExpander
        ) {
            Text(
                "Search Expander",
                modifier = Modifier.padding(16.dp),
                color = colorScheme.error
            )
        }

        OutlinedCard(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .height(56.dp),
            onClick = navigateToCurvedScreen
        ) {
            Text(
                "Curved Screen",
                modifier = Modifier.padding(16.dp),
                color = colorScheme.error
            )
        }

        OutlinedCard(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .height(56.dp),
            onClick = navigateToCounter
        ) {
            Text(
                "Counter with FlowRedux2",
                modifier = Modifier.padding(16.dp),
                color = colorScheme.error
            )
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            OutlinedCard(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .height(56.dp),
                onClick = navigateToToJulia
            ) {
                Text(
                    "Julia",
                    modifier = Modifier.padding(16.dp),
                    color = colorScheme.error
                )
            }

            OutlinedCard(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .height(56.dp),
                onClick = navigateToMandelbrot
            ) {
                Text(
                    "Mandelbrot",
                    modifier = Modifier.padding(16.dp),
                    color = colorScheme.error
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
            navigateToFirstLineAlignedCheckBox = {},
            navigateToDragOrTransformBox = {},
            navigateToKenBurnsEffect = {},
            navigateToSearchExpander = {},
            navigateToCurvedScreen = {},
            navigateToCounter = {},
            navigateToToJulia = {},
            navigateToMandelbrot = {}
        )
    }
}