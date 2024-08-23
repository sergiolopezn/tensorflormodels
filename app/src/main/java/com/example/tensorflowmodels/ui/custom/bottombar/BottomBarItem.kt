package com.example.tensorflowmodels.ui.custom.bottombar

import androidx.compose.ui.graphics.vector.ImageVector

data class BottomBarItem(
    val route: Screen,
    val label: Int,
    val icon: ImageVector,
    val enabled: Boolean = true
)

