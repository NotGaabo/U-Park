package com.kotlin.u_park.ui.navigation

sealed class Routes(val route: String) {
    object Splash : Routes("splash")
    object Login : Routes("login")
    object Register : Routes("register")
    object Home : Routes("home")
    object Profile : Routes("profile")
    object Detalles : Routes("detalles")
    object Settings : Routes("settings")
}
