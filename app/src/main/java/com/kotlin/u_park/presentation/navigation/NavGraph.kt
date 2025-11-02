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
import com.kotlin.u_park.presentation.screens.home.DuenoGarageScreen
import com.kotlin.u_park.presentation.screens.home.HomeScreen
import com.kotlin.u_park.presentation.screens.profile.SettingsScreen
import com.kotlin.u_park.presentation.screens.profile.SettingsScreenDueno
import com.kotlin.u_park.presentation.screens.splash.SplashScreen
import com.kotlin.u_park.presentation.screens.garage.GarageViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NavGraph(
    navController: NavHostController,
    sessionManager: SessionManager,
    authViewModel: AuthViewModel,
    startDestination: String = Routes.Splash.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Routes.Splash.route) {
            SplashScreen(
                navController = navController,
                sessionManager = sessionManager,
                supabase = supabase,
                authViewModel = authViewModel
            )
        }

        composable(Routes.Register.route) {
            RegisterScreen(navController = navController, supabase = supabase)
        }

        composable(Routes.Login.route) {
            LoginScreen(navController = navController, supabase = supabase)
        }

        composable(Routes.Home.route) {
            HomeScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        composable(Routes.Detalles.route) {
            DetallesScreen(navController = navController, garage = Garage())
        }

        composable(Routes.Settings.route) {
            SettingsScreen(
                navController = navController,
                supabase = supabase,
                onSignOut = {
                    navController.navigate("login") {
                        popUpTo("settings") { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.SettingsDueno.route) {
            val currentUser by authViewModel.currentUser.collectAsState()
            SettingsScreenDueno(
                navController = navController,
                currentUser = currentUser,
                sessionManager = sessionManager,
                onSignOut = {
                    authViewModel.signOut()
                    navController.navigate(Routes.Login.route) {
                        popUpTo(Routes.Home.route) { inclusive = true }
                    }
                }
            )
        }

        // 🟢 Pantalla de dueño de garage
        composable(Routes.DuenoGarage.route) {
            val currentUser by authViewModel.currentUser.collectAsState()
            val userId = currentUser?.id ?: ""

            // Crear el repositorio y el ViewModel manualmente (sin Hilt)
            val garageRepository = remember { GarageRepositoryImpl(supabase) }
            val garageViewModel = remember { GarageViewModel(garageRepository) }

            DuenoGarageScreen(
                onSave = { navController.popBackStack() },
                viewModel = garageViewModel,
                userId = userId
            )
        }


        composable(Routes.GarageAdd.route) {
            val currentUser by authViewModel.currentUser.collectAsState()

            currentUser?.let { user ->
                val garageRepository = remember { com.kotlin.u_park.data.repository.GarageRepositoryImpl(supabase) }
                val garageViewModel = remember { com.kotlin.u_park.presentation.screens.garage.GarageViewModel(garageRepository) }

                com.kotlin.u_park.presentation.screens.garage.GarageAddScreen(
                    navController = navController,
                    userId = user.id,
                    viewModel = garageViewModel
                )
            }
        }
    }
}
