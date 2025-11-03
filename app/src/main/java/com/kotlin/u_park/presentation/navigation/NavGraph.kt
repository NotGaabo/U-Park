package com.kotlin.u_park.presentation.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.*
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
import com.kotlin.u_park.presentation.screens.home.DuenoGarageScreen
import com.kotlin.u_park.presentation.screens.home.HomeScreen
import com.kotlin.u_park.presentation.screens.profile.SettingsScreen
import com.kotlin.u_park.presentation.screens.splash.SplashScreen

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NavGraph(
    navController: NavHostController,
    sessionManager: SessionManager,
    authViewModel: AuthViewModel,
    startDestination: String = Routes.Splash.route
) {
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
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        // --- DUEÑO DE GARAGE ---
        composable(Routes.DuenoGarage.route) {
            val currentUser by authViewModel.currentUser.collectAsState()
            val userId = currentUser?.id ?: ""
            val garageRepository = remember { GarageRepositoryImpl(supabase) }
            val garageViewModel = remember { GarageViewModel(garageRepository) }

            DuenoGarageScreen(
                navController = navController,
                viewModel = garageViewModel,
                userId = userId
            )
        }

        // --- AGREGAR GARAGE ---
        composable(Routes.GarageAdd.route) {
            val currentUser by authViewModel.currentUser.collectAsState()
            currentUser?.let { user ->
                val garageRepository = remember { GarageRepositoryImpl(supabase) }
                val garageViewModel = remember { GarageViewModel(garageRepository) }

                GarageAddScreen(
                    userId = user.id,
                    viewModel = garageViewModel,
                    onDismiss = { navController.popBackStack() },
                    onSuccess = {
                        navController.navigate(Routes.DuenoGarage.route) {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    }
}
