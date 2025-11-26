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
import com.kotlin.u_park.data.repository.ReservasRepositoryImpl
import com.kotlin.u_park.presentation.screens.auth.*
import com.kotlin.u_park.presentation.screens.detalles.DetallesScreen
import com.kotlin.u_park.presentation.screens.employee.*
import com.kotlin.u_park.presentation.screens.garage.*
import com.kotlin.u_park.presentation.screens.home.*
import com.kotlin.u_park.presentation.screens.parking.ParkingViewModel
import com.kotlin.u_park.presentation.screens.parking.ParkingViewModelFactory
import com.kotlin.u_park.presentation.screens.profile.*
import com.kotlin.u_park.presentation.screens.splash.SplashScreen

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NavGraph(
    navController: NavHostController,
    sessionManager: SessionManager,
    authViewModel: AuthViewModel,
    startDestination: String = Routes.Splash.route
) {
    val garageRepository = remember { GarageRepositoryImpl(supabase) }
    val parkingRepository = remember { ParkingRepositoryImpl(supabase) }
    val reservasRepository = remember { ReservasRepositoryImpl(supabase) }
    val parkingViewModel: ParkingViewModel = viewModel(
        factory = ParkingViewModelFactory(
            parkingRepository,
            reservasRepository
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

        // -------------------- EMPLEADOS --------------------
        composable(Routes.Empleados.route) { backStackEntry ->
            val garageId = backStackEntry.arguments?.getString("garageId") ?: ""

            val repo = remember { EmpleadoGarageRepositoryImpl(supabase) }
            val viewModel: EmpleadosViewModel = viewModel(
                backStackEntry,
                factory = EmpleadosViewModelFactory(repo)
            )

            LaunchedEffect(garageId) {
                if (garageId.isNotBlank()) {
                    viewModel.loadEmpleados(garageId)
                }
            }

            EmpleadosScreen(
                garageId = garageId,
                viewModel = viewModel,
                onAgregarEmpleado = {
                    navController.navigate(Routes.AgregarEmpleado.createRoute(garageId))
                }
            )
        }

        composable(Routes.AgregarEmpleado.route) { backStackEntry ->
            val garageId = backStackEntry.arguments?.getString("garageId") ?: ""

            val repo = remember { EmpleadoGarageRepositoryImpl(supabase) }
            val viewModel: EmpleadosViewModel = viewModel(
                backStackEntry,
                factory = EmpleadosViewModelFactory(repo)
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
            val repo = remember { EmpleadoGarageRepositoryImpl(supabase) }

            var garageId by remember { mutableStateOf("") }

            LaunchedEffect(currentUser) {
                currentUser?.let {
                    garageId = repo.getGarageByEmpleadoId(it.cedula!!.toLong()) ?: ""
                }
            }

            if (garageId.isNotBlank()) {
                EmployeeHomeScreen(
                    navController = navController,
                    garageId = garageId,
                    viewModel = viewModel(
                        backStackEntry,
                        factory = EmpleadosViewModelFactory(repo)
                    )
                )
            }
        }
        // -------------------- REGISTRAR ENTRADA --------------------
        composable(
            route = Routes.RegistrarEntrada.route,
            arguments = listOf(navArgument("garageId") { type = NavType.StringType })
        ) { backStackEntry ->

            val garageId = backStackEntry.arguments?.getString("garageId") ?: ""

            RegistrarEntradaScreen(
                navController = navController,
                viewModel = parkingViewModel,
                garageId = garageId,
                empleadoId = empleadoId    // 🟩 ID real desde SessionManager
            )
        }

        // -------------------- REGISTRAR RESERVA --------------------
        composable(
            route = Routes.RegistrarReserva.route,
            arguments = listOf(navArgument("garageId") { type = NavType.StringType })
        ) { backStackEntry ->

            val garageId = backStackEntry.arguments?.getString("garageId") ?: ""
            val currentUser by authViewModel.currentUser.collectAsState()

            val userId = currentUser?.id ?: ""

            RegistrarReservaScreen(
                viewModel = parkingViewModel,
                garageId = garageId,
                userId = userId
            )
        }



        // -------------------- LISTA DE RESERVAS --------------------
        composable(
            route = Routes.ListaReservas.route,
            arguments = listOf(navArgument("garageId") { type = NavType.StringType })
        ) { backStackEntry ->

            val garageId = backStackEntry.arguments?.getString("garageId") ?: ""

            ListaReservasScreen(
                viewModel = parkingViewModel,
                garageId = garageId
            )
        }


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
        composable(Routes.EmployeeSettings.route) {
            SettingsEmployeeScreen(
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
