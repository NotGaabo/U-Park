package com.kotlin.u_park.presentation.navigation

sealed class Routes(val route: String) {
    object Splash : Routes("splash")
    object Empleados : Routes("empleados/{garageId}") {
        fun createRoute(garageId: String) = "empleados/$garageId"
    }

    // 📌 Ruta con parámetro
    object AgregarEmpleado : Routes("agregar_empleado/{garageId}") {
        fun createRoute(garageId: String) = "agregar_empleado/$garageId"
    }

    object RegistrarEntrada : Routes("registrarEntrada/{garageId}") {
        fun createRoute(garageId: String) = "registrarEntrada/$garageId"
    }

    object EmployeeHome : Routes("employee_home")
    object Login : Routes("login")
    object Register : Routes("register")
    object Home : Routes("home")
    object DuenoGarage : Routes("dueno_garage")

    object Detalles : Routes("garage/{garageId}") {
        fun createRoute(garageId: String) = "garage/$garageId"
    }

    object Settings : Routes("settings")
    object SettingsDueno : Routes("settingsdueno")
    object GarageAdd : Routes("new_garage")
}