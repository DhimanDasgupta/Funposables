package com.dhimandasgupta.funposables.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.dhimandasgupta.funposables.composables.Counter
import com.dhimandasgupta.funposables.composables.CurvedLayout
import com.dhimandasgupta.funposables.composables.DragOrTransformBox
import com.dhimandasgupta.funposables.composables.ExpandableCollapsableItems
import com.dhimandasgupta.funposables.composables.FirstLineAlignedCheckbox
import com.dhimandasgupta.funposables.composables.InteractiveJulia
import com.dhimandasgupta.funposables.composables.KenBurnsEffectPane
import com.dhimandasgupta.funposables.composables.Launcher
import com.dhimandasgupta.funposables.composables.Mandelbrot
import com.dhimandasgupta.funposables.composables.SearchExpander

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun FunposablesRoot(
    modifier: Modifier
) {
    val backStack = rememberNavBackStack(LauncherNavKey)
    val sceneStrategy = rememberListDetailSceneStrategy<NavKey>()

    NavDisplay(
        modifier = modifier,
        backStack = backStack,
        sceneStrategy = sceneStrategy,
        onBack = { backStack.removeLastOrNull() },
        transitionSpec = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
            ) { initialOffSet -> initialOffSet } togetherWith slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
            ) { initialOffSet -> -initialOffSet }
        },
        popTransitionSpec = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
            ) { initialOffSet -> -initialOffSet } togetherWith slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
            ) { initialOffSet -> initialOffSet }
        },
        predictivePopTransitionSpec = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
            ) { initialOffSet -> -initialOffSet } + fadeIn() togetherWith slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
            ) { initialOffSet -> initialOffSet } + fadeOut()
        },
        entryProvider = entryProvider {
            entry<LauncherNavKey>(
                metadata = ListDetailSceneStrategy.listPane()
            ) {
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
                        backStack.add(InteractiveJulia)
                    },
                    navigateToMandelbrot = {
                        backStack.add(Mandelbrot)
                    }
                )
            }
            entry<ExpandableCollapsableItemsNavKey>(
                metadata = ListDetailSceneStrategy.detailPane()
            ) {
                ExpandableCollapsableItems(
                    modifier = modifier
                )
            }
            entry<FirstLineAlignedCheckBoxNavKey>(
                metadata = ListDetailSceneStrategy.detailPane()
            ) {
                FirstLineAlignedCheckbox(
                    modifier = modifier
                )
            }
            entry<DragOrTransformBoxNavKey>(
                metadata = ListDetailSceneStrategy.detailPane()
            ) {
                DragOrTransformBox(
                    modifier = modifier
                )
            }
            entry<KenBurnsEffectNavKey>(
                metadata = ListDetailSceneStrategy.detailPane()
            ) {
                KenBurnsEffectPane(
                    modifier = modifier
                )
            }
            entry<SearchExpanderNavKey>(
                metadata = ListDetailSceneStrategy.detailPane()
            ) {
                SearchExpander(
                    modifier = modifier
                )
            }
            entry<CurvedLayoutNavKey>(
                metadata = ListDetailSceneStrategy.detailPane()
            ) {
                CurvedLayout(
                    modifier = modifier
                )
            }
            entry<CounterNavKey>(
                metadata = ListDetailSceneStrategy.detailPane()
            ) {
                Counter(
                    modifier = modifier
                )
            }
            entry<InteractiveJulia>(
                metadata = ListDetailSceneStrategy.detailPane()
            ) {
                InteractiveJulia(
                    modifier = modifier
                )
            }
            entry<Mandelbrot>(
                metadata = ListDetailSceneStrategy.detailPane()
            ) {
                Mandelbrot(
                    modifier = modifier
                )
            }
        }
    )
}