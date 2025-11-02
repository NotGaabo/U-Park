package com.kotlin.u_park.presentation.navigation

sealed class Routes(val route: String) {
    object Splash : Routes("splash")
    object Login : Routes("login")
    object Register : Routes("register")
    object Home : Routes("home")
    object DuenoGarage : Routes("duenogarage")
    object Detalles : Routes("detalles")
    object Settings : Routes("settings")
    object SettingsDueno : Routes("settingsdueno")
    object GarageAdd : Routes("newgarage")
}
