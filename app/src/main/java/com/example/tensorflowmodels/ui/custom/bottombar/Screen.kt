package com.example.tensorflowmodels.ui.custom.bottombar

import kotlinx.serialization.Serializable

sealed interface Screen {
    @Serializable
    data object Tensorflow : Screen
}
