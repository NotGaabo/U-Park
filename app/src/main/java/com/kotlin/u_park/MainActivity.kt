package com.kotlin.u_park

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.runtime.*
import androidx.navigation.compose.rememberNavController
import com.kotlin.u_park.data.remote.SessionManager
import com.kotlin.u_park.data.remote.supabase
import com.kotlin.u_park.data.repository.AuthRepositoryImpl
import com.kotlin.u_park.presentation.navigation.NavGraph
import com.kotlin.u_park.presentation.navigation.Routes
import com.kotlin.u_park.presentation.screens.auth.AuthViewModel
import com.kotlin.u_park.ui.theme.UParkTheme


class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val sessionManager = SessionManager.getInstance(this, supabase)
        val authRepository = AuthRepositoryImpl(supabase)
        val authViewModel = AuthViewModel(authRepository, sessionManager)

        setContent {
            UParkTheme {
                // startDestination siempre splash
                App(authViewModel, sessionManager, startDestination = Routes.Splash.route)
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun App(
    authViewModel: AuthViewModel,
    sessionManager: SessionManager,
    startDestination: String
) {
    val navController = rememberNavController()
    NavGraph(
        navController = navController,
        authViewModel = authViewModel,
        sessionManager = sessionManager,
        startDestination = startDestination
    )
}
