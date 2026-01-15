package com.kotlin.u_park.presentation.navigation

sealed class Routes(val route: String) {
    object Splash : Routes("splash")
    object Empleados : Routes("empleados/{garageId}") {
        fun createRoute(garageId: String) = "empleados/$garageId"
    }
    // 📌 Ruta con parámetro
    object AgregarEmpleado : Routes ("agregar_empleado/{garageId}") {
        fun createRoute(garageId: String) = "agregar_empleado/$garageId"
    }
    object RegistrarEntrada : Routes("registrarEntrada/{garageId}") {
        fun createRoute(garageId: String) = "registrarEntrada/$garageId"
    }
    object ParkingRecords : Routes("parking_records/{garageId}") {
        fun createRoute(garageId: String) = "parking_records/$garageId"
    }

    object VehiculosDentro : Routes("vehiculos_dentro/{garageId}") {
        fun createRoute(garageId: String) = "vehiculos_dentro/$garageId"
    }
    object RegistrarSalida : Routes("registrarSalida/{parkingId}") {
        fun createRoute(parkingId: String) = "registrarSalida/$parkingId"
    }
    object EmployeeSettings : Routes("employee_settings/{garageId}") {
        fun createRoute(garageId: String) = "employee_settings/$garageId"
    }
    object EmployeeHome : Routes("employee_home")
    object Login : Routes("login")
    object Register : Routes("register")
    object Home : Routes("home")
    object DuenoGarage : Routes("dueno_garage")
    object Detalles : Routes("garage/{garageId}") {
        fun createRoute(garageId: String) = "garage/$garageId"
    }
    object Rates : Routes("rates_list/{userId}") {
        fun createRoute(userId: String) = "rates_list/$userId"
    }

    object RateForm : Routes("rate_form/{userId}/{garageId}/{rateId}") {
        fun createRoute(
            userId: String,
            garageId: String,
            rateId: String = "new"
        ) = "rate_form/$userId/$garageId/$rateId"
    }

    // -------------------- SUSCRIPCIONES --------------------
    object ManageSubscription : Routes("manage_subscription/{garageId}") {
        fun createRoute(garageId: String) = "manage_subscription/$garageId"
    }
    // -------------------- SUSCRIBIRSE --------------------
    object Subscription : Routes("subscription/{garageId}") {
        fun createRoute(garageId: String) = "subscription/$garageId"
    }

    object GarageDashboard : Routes("garage_dashboard/{garageId}/{garageName}") {
        fun createRoute(garageId: String, garageName: String): String {
            return "garage_dashboard/$garageId/$garageName"
        }
    }


    // En Routes.kt, agrega:
    object GarageManagement : Routes("garage_management/{garageId}/{garageName}") {
        fun createRoute(garageId: String, garageName: String): String {
            return "garage_management/$garageId/$garageName"
        }
    }

    object GarageSuscripciones : Routes("garage_suscripciones/{garageId}/{garageName}") {
        fun createRoute(garageId: String, garageName: String): String {
            return "garage_suscripciones/$garageId/$garageName"
        }
    }
    object HistorialParking : Routes("historial_parking/{userId}") {
        fun createRoute(userId: String) = "historial_parking/$userId"
    }
    object Settings : Routes("settings")
    object SettingsDueno : Routes("settingsdueno")
    object GarageAdd : Routes("new_garage")
    // Crear una reserva
    object RegistrarReserva : Routes("registrarReserva/{garageId}") {
        fun createRoute(garageId: String) = "registrarReserva/$garageId"
    }
    // Listado de reservas
    object ListaReservas : Routes("listaReservas/{garageId}") {
        fun createRoute(garageId: String) = "listaReservas/$garageId"
    }
    object Vehicles : Routes("vehicles")
}