package com.kotlin.u_park.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.kotlin.u_park.data.remote.SessionManager
import com.kotlin.u_park.data.remote.supabase
import com.kotlin.u_park.data.repository.AuthRepository
import com.kotlin.u_park.domain.model.Garage
import com.kotlin.u_park.ui.detalles.DetallesScreen
import com.kotlin.u_park.ui.screens.home.HomeScreen
import com.kotlin.u_park.ui.screens.profile.SettingsScreen
import com.kotlin.u_park.ui.screens.register.RegisterScreen
import com.kotlin.u_park.ui.screens.splash.SplashScreen
import com.kotlin.u_park.ui.screens.login.LoginScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    sessionManager: SessionManager,
    authRepository: AuthRepository,
    startDestination: String = "login"// pasa el repo desde la Activity o Hilt
) {
    val scope = rememberCoroutineScope() // para coroutines dentro de Composables

    NavHost(
        navController = navController,
        startDestination = Routes.Splash.route
    ) {
        composable(Routes.Splash.route) { SplashScreen( navController = navController,
            sessionManager = sessionManager,
            supabase = supabase) }

        composable(Routes.Register.route) {
            RegisterScreen(navController = navController, supabase = supabase)
        }

        composable(Routes.Home.route) { HomeScreen(navController) }

        composable(Routes.Detalles.route) {
            DetallesScreen(navController = navController, garage = Garage())
        }

        composable(Routes.Login.route) {
            LoginScreen(navController = navController, supabase = supabase)
        }

        // --- SETTINGS ---
        composable(Routes.Settings.route) {
            val currentUser by authRepository.currentUser.collectAsState(initial = null)

            currentUser?.let { user ->
                SettingsScreen(
                    navController = navController,
                    currentUser = user,
                    userRoles = user.roles,
                    allRoles = listOf("user", "employee"),
                    onSaveRoles = { /* TODO: implementar guardado de roles */ }
                )
            }
        }
    }
}
