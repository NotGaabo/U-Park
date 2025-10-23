package com.kotlin.u_park

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.navigation.compose.rememberNavController
import com.kotlin.u_park.data.remote.SessionManager
import com.kotlin.u_park.data.remote.supabase
import com.kotlin.u_park.data.repository.AuthRepository
import com.kotlin.u_park.data.repository.AuthViewModel
import com.kotlin.u_park.ui.navigation.NavGraph
import com.kotlin.u_park.ui.theme.UParkTheme
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val sessionManager = SessionManager.getInstance(this, supabase)
        val authRepository = AuthRepository(supabase)
        val authViewModel = AuthViewModel(authRepository, sessionManager)

        setContent {
            UParkTheme {
                var startDestination by remember { mutableStateOf<String?>(null) }

                // Restaurar sesión
                LaunchedEffect(Unit) {
                    withContext(Dispatchers.IO) {
                        sessionManager.restoreSession()
                    }
                    val user = supabase.auth.currentUserOrNull()
                    startDestination = if (user != null) "home" else "login"
                }

                startDestination?.let { destination ->
                    App(authViewModel, sessionManager, destination)
                }
            }
        }
    }
}

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
