package com.dhimandasgupta.funposables

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.dhimandasgupta.funposables.composables.animatedBackground
import com.dhimandasgupta.funposables.navigation.FunposablesRoot
import com.dhimandasgupta.funposables.ui.theme.FunposablesTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FunposablesTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .animatedBackground(
                                color1 = colorResource(R.color.purple_200),
                                color2 = colorResource(R.color.purple_700),
                                color3 = colorResource(R.color.teal_700)
                            )
                            .consumeWindowInsets(paddingValues = innerPadding)
                        ,
                        contentAlignment = Alignment.Center
                    ) {
                        FunposablesRoot(
                            modifier = Modifier.consumeWindowInsets(paddingValues = innerPadding)
                        )
                    }
                }
            }
        }
    }
}