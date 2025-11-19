package com.dhimandasgupta.funposables.composables

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.dhimandasgupta.funposables.ui.theme.FunposablesTheme

@Composable
fun SearchExpander(
    modifier: Modifier = Modifier,
    windowSizeClass: WindowSizeClass,
) {
    val searchTextState = rememberTextFieldState()

    var searchClicked by remember { mutableStateOf(false) }
    val onSearchClicked = {
        if (searchClicked) {
            searchTextState.clearText()
        }
        searchClicked = !searchClicked
    }

    Scaffold(
        modifier = modifier,
    ) { innerPadding ->
        FunposablesTheme {
            Column(
                modifier = Modifier
                    .consumeWindowInsets(innerPadding),
            ) {
                Toolbar(
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
                    searchClicked = searchClicked,
                    onSearchClicked = onSearchClicked,
                    searchTextState = searchTextState
                )
                Body(
                    modifier = Modifier
                        .padding(
                            start = WindowInsets
                                .displayCutout.union(insets = WindowInsets.navigationBars)
                                .asPaddingValues()
                                .calculateStartPadding(LayoutDirection.Ltr),
                            end = WindowInsets
                                .displayCutout.union(insets = WindowInsets.navigationBars)
                                .asPaddingValues()
                                .calculateEndPadding(LayoutDirection.Ltr),
                            bottom = WindowInsets
                                .displayCutout.union(insets = WindowInsets.navigationBars)
                                .asPaddingValues()
                                .calculateBottomPadding()
                        )
                        .fillMaxSize(),
                    searchTextState = searchTextState
                )
            }
        }
    }
}

@Composable
private fun Toolbar(
    modifier: Modifier = Modifier,
    searchTextState: TextFieldState,
    searchClicked: Boolean,
    onSearchClicked: () -> Unit = {}
) {
    AnimatedContent(
        targetState = searchClicked,
        modifier = modifier,
        transitionSpec = {
            //fadeIn() togetherWith fadeOut()
            slideInHorizontally() + fadeIn() togetherWith slideOutHorizontally() + fadeOut()
        },
        label = "ToolbarAnimation"
    ) { isSearchActive ->
        if (isSearchActive) {
            ExpandedToolbar(
                searchTextState = searchTextState,
                onSearchClicked = onSearchClicked
            )
        } else {
            CollapsedToolbar(onSearchClicked = onSearchClicked)
        }
    }
}

@Composable
private fun CollapsedToolbar(
    modifier: Modifier = Modifier,
    onSearchClicked: () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = 16.dp,
                vertical = 8.dp
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Text(
            text = "Toolbar text",
            style = typography.labelLarge,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Search",
            modifier = Modifier.clickable(onClick = onSearchClicked)
        )
        Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = "Settings"
        )
    }
}

@Composable
private fun ExpandedToolbar(
    modifier: Modifier = Modifier,
    searchTextState: TextFieldState,
    onSearchClicked: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = 16.dp,
                vertical = 8.dp
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Search",
            modifier = Modifier.clickable(onClick = onSearchClicked)
        )

        val focusRequester = remember { FocusRequester() }

        BasicTextField(
            state = searchTextState,
            lineLimits = TextFieldLineLimits.SingleLine,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp)
                .focusRequester(focusRequester),
            textStyle = typography.labelLarge.copy(color = colorScheme.onSurface),
            cursorBrush = SolidColor(colorScheme.onSurface),
        )

        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }
    }
}


@Composable
private fun Body(
    modifier: Modifier = Modifier,
    searchTextState: TextFieldState
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = searchTextState.text.toString(),
            style = typography.headlineLarge,
            modifier = Modifier.padding(all = 32.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview
@Composable
private fun SearchExpanderPreview() {
    FunposablesTheme {
        SearchExpander(
            windowSizeClass = WindowSizeClass.calculateFromSize(
                size = DpSize(
                    width = 360.dp,
                    height = 780.dp
                )
            )
        )
    }
}