package com.example.tensorflowmodels.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import com.example.tensorflowmodels.R
import com.example.tensorflowmodels.ui.custom.bottombar.BottomBarItem
import com.example.tensorflowmodels.ui.custom.bottombar.Screen

class AppState {
    companion object{
        val bottomBarTabs = listOf(
            BottomBarItem(
                route = Screen.Tensorflow,
                label = R.string.screen_fruits,
                icon = Icons.Filled.Home
            ),
        )
    }
}