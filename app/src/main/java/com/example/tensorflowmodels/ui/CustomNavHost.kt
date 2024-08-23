package com.example.tensorflowmodels.ui


import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.tensorflowmodels.ui.custom.bottombar.Screen
import com.example.tensorflowmodels.ui.features.FaceRoute

@Composable
fun CustomNavHost(
    navController: NavHostController,
    padding: PaddingValues,
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Tensorflow,
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .consumeWindowInsets(padding)
    ) {
        composable<Screen.Tensorflow> {
            FaceRoute()
        }
    }
}