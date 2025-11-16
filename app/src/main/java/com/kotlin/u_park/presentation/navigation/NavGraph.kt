package com.kotlin.u_park.presentation.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.kotlin.u_park.data.remote.SessionManager
import com.kotlin.u_park.data.remote.supabase
import com.kotlin.u_park.data.repository.GarageRepositoryImpl
import com.kotlin.u_park.domain.model.Garage
import com.kotlin.u_park.presentation.screens.auth.AuthViewModel
import com.kotlin.u_park.presentation.screens.auth.LoginScreen
import com.kotlin.u_park.presentation.screens.auth.RegisterScreen
import com.kotlin.u_park.presentation.screens.detalles.DetallesScreen
import com.kotlin.u_park.presentation.screens.garage.GarageAddScreen
import com.kotlin.u_park.presentation.screens.garage.GarageViewModel
import com.kotlin.u_park.presentation.screens.garage.GarageViewModelFactory
import com.kotlin.u_park.presentation.screens.home.DuenoGarageScreen
import com.kotlin.u_park.presentation.screens.home.HomeScreen
import com.kotlin.u_park.presentation.screens.profile.SettingsScreen
import com.kotlin.u_park.presentation.screens.profile.SettingsScreenDueno
import com.kotlin.u_park.presentation.screens.employee.EmpleadosScreen
import com.kotlin.u_park.presentation.screens.employee.AgregarEmpleadoScreen
import com.kotlin.u_park.presentation.screens.employee.EmpleadosViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kotlin.u_park.data.repository.EmpleadoGarageRepositoryImpl
import com.kotlin.u_park.domain.repository.GarageRepository
import com.kotlin.u_park.presentation.screens.employee.EmpleadosViewModelFactory
import com.kotlin.u_park.presentation.screens.employee.EmployeeHomeScreen
import com.kotlin.u_park.presentation.screens.splash.SplashScreen

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NavGraph(
    navController: NavHostController,
    sessionManager: SessionManager,
    authViewModel: AuthViewModel,
    startDestination: String = Routes.Splash.route
) {
    // ✅ Crear el repository una sola vez para toda la navegación
    val garageRepository = remember { GarageRepositoryImpl(supabase) }

    NavHost(navController = navController, startDestination = startDestination) {

        // --- SPLASH ---
        composable(Routes.Splash.route) {
            SplashScreen(
                navController = navController,
                sessionManager = sessionManager,
                supabase = supabase,
                authViewModel = authViewModel
            )
        }

        // --- AUTH ---
        composable(Routes.Register.route) {
            RegisterScreen(navController = navController, supabase = supabase)
        }

        composable(Routes.Login.route) {
            LoginScreen(navController = navController, supabase = supabase)
        }

        // --- HOME (Usuario normal) ---
        composable(Routes.Home.route) {
            HomeScreen(navController = navController, authViewModel = authViewModel)
        }

        // --- DETALLES ---
        composable(Routes.Detalles.route) {
            DetallesScreen(navController = navController, garage = Garage())
        }

        // --- CONFIGURACIÓN / PERFIL ---
        composable(Routes.Settings.route) {
            SettingsScreen(
                navController = navController,
                supabase = supabase,
                onSignOut = {
                    authViewModel.signOut()
                    navController.navigate(Routes.Login.route) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(
                route = Routes.AgregarEmpleado.route
                ) { backStackEntry ->

            // 👇 Recibir el garageId recibido por la navegación
            val garageId = backStackEntry.arguments?.getString("garageId") ?: ""

            val empleadosRepo = remember { EmpleadoGarageRepositoryImpl(supabase) }

            val viewModel: EmpleadosViewModel = viewModel(
                viewModelStoreOwner = backStackEntry,
                factory = EmpleadosViewModelFactory(empleadosRepo)
            )

            AgregarEmpleadoScreen(
                garageId = garageId,   // 👈 ya no se usa firstOrNull()
                viewModel = viewModel,
                onClose = { navController.popBackStack() }
            )
        }
        composable(
            route = Routes.Empleados.route
        ) { backStackEntry ->

            // 👇 Recibir el garageId desde la navegación
            val garageId = backStackEntry.arguments?.getString("garageId") ?: ""

            val empleadosRepo = remember { EmpleadoGarageRepositoryImpl(supabase) }

            val viewModel: EmpleadosViewModel = viewModel(
                viewModelStoreOwner = backStackEntry,
                factory = EmpleadosViewModelFactory(empleadosRepo)
            )

            // 👇 Cargar empleados SOLO del garage tocado
            LaunchedEffect(garageId) {
                if (garageId.isNotBlank()) {
                    android.util.Log.d("NAV_EMPLEADOS", "Cargando empleados del garage: $garageId")
                    viewModel.loadEmpleados(garageId)
                }
            }

            EmpleadosScreen(
                garageId = garageId,
                viewModel = viewModel,
                onAgregarEmpleado = {
                    navController.navigate(
                        Routes.AgregarEmpleado.createRoute(garageId)
                    )
                }
            )
        }




        // --- CONFIGURACIÓN / PERFIL ---
        composable(Routes.SettingsDueno.route) {
            SettingsScreenDueno(
                navController = navController,
                currentUser = authViewModel.currentUser.value,
                sessionManager = sessionManager,
                onSignOut = {
                    authViewModel.signOut()
                    navController.navigate(Routes.Login.route) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }


        // --- DUEÑO DE GARAGE ---
        composable(Routes.DuenoGarage.route) { backStackEntry ->
            val currentUser by authViewModel.currentUser.collectAsState()
            val userId = currentUser?.id ?: ""

            // ✅ Usar viewModel con el NavBackStackEntry para persistir el estado
            val garageViewModel: GarageViewModel = viewModel(
                viewModelStoreOwner = backStackEntry,
                factory = GarageViewModelFactory(garageRepository)
            )

            DuenoGarageScreen(
                navController = navController,
                viewModel = garageViewModel,
                userId = userId
            )
        }


        composable(Routes.EmployeeHome.route) { backStackEntry ->
            val currentUser by authViewModel.currentUser.collectAsState()
            val garageRepository = remember { EmpleadoGarageRepositoryImpl(supabase) }

            var garageId by remember { mutableStateOf("") }

            // 👉 Obtener garageId del usuario logueado
            LaunchedEffect(currentUser) {
                currentUser?.let { user ->
                    val id = garageRepository.getGarageByEmpleadoId(user.id!!)
                    garageId = id ?: ""
                }
            }

            // 👉 Solo mostrar la pantalla cuando garageId ya esté disponible
            if (garageId.isNotBlank()) {
                EmployeeHomeScreen(
                    navController = navController,
                    garageId = garageId,
                    viewModel = viewModel(
                        viewModelStoreOwner = backStackEntry,
                        factory = EmpleadosViewModelFactory(EmpleadoGarageRepositoryImpl(supabase))
                    )
                )
            }
        }




        // --- AGREGAR GARAGE ---
        composable(Routes.GarageAdd.route) { backStackEntry ->
            val currentUser by authViewModel.currentUser.collectAsState()
            currentUser?.let { user ->
                // ✅ Usar viewModel para persistir el estado
                val garageViewModel: GarageViewModel = viewModel(
                    viewModelStoreOwner = backStackEntry,
                    factory = GarageViewModelFactory(garageRepository)
                )

                GarageAddScreen(
                    userId = user.id,
                    viewModel = garageViewModel,
                    onDismiss = { navController.popBackStack() },
                    onSuccess = {
                        navController.navigate(Routes.DuenoGarage.route) {
                            popUpTo(Routes.DuenoGarage.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    }
}