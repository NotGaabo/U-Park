package com.kotlin.u_park.presentation.utils

data class UiError(
    val title: String = "Error",
    val message: String,
    val stacktrace: String
)
