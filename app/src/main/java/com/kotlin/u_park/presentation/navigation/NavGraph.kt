package com.kotlin.u_park.presentation.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.kotlin.u_park.data.remote.SessionManager
import com.kotlin.u_park.data.remote.supabase
import com.kotlin.u_park.data.repository.GarageRepositoryImpl
import com.kotlin.u_park.data.repository.EmpleadoGarageRepositoryImpl
import com.kotlin.u_park.data.repository.ParkingRepositoryImpl
import com.kotlin.u_park.data.repository.RatesRepositoryImpl
import com.kotlin.u_park.data.repository.ReservasRepositoryImpl
import com.kotlin.u_park.data.repository.VehiclesRepositoryImpl
import com.kotlin.u_park.presentation.screens.auth.*
import com.kotlin.u_park.presentation.screens.detalles.DetallesScreen
import com.kotlin.u_park.presentation.screens.employee.*
import com.kotlin.u_park.presentation.screens.garage.*
import com.kotlin.u_park.presentation.screens.home.*
import com.kotlin.u_park.presentation.screens.parking.ParkingHistoryScreen
import com.kotlin.u_park.presentation.screens.parking.ParkingViewModel
import com.kotlin.u_park.presentation.screens.parking.ParkingViewModelFactory
import com.kotlin.u_park.presentation.screens.profile.*
import com.kotlin.u_park.presentation.screens.rates.RateFormScreen
import com.kotlin.u_park.presentation.screens.rates.RatesScreen
import com.kotlin.u_park.presentation.screens.rates.RatesViewModel
import com.kotlin.u_park.presentation.screens.rates.RatesViewModelFactory
import com.kotlin.u_park.presentation.screens.splash.SplashScreen
import com.kotlin.u_park.presentation.screens.vehicles.VehicleScreen
import com.kotlin.u_park.presentation.screens.vehicles.VehiclesViewModel
import com.kotlin.u_park.presentation.screens.vehicles.VehiclesViewModelFactory
import com.kotlin.u_park.data.repository.SubscriptionRepository
import com.kotlin.u_park.presentation.screens.suscriptions.ManageSubscriptionScreen
import com.kotlin.u_park.presentation.screens.suscriptions.SubscriptionScreen
import com.kotlin.u_park.presentation.screens.suscriptions.SubscriptionViewModel
import com.kotlin.u_park.presentation.screens.suscriptions.SubscriptionViewModelFactory


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NavGraph(
    navController: NavHostController,
    sessionManager: SessionManager,
    authViewModel: AuthViewModel,
    startDestination: String = Routes.Splash.route
) {
    val subscriptionRepository = remember { SubscriptionRepository() }
    val garageRepository = remember { GarageRepositoryImpl(supabase) }
    val parkingRepository = remember { ParkingRepositoryImpl(supabase) }
    val reservasRepository = remember { ReservasRepositoryImpl(supabase) }
    val parkingViewModel: ParkingViewModel = viewModel(
        factory = ParkingViewModelFactory(
            parkingRepository,
            reservasRepository,
            sessionManager
        )
    )
    val currentEmpleado by sessionManager.getUserFlow().collectAsState(initial = null)
    val empleadoId = currentEmpleado?.id ?: ""


    NavHost(navController = navController, startDestination = startDestination) {

        // -------------------- SPLASH --------------------
        composable(Routes.Splash.route) {
            SplashScreen(
                navController = navController,
                sessionManager = sessionManager,
                supabase = supabase,
                authViewModel = authViewModel
            )
        }

        // -------------------- AUTH --------------------
        composable(Routes.Register.route) {
            RegisterScreen(navController, supabase)
        }

        composable(Routes.Login.route) {
            LoginScreen(navController, supabase)
        }

        // -------------------- HOME --------------------
        composable(Routes.Home.route) {
            HomeScreen(navController, authViewModel)
        }

        // -------------------- DETALLES --------------------
        composable(
            route = Routes.Detalles.route,
        ) { backStackEntry ->
            val garageId = backStackEntry.arguments?.getString("garageId") ?: ""

            DetallesScreen(
                navController = navController,
                garageId = garageId
            )
        }

        // -------------------- SETTINGS --------------------
        composable(Routes.Settings.route) {
            SettingsScreen(
                navController = navController,
                supabase = supabase,
                onSignOut = {
                    authViewModel.signOut()
                    navController.navigate(Routes.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // -------------------- SETTINGS DUENO --------------------
        composable(Routes.SettingsDueno.route) {
            SettingsScreenDueno(
                navController,
                currentUser = authViewModel.currentUser.value,
                sessionManager = sessionManager,
                onSignOut = {
                    authViewModel.signOut()
                    navController.navigate(Routes.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // -------------------- GESTIÓN DE GARAGE (EMPLEADOS + SUSCRIPCIONES) --------------------
        composable(
            route = Routes.GarageManagement.route,
            arguments = listOf(
                navArgument("garageId") { type = NavType.StringType },
                navArgument("garageName") { type = NavType.StringType }
            )
        ) { backStackEntry ->

            val garageId = backStackEntry.arguments?.getString("garageId") ?: ""
            val garageName = backStackEntry.arguments?.getString("garageName") ?: ""

            val empleadoRepo = remember { EmpleadoGarageRepositoryImpl(supabase) }
            val parkingRepo = remember { ParkingRepositoryImpl(supabase) }

            val empleadosViewModel: EmpleadosViewModel = viewModel(
                backStackEntry,
                factory = EmpleadosViewModelFactory(
                    empleadoRepo = empleadoRepo,
                    parkingRepo = parkingRepo
                )
            )

            val subscriptionViewModel: SubscriptionViewModel = viewModel(
                backStackEntry,
                factory = SubscriptionViewModelFactory(subscriptionRepository)
            )

            // ⚠️ Cargar SOLO aquí
            LaunchedEffect(garageId) {
                if (garageId.isNotBlank()) {
                    empleadosViewModel.loadEmpleados(garageId)
                    empleadosViewModel.loadStats(garageId)
                    empleadosViewModel.loadActividad(garageId)
                }
            }

            GarageManagementScreen(
                navController = navController,
                garageId = garageId,
                garageName = garageName,
                empleadosViewModel = empleadosViewModel,
                subscriptionViewModel = subscriptionViewModel
            )
        }

        composable(
            route = Routes.GarageDashboard.route,
            arguments = listOf(
                navArgument("garageId") { type = NavType.StringType },
                navArgument("garageName") { type = NavType.StringType }
            )
        ) { backStack ->

            val garageId = backStack.arguments?.getString("garageId") ?: ""
            val garageName = backStack.arguments?.getString("garageName") ?: ""

            GarageDashboardScreen(
                garageId = garageId,
                garageName = garageName,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEmployees = {
                    navController.navigate(
                        Routes.GarageManagement.createRoute(garageId, garageName)
                    ) {
                        launchSingleTop = true
                    }
                },
                onNavigateToSubscriptions = {
                    navController.navigate(
                        Routes.GarageManagement.createRoute(garageId, garageName)
                    ) {
                        launchSingleTop = true
                    }
                }
            )
        }


        // -------------------- AGREGAR EMPLEADOS --------------------
        composable(Routes.AgregarEmpleado.route) { backStackEntry ->
            val garageId = backStackEntry.arguments?.getString("garageId") ?: ""

            val empleadoRepo = remember { EmpleadoGarageRepositoryImpl(supabase) }
            val parkingRepo = remember { ParkingRepositoryImpl(supabase) }
            val viewModel: EmpleadosViewModel = viewModel(
                backStackEntry,
                factory = EmpleadosViewModelFactory(
                    empleadoRepo = empleadoRepo,
                    parkingRepo = parkingRepo
                )
            )

            AgregarEmpleadoScreen(
                garageId = garageId,
                viewModel = viewModel,
                onClose = { navController.popBackStack() }
            )
        }

        // -------------------- DUEÑO DE GARAGE --------------------
        composable(Routes.DuenoGarage.route) { backStackEntry ->
            val user by authViewModel.currentUser.collectAsState()
            val userId = user?.id ?: ""

            val viewModel: GarageViewModel = viewModel(
                backStackEntry,
                factory = GarageViewModelFactory(garageRepository)
            )

            DuenoGarageScreen(
                navController = navController,
                viewModel = viewModel,
                userId = userId
            )
        }

        // -------------------- EMPLOYEE HOME --------------------
        composable(Routes.EmployeeHome.route) { backStackEntry ->

            val currentUser by authViewModel.currentUser.collectAsState()

            val empleadoRepo = remember { EmpleadoGarageRepositoryImpl(supabase) }
            val parkingRepo = remember { ParkingRepositoryImpl(supabase) }
            val reservasRepo = remember { ReservasRepositoryImpl(supabase) }

            var garageId by remember { mutableStateOf("") }
            var parkingId by remember { mutableStateOf("") }

            LaunchedEffect(currentUser) {
                currentUser?.let {
                    garageId = empleadoRepo.getGarageByEmpleadoId(it.cedula!!.toLong()) ?: ""
                }
            }

            if (garageId.isNotBlank()) {

                val empleadosViewModel: EmpleadosViewModel = viewModel(
                    backStackEntry,
                    factory = EmpleadosViewModelFactory(
                        empleadoRepo = empleadoRepo,
                        parkingRepo = parkingRepo
                    )
                )

                val parkingViewModel: ParkingViewModel = viewModel(
                    backStackEntry,
                    factory = ParkingViewModelFactory(
                        parkingRepository = parkingRepo,
                        reservasRepository = reservasRepo,
                        sessionManager = sessionManager
                    )
                )

                EmployeeHomeScreen(
                    navController = navController,
                    garageId = garageId,
                    parkingId = parkingId,
                    viewModel = empleadosViewModel,
                    parkingViewModel = parkingViewModel   // 🔥 YA NO FALTA
                )
            }
        }

        // -------------------- VEHICULOS DENTRO --------------------
        composable(Routes.VehiculosDentro.route,listOf(navArgument("garageId") { type = NavType.StringType })
        ) { backStackEntry ->

            val garageId = backStackEntry.arguments?.getString("garageId") ?: ""

            VehiculosDentroScreen(
                navController = navController,
                parkingViewModel = parkingViewModel,
                garageId = garageId
            )
        }

        // -------------------- REGISTRAR ENTRADA --------------------
        composable(Routes.RegistrarEntrada.route,listOf(navArgument("garageId") { type = NavType.StringType })
        ) { backStackEntry ->

            val garageId = backStackEntry.arguments?.getString("garageId") ?: ""

            RegistrarEntradaScreen(
                navController = navController,
                viewModel = parkingViewModel,
                garageId = garageId,
                empleadoId = empleadoId    // 🟩 ID real desde SessionManager
            )
        }

        // -------------------- REGISTRAR SALIDA --------------------
        composable(Routes.RegistrarSalida.route, listOf(navArgument("parkingId") { type = NavType.StringType })
        ) { backStackEntry ->

            val parkingId = backStackEntry.arguments?.getString("parkingId") ?: ""

            // RatesViewModel
            val ratesRepo = remember { RatesRepositoryImpl(supabase) }
            val ratesViewModel: RatesViewModel = viewModel(
                factory = RatesViewModelFactory(ratesRepo)
            )

            // ParkingViewModel  ← NECESARIO AHORA
            val parkingRepo = remember { ParkingRepositoryImpl(supabase) }
            val reservasRepo = remember { ReservasRepositoryImpl(supabase) }
            val parkingViewModel: ParkingViewModel = viewModel(
                factory = ParkingViewModelFactory(parkingRepo, reservasRepo, sessionManager)
            )

            RegistrarSalidaScreen(
                parkingId = parkingId,
                ratesViewModel = ratesViewModel,
                parkingViewModel = parkingViewModel,
                navController = navController
            )
        }

        // -------------------- REGISTRAR RESERVA --------------------
        composable(Routes.RegistrarReserva.route, listOf(navArgument("garageId") { type = NavType.StringType })
        ) { backStackEntry ->

            val garageId = backStackEntry.arguments?.getString("garageId") ?: ""
            val currentUser by authViewModel.currentUser.collectAsState()

            val userId = currentUser?.id ?: ""

            RegistrarReservaScreen(
                viewModel = parkingViewModel,
                garageId = garageId,
                navController = navController,
                userId = userId
            )
        }

        // -------------------- HISTORIAL PARKING --------------------
        composable(
            Routes.HistorialParking.route,
            listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->

            val userId = backStackEntry.arguments?.getString("userId") ?: ""

            ParkingHistoryScreen(
                viewModel = parkingViewModel,
                navController = navController,
                userId = userId
            )
        }


        // -------------------- LISTA DE RESERVAS --------------------
        composable(Routes.ListaReservas.route, listOf(navArgument("garageId") { type = NavType.StringType })
        ) { backStackEntry ->

            val garageId = backStackEntry.arguments?.getString("garageId") ?: ""
            val currentUser by authViewModel.currentUser.collectAsState()
            val userId = currentUser?.id ?: ""

            ListaReservasScreen(
                viewModel = parkingViewModel,
                garageId = garageId,
                userId = userId,
                garageRepository = garageRepository,
                navController = navController
            )
        }

        // -------------------- TARIFAS (ADMIN DUEÑO) --------------------
        composable(
            route = Routes.Rates.route,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->

            val userId = backStackEntry.arguments?.getString("userId") ?: ""

            // Crear RatesViewModel correctamente
            val ratesRepo = remember { RatesRepositoryImpl(supabase) }
            val ratesViewModel: RatesViewModel = viewModel(
                factory = RatesViewModelFactory(ratesRepo)
            )

            RatesScreen(
                navController = navController,
                viewModel = ratesViewModel,
                userId = userId,
                onCreateRate = { garageId ->
                    // Nueva tarifa
                    navController.navigate(
                        Routes.RateForm.createRoute(
                            userId = userId,
                            garageId = garageId,
                            rateId = "new"
                        )
                    )
                },
                onEditRate = { rateId ->
                    // Buscar la tarifa para conocer el garageId
                    val allRates = ratesViewModel.groupedRates.value.values.flatten()
                    val rate = allRates.firstOrNull { it.id == rateId }

                    if (rate != null) {
                        navController.navigate(
                            Routes.RateForm.createRoute(
                                userId = userId,
                                garageId = rate.garageId,
                                rateId = rateId
                            )
                        )
                    }
                }
            )
        }



        // -------------------- FORMULARIO CREAR / EDITAR TARIFA --------------------
        composable(
            route = Routes.RateForm.route,
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType },
                navArgument("garageId") { type = NavType.StringType },
                navArgument("rateId") { type = NavType.StringType }
            )
        ) { backStackEntry ->

            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val garageId = backStackEntry.arguments?.getString("garageId") ?: ""
            val rateId = backStackEntry.arguments?.getString("rateId") ?: "new"

            val ratesRepo = remember { RatesRepositoryImpl(supabase) }
            val ratesViewModel: RatesViewModel = viewModel(
                factory = RatesViewModelFactory(ratesRepo)
            )

            RateFormScreen(
                navController = navController,
                viewModel = ratesViewModel,
                userId = userId,
                garageId = garageId,
                rateId = rateId,
                onSaved = { navController.popBackStack() }
            )
        }

        // -------------------- VEHICULOS --------------------
        composable(Routes.Vehicles.route) {
            val currentUser by authViewModel.currentUser.collectAsState()
            val userId = currentUser?.id ?: ""

            val repository = remember { VehiclesRepositoryImpl(supabase) }
            val viewModel: VehiclesViewModel = viewModel(
                factory = VehiclesViewModelFactory(repository)
            )

            VehicleScreen(
                navController = navController,
                userId = userId,
                viewModel = viewModel
            )
        }
//        // -------------------- AGREGAR VEHICULO --------------------
        // -------------------- GESTIONAR SUSCRIPCIÓN --------------------
        composable(
            route = Routes.ManageSubscription.route,
            arguments = listOf(navArgument("garageId") { type = NavType.StringType })
        ) { backStackEntry ->

            val garageId = backStackEntry.arguments?.getString("garageId") ?: ""
            val currentUser by authViewModel.currentUser.collectAsState()
            val userId = currentUser?.id ?: ""

            val viewModel: SubscriptionViewModel = viewModel(
                backStackEntry,
                factory = SubscriptionViewModelFactory(subscriptionRepository)
            )

            // ✅ SOLO AQUÍ
            LaunchedEffect(garageId, userId) {
                if (garageId.isNotBlank() && userId.isNotBlank()) {
                    viewModel.loadGarageData(userId, garageId)
                }
            }

            ManageSubscriptionScreen(
                userId = userId,
                garageId = garageId,
                viewModel = viewModel
            )
        }



        // -------------------- SUSCRIBIRSE A GARAGE --------------------
        // ✅ CORRECTO - Con argumentos bien definidos
        composable(
            route = Routes.Subscription.route,
            arguments = listOf(
                navArgument("garageId") {
                    type = NavType.StringType
                    nullable = false
                }
            )
        ) { backStackEntry ->
            val garageId = backStackEntry.arguments?.getString("garageId") ?: ""
            val currentUser by authViewModel.currentUser.collectAsState()
            val userId = currentUser?.id ?: ""

            // ✅ Agrega este log para verificar
            println("🔍 NavGraph - garageId recibido: $garageId")

            val viewModel: SubscriptionViewModel = viewModel(
                backStackEntry,
                factory = SubscriptionViewModelFactory(subscriptionRepository)
            )

            SubscriptionScreen(
                userId = userId,
                garageId = garageId,
                user = currentUser,
                viewModel = viewModel,
                onSuccess = {
                    navController.popBackStack()
                }
            )
        }

//        // -------------------- AGREGAR VEHICULO --------------------
//        composable(Routes.VehicleAdd.route) {

        // -------------------- AGREGAR GARAGE --------------------
        composable(Routes.GarageAdd.route) { backStackEntry ->
            val currentUser by authViewModel.currentUser.collectAsState()

            currentUser?.let { user ->
                val vm: GarageViewModel = viewModel(
                    viewModelStoreOwner = backStackEntry,
                    factory = GarageViewModelFactory(garageRepository)
                )

                GarageAddScreen(
                    userId = user.id,
                    viewModel = vm,
                    onDismiss = { navController.popBackStack() },
                    onSuccess = {
                        navController.navigate(Routes.DuenoGarage.route) {
                            popUpTo(Routes.DuenoGarage.route) { inclusive = true }
                        }
                    }
                )
            }
        }

        composable(
            route = Routes.ParkingRecords.route,
            arguments = listOf(
                navArgument("garageId") {
                    type = NavType.StringType
                    nullable = false
                }
            )
        ) { backStackEntry ->

            val garageId = backStackEntry.arguments?.getString("garageId")
                ?: error("garageId no fue enviado a ParkingRecords")

            ParkingRecordsScreen(
                navController = navController,
                garageId = garageId,
                viewModel = parkingViewModel
            )
        }


        // -------------------- SETTINGS EMPLEADOS --------------------
        composable(
            route = Routes.EmployeeSettings.route,
            arguments = listOf(
                navArgument("garageId") { type = NavType.StringType }
            )
        ) { backStackEntry ->

            val garageId = backStackEntry.arguments!!.getString("garageId")!!

            SettingsEmployeeScreen(
                garageId = garageId,
                navController = navController,
                supabase = supabase,
                onSignOut = {
                    navController.navigate(Routes.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

    }
}