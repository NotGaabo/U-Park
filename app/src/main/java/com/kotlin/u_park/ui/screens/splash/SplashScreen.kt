package com.kotlin.u_park.ui.screens.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.kotlin.u_park.R
import com.kotlin.u_park.data.remote.SessionManager
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@Composable
fun SplashScreen(
    navController: NavController,
    sessionManager: SessionManager,
    supabase: SupabaseClient
) {
    // Decide a dónde ir según la sesión
    LaunchedEffect(Unit) {
        delay(2000)

        val hasSession = withContext(Dispatchers.IO) {
            sessionManager.refreshSessionFromDataStore()
        }

        val user = if (hasSession) supabase.auth.currentUserOrNull() else null
        val destination = if (user != null) "home" else "login"

        navController.navigate(destination) {
            popUpTo("splash") { inclusive = true }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo de la app
                Image(
                    painter = painterResource(id = R.drawable.up),
                    contentDescription = "App Logo",
                    modifier = Modifier.size(120.dp)
                )

                Spacer(modifier = Modifier.height(48.dp)) // Separación entre logo y loading

                // Círculo de carga
                CircularProgressIndicator(
                    color = Color(0xFFE60023),
                    strokeWidth = 4.dp,
                    modifier = Modifier.size(50.dp)
                )
            }
        }
    }
}
