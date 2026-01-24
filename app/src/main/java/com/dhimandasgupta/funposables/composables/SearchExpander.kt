package com.dhimandasgupta.funposables.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextField
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import com.dhimandasgupta.funposables.ui.theme.FunposablesTheme

@Composable
fun SearchExpander(
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
    ) { innerPadding ->
        FunposablesTheme {
            Column(
                modifier = Modifier
                    .consumeWindowInsets(innerPadding),
            ) {
                ExpandableTextFieldVisibility(
                    modifier = Modifier
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
                        ),
                )
            }
        }
    }
}

@Composable
fun ExpandableTextFieldVisibility(
    modifier: Modifier,
) {
    val searchTextState = rememberTextFieldState()
    var isExpanded by remember { mutableStateOf(false) }

    // Container Row
    Row(
        modifier = modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End // Pushes content to right initially
    ) {
        // The Search/Close Button
        IconButton(onClick = { isExpanded = !isExpanded }) {
            Icon(
                imageVector = if (isExpanded) Icons.Default.Close else Icons.Default.Search,
                contentDescription = "Toggle Search"
            )
        }

        // The Animation Container
        AnimatedVisibility(
            visible = isExpanded,
            // Animation for entering (Expand)
            enter = expandHorizontally(
                animationSpec = tween(durationMillis = 300),
                expandFrom = Alignment.End // Expands from right to left
            ) + fadeIn(),
            // Animation for exiting (Shrink)
            exit = shrinkHorizontally(
                animationSpec = tween(durationMillis = 300),
                shrinkTowards = Alignment.End
            ) + fadeOut()
        ) {
            TextField(
                state = searchTextState,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview
@Composable
private fun SearchExpanderPreview() {
    FunposablesTheme {
        SearchExpander()
    }
}