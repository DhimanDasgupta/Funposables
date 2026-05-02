package com.dhimandasgupta.funposables.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dhimandasgupta.funposables.ui.common.drawBackgroundGrid

@Composable
fun BackgroundGrid(
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier
        .fillMaxSize()
        .drawBackgroundGrid()
    )
}