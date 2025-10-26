package com.kotlin.u_park.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.kotlin.u_park.data.remote.SessionManager
import com.kotlin.u_park.data.repository.AuthViewModel
import com.kotlin.u_park.domain.model.Garage
import com.kotlin.u_park.ui.detalles.DetallesScreen
import com.kotlin.u_park.ui.screens.home.HomeScreen
import com.kotlin.u_park.ui.screens.profile.SettingsScreen
import com.kotlin.u_park.ui.screens.register.RegisterScreen
import com.kotlin.u_park.ui.screens.splash.SplashScreen
import com.kotlin.u_park.ui.screens.login.LoginScreen
import com.kotlin.u_park.data.remote.supabase

@Composable
fun NavGraph(
    navController: NavHostController,
    sessionManager: SessionManager,
    authViewModel: AuthViewModel,
    startDestination: String = "login"
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Routes.Splash.route) {
            SplashScreen(
                navController = navController,
                sessionManager = sessionManager,
                supabase = supabase
            )
        }

        composable(Routes.Register.route) {
            RegisterScreen(navController = navController, supabase = supabase)
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

        composable(Routes.Login.route) {
            LoginScreen(navController = navController, supabase = supabase, authViewModel = authViewModel)
        }

        composable(Routes.Settings.route) {
            val currentUser by authViewModel.currentUser.collectAsState()
            SettingsScreen(navController = navController, currentUser = currentUser,onSignOut = {
                authViewModel.signOut()
                navController.navigate("login") {
                    popUpTo("home") { inclusive = true }
                }
            })
        }
    }
}
