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
import com.kotlin.u_park.data.repository.AuthViewModel
import com.kotlin.u_park.utils.LocationHelper
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@Composable
fun SplashScreen(
    navController: NavController,
    sessionManager: SessionManager,
    supabase: SupabaseClient,
    authViewModel: AuthViewModel // 🔥 agrega este parámetro
) {
    val context = LocalContext.current

    var locationGranted by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        locationGranted = isGranted
    }

    LaunchedEffect(Unit) {
        // Pide permiso de ubicación
        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    LaunchedEffect(locationGranted) {
        if (locationGranted) {
            val location = LocationHelper.getCurrentLocation(context)
            location?.let {
                authViewModel.updateLocation(it.first, it.second)
            }
        }

        delay(2000)
        val hasSession = withContext(Dispatchers.IO) {
            sessionManager.refreshSessionFromDataStore()
        }

        val user = if (hasSession) supabase.auth.currentUserOrNull() else null

        if (user != null) {
            val activeRole = withContext(Dispatchers.IO) {
                sessionManager.getActiveRole()
            }

            val destination = when (activeRole?.lowercase()) {
                "dueno-garage" -> "duenogarage"
                "employee" -> "employeeHome"
                "user" -> "home"
                else -> "home"
            }

            navController.navigate(destination) {
                popUpTo("splash") { inclusive = true }
            }
        } else {
            navController.navigate("login") {
                popUpTo("splash") { inclusive = true }
            }
        }
    }

    // -----------------------------
    // UI del Splash Screen
    // -----------------------------
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

                Spacer(modifier = Modifier.height(48.dp)) // espacio entre logo y carga

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
