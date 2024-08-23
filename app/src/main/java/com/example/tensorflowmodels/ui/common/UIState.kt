package com.example.tensorflowmodels.ui.common

sealed interface UiState {
    data object Success : UiState
    data class Error(val error: Throwable?) : UiState
    data object Loading : UiState
}