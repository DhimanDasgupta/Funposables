package com.dhimandasgupta.funposables.composables.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
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
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.NavigationEventTransitionState
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.dhimandasgupta.funposables.ui.theme.FunposablesTheme

@Composable
fun SearchExpander(modifier: Modifier = Modifier) {
  ExpandableTextFieldVisibility(
    modifier =
      modifier.padding(
        start =
          WindowInsets.displayCutout
            .union(insets = WindowInsets.navigationBars)
            .asPaddingValues()
            .calculateStartPadding(LayoutDirection.Ltr),
        top =
          WindowInsets.displayCutout
            .union(insets = WindowInsets.statusBars)
            .asPaddingValues()
            .calculateTopPadding(),
        end =
          WindowInsets.displayCutout
            .union(insets = WindowInsets.navigationBars)
            .asPaddingValues()
            .calculateEndPadding(LayoutDirection.Ltr),
        bottom =
          WindowInsets.displayCutout
            .union(insets = WindowInsets.navigationBars)
            .asPaddingValues()
            .calculateBottomPadding(),
      )
  )
}

@Composable
private fun ExpandableTextFieldVisibility(modifier: Modifier) {
  val searchTextState = rememberTextFieldState()
  var isExpanded by remember { mutableStateOf(false) }

  // Back collapses the search field before Nav3 pops the screen
  val navigationState = rememberNavigationEventState(currentInfo = NavigationEventInfo.None)
  NavigationBackHandler(
    state = navigationState,
    isBackEnabled = isExpanded,
    onBackCompleted = { isExpanded = false },
  )

  // Container Row
  Row(
    modifier = modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.End, // Pushes content to right initially
  ) {
    // The Search/Close Button
    IconButton(onClick = { isExpanded = !isExpanded }) {
      Icon(
        imageVector = if (isExpanded) Icons.Default.Close else Icons.Default.Search,
        contentDescription = "Toggle Search",
      )
    }

    // The Animation Container
    AnimatedVisibility(
      visible = isExpanded,
      // Animation for entering (Expand)
      enter =
        expandHorizontally(
          animationSpec = tween(durationMillis = 300),
          expandFrom = Alignment.End, // Expands from right to left
        ) + fadeIn(),
      // Animation for exiting (Shrink)
      exit =
        shrinkHorizontally(
          animationSpec = tween(durationMillis = 300),
          shrinkTowards = Alignment.End,
        ) + fadeOut(),
    ) {
      TextField(
        state = searchTextState,
        colors =
          TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
          ),
        modifier =
          Modifier.fillMaxWidth().graphicsLayer {
            // Shrink towards the end while the predictive back gesture is in progress
            val progress =
              (navigationState.transitionState as? NavigationEventTransitionState.InProgress)
                ?.latestEvent
                ?.progress ?: 0f
            alpha = 1f - progress
            scaleX = 1f - 0.2f * progress
            transformOrigin = TransformOrigin(pivotFractionX = 1f, pivotFractionY = 0.5f)
          },
      )
    }
  }
}

@Preview
@Composable
private fun SearchExpanderPreview() {
  FunposablesTheme {
    SearchExpander()
  }
}
