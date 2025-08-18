package com.dhimandasgupta.funposables

import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.dhimandasgupta.funposables.composables.ExpandableCollapsableItems
import com.dhimandasgupta.funposables.composables.Launcher
import kotlinx.serialization.Serializable

@Composable
fun App(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    windowSizeClass: WindowSizeClass
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = AppDestinations.RootPane,
        enterTransition = { slideIn { IntOffset(x = it.width, y = 0) } },
        exitTransition = { slideOut { IntOffset(x = -it.width, y = 0) } },
        popEnterTransition = { slideIn { IntOffset(x = -it.width / 2, y = 0) } },
        popExitTransition = { slideOut { IntOffset(x = it.width / 2, y = 0) } }
    ) {
        appGraph(
            navController = navController,
            windowSizeClass = windowSizeClass
        )
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
private fun NavGraphBuilder.appGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    windowSizeClass: WindowSizeClass
) {
    navigation<AppDestinations.RootPane>(
        startDestination = AppDestinations.LauncherPane
    ) {
        composable<AppDestinations.LauncherPane> {
            Launcher(
                modifier = modifier,
                windowSizeClass = windowSizeClass,
                navigateToExpandableCollapsableItems = {
                    navController.navigate(AppDestinations.ExpandableCollapsableItems)
                }
            )
        }

        composable<AppDestinations.ExpandableCollapsableItems> {
            ExpandableCollapsableItems(
                modifier = modifier,
                windowSizeClass = windowSizeClass,
            )
        }
    }
}

object AppDestinations {
    @Serializable
    data object RootPane

    @Serializable
    data object LauncherPane

    @Serializable
    data object ExpandableCollapsableItems
}