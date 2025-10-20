package com.kotlin.u_park.ui.navigation


import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.kotlin.u_park.data.remote.supabase
import com.kotlin.u_park.ui.screens.splash.SplashScreen
import com.kotlin.u_park.ui.screens.register.RegisterScreen
import com.kotlin.u_park.ui.screens.home.HomeScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Routes.Splash.route
    ) {
        composable(Routes.Splash.route) {
            SplashScreen(navController)
        }
//        composable(Routes.Login.route) {
//            LoginScreen(navController)
//        }
        composable(Routes.Register.route) {
            RegisterScreen(navController = navController,
                supabase = supabase)
        }
        composable(Routes.Home.route) {
            HomeScreen(navController)
        }
//        composable(Routes.Profile.route) {
//            ProfileScreen(navController)
//        }
        }
}
