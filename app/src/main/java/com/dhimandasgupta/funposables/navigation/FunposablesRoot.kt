package com.dhimandasgupta.funposables.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.dhimandasgupta.funposables.composables.bggrid.BackgroundGrid
import com.dhimandasgupta.funposables.composables.checkbox.FirstLineAlignedCheckbox
import com.dhimandasgupta.funposables.composables.circularlayout.CircularLayoutPane
import com.dhimandasgupta.funposables.composables.counter.Counter
import com.dhimandasgupta.funposables.composables.curvedlayout.CurvedLayout
import com.dhimandasgupta.funposables.composables.drag.DragOrTransformBox
import com.dhimandasgupta.funposables.composables.expandable.ExpandableCollapsableItems
import com.dhimandasgupta.funposables.composables.interactivejulia.InteractiveJulia
import com.dhimandasgupta.funposables.composables.kenburns.KenBurnsEffectPane
import com.dhimandasgupta.funposables.composables.launcher.Launcher
import com.dhimandasgupta.funposables.composables.mandelbrot.Mandelbrot
import com.dhimandasgupta.funposables.composables.orbitalloader.OrbitalLoader
import com.dhimandasgupta.funposables.composables.richhtml.RichHTMLText
import com.dhimandasgupta.funposables.composables.richmarkdown.RichTextMarkdownText
import com.dhimandasgupta.funposables.composables.search.SearchExpander
import com.dhimandasgupta.funposables.composables.starynight.StarryNightCanvas
import com.dhimandasgupta.funposables.composables.subway.SubwayPane
import com.dhimandasgupta.funposables.di.LocalFunposablesGraph
import com.freeletics.flowredux2.produceStateMachine

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun FunposablesRoot(modifier: Modifier) {
  val backStack = rememberNavBackStack(LauncherNavKey)
  val sceneStrategy = rememberListDetailSceneStrategy<NavKey>()

  SharedTransitionLayout(modifier = modifier) {
    NavDisplay(
      modifier = Modifier.fillMaxSize(),
      backStack = backStack,
      sceneStrategies = listOf(sceneStrategy),
      onBack = { backStack.removeLastOrNull() },
      sharedTransitionScope = this,
      transitionSpec = {
        slideIntoContainer(towards = AnimatedContentTransitionScope.SlideDirection.Start) {
          initialOffSet ->
          initialOffSet
        } togetherWith
          slideOutOfContainer(towards = AnimatedContentTransitionScope.SlideDirection.End) {
            initialOffSet ->
            -initialOffSet
          }
      },
      popTransitionSpec = {
        slideIntoContainer(towards = AnimatedContentTransitionScope.SlideDirection.Start) {
          initialOffSet ->
          -initialOffSet
        } togetherWith
          slideOutOfContainer(towards = AnimatedContentTransitionScope.SlideDirection.End) {
            initialOffSet ->
            initialOffSet
          }
      },
      predictivePopTransitionSpec = {
        slideIntoContainer(towards = AnimatedContentTransitionScope.SlideDirection.Start) {
          initialOffSet ->
          -initialOffSet
        } + fadeIn() togetherWith
          slideOutOfContainer(towards = AnimatedContentTransitionScope.SlideDirection.End) {
            initialOffSet ->
            initialOffSet
          } + fadeOut()
      },
      entryProvider =
        entryProvider {
          entry<LauncherNavKey>(metadata = ListDetailSceneStrategy.listPane()) {
            Launcher(
              modifier = modifier,
              navigateToExpandableCollapsableItems = {
                backStack.add(ExpandableCollapsableItemsNavKey)
              },
              navigateToFirstLineAlignedCheckBox = {
                backStack.add(FirstLineAlignedCheckBoxNavKey)
              },
              navigateToDragOrTransformBox = {
                backStack.add(DragOrTransformBoxNavKey)
              },
              navigateToKenBurnsEffect = {
                backStack.add(KenBurnsEffectNavKey)
              },
              navigateToSearchExpander = {
                backStack.add(SearchExpanderNavKey)
              },
              navigateToCurvedScreen = {
                backStack.add(CurvedLayoutNavKey)
              },
              navigateToCounter = {
                backStack.add(CounterNavKey)
              },
              navigateToToJulia = {
                backStack.add(InteractiveJuliaNavKey)
              },
              navigateToMandelbrot = {
                backStack.add(MandelbrotNavKey)
              },
              navigateToCircularLayout = {
                backStack.add(CircularLayoutNavKey)
              },
              navigateToOrbitalLoader = {
                backStack.add(OrbitalLoaderNavKey)
              },
              navigateToBackgroundGrid = {
                backStack.add(BackgroundGridNavKey)
              },
              navigateToRichTextHTML = {
                backStack.add(RichTextHTMLNavKey)
              },
              navigateToRichTextMarkdown = {
                backStack.add(RichTextMarkdownNavKey)
              },
              navigateToSubway = {
                backStack.add(SubwayNavKey)
              },
              navigateToStaryNight = {
                backStack.add(StaryNightNavKey)
              },
            )
          }
          entry<ExpandableCollapsableItemsNavKey>(metadata = ListDetailSceneStrategy.detailPane()) {
            ExpandableCollapsableItems(modifier = modifier)
          }
          entry<FirstLineAlignedCheckBoxNavKey>(metadata = ListDetailSceneStrategy.detailPane()) {
            FirstLineAlignedCheckbox(modifier = modifier)
          }
          entry<DragOrTransformBoxNavKey>(metadata = ListDetailSceneStrategy.detailPane()) {
            DragOrTransformBox(modifier = modifier)
          }
          entry<KenBurnsEffectNavKey>(metadata = ListDetailSceneStrategy.detailPane()) {
            KenBurnsEffectPane(modifier = modifier)
          }
          entry<SearchExpanderNavKey>(metadata = ListDetailSceneStrategy.detailPane()) {
            SearchExpander(modifier = modifier)
          }
          entry<CurvedLayoutNavKey>(metadata = ListDetailSceneStrategy.detailPane()) {
            CurvedLayout(modifier = modifier)
          }
          entry<CounterNavKey>(metadata = ListDetailSceneStrategy.detailPane()) {
            val graph = LocalFunposablesGraph.current
            val counterStateMachineFactory = remember { graph.counterStateMachineFactory }
            println("CounterStateMachineFactory: $counterStateMachineFactory")
            val counterStateMachine = counterStateMachineFactory.produceStateMachine()

            Counter(
              modifier = modifier,
              counterBaseState = { counterStateMachine.state.value },
              dispatch = counterStateMachine.dispatchAction,
            )
          }
          entry<InteractiveJuliaNavKey>(metadata = ListDetailSceneStrategy.detailPane()) {
            InteractiveJulia(modifier = modifier)
          }
          entry<MandelbrotNavKey>(metadata = ListDetailSceneStrategy.detailPane()) {
            Mandelbrot(modifier = modifier)
          }
          entry<CircularLayoutNavKey>(metadata = ListDetailSceneStrategy.detailPane()) {
            CircularLayoutPane(modifier = modifier)
          }
          entry<OrbitalLoaderNavKey>(metadata = ListDetailSceneStrategy.detailPane()) {
            OrbitalLoader(modifier = modifier)
          }
          entry<BackgroundGridNavKey>(metadata = ListDetailSceneStrategy.detailPane()) {
            BackgroundGrid(modifier = modifier)
          }
          entry<RichTextHTMLNavKey>(metadata = ListDetailSceneStrategy.detailPane()) {
            RichHTMLText(modifier = modifier)
          }
          entry<RichTextMarkdownNavKey>(metadata = ListDetailSceneStrategy.detailPane()) {
            RichTextMarkdownText(modifier = modifier)
          }
          entry<SubwayNavKey>(metadata = ListDetailSceneStrategy.detailPane()) {
            SubwayPane(modifier = modifier)
          }
          entry<StaryNightNavKey>(metadata = ListDetailSceneStrategy.detailPane()) {
            StarryNightCanvas(modifier = modifier)
          }
        },
    )
  }
}
