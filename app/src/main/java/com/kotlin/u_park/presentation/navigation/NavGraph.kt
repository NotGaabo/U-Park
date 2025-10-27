package com.kotlin.u_park.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.kotlin.u_park.data.remote.SessionManager
import com.kotlin.u_park.domain.model.Garage
import com.kotlin.u_park.presentation.screens.detalles.DetallesScreen
import com.kotlin.u_park.presentation.screens.home.HomeScreen
import com.kotlin.u_park.presentation.screens.auth.RegisterScreen
import com.kotlin.u_park.presentation.screens.splash.SplashScreen
import com.kotlin.u_park.presentation.screens.auth.LoginScreen
import com.kotlin.u_park.data.remote.supabase
import com.kotlin.u_park.presentation.screens.auth.AuthViewModel
import com.kotlin.u_park.presentation.screens.home.DuenoGarageScreen
import com.kotlin.u_park.presentation.screens.profile.SettingsScreen
import com.kotlin.u_park.presentation.screens.profile.SettingsScreenDueno

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
            val currentUser by authViewModel.currentUser.collectAsState()
            SettingsScreen(
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

        // Pantalla de dueño de garage
        composable(Routes.DuenoGarage.route) {
            DuenoGarageScreen(navController = navController)
        }

//        // Pantalla de empleado
//        composable(Routes.EmployeeHome.route) {
//            EmployeeHomeScreen(navController = navController, authViewModel = authViewModel)
//        }
//
//        // Pantalla para agregar garaje
//        composable(Routes.AddGarage.route) {
//            AddGarageScreen(navController = navController)
//        }
//
//        // Pantalla de historial
//        composable(Routes.History.route) {
//            HistoryScreen(navController = navController)
//        }
    }
}

