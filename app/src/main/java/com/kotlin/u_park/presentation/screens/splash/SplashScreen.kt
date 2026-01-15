package com.kotlin.u_park.presentation.screens.splash

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.kotlin.u_park.R
import com.kotlin.u_park.data.remote.SessionManager
import com.kotlin.u_park.presentation.navigation.Routes
import com.kotlin.u_park.presentation.screens.auth.AuthViewModel
import com.kotlin.u_park.presentation.utils.LocationHelper
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext


@Composable
fun SplashScreen(
    navController: NavController,
    sessionManager: SessionManager,
    supabase: SupabaseClient,
    authViewModel: AuthViewModel
) {
    val context = LocalContext.current

    var locationGranted by remember { mutableStateOf(false) }
    var askedPermission by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> locationGranted = granted }

    // 📍 Solicitud de permisos
    LaunchedEffect(Unit) {
        val permission = Manifest.permission.ACCESS_FINE_LOCATION
        val granted = ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED

        if (!granted && !askedPermission) {
            askedPermission = true
            permissionLauncher.launch(permission)
        } else {
            locationGranted = granted
        }
    }

    // 📍 Actualizar ubicación
    LaunchedEffect(locationGranted) {
        if (locationGranted) {
            val loc = LocationHelper.getCurrentLocation(context)
            if (loc != null) {
                authViewModel.updateLocation(loc.first, loc.second)
            }
        }
    }

    // 🎨 UI Splash
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(id = R.drawable.up),
                    contentDescription = "App Logo",
                    modifier = Modifier.size(120.dp)
                )
                Spacer(modifier = Modifier.height(48.dp))
                CircularProgressIndicator(
                    color = Color(0xFFE60023),
                    strokeWidth = 4.dp,
                    modifier = Modifier.size(50.dp)
                )
            }
        }
    }

    // 🚀 Lógica de sesión y navegación
    LaunchedEffect(Unit) {
        delay(2000)

        val hasSession = withContext(Dispatchers.IO) {
            sessionManager.refreshSessionFromDataStore()
        }

        if (!hasSession) {
            navController.navigate(Routes.Login.route) {
                popUpTo(Routes.Splash.route) { inclusive = true }
            }
            return@LaunchedEffect
        }

        val user = withContext(Dispatchers.IO) {
            sessionManager.getUser()
        }

        // 🔑 Rol: primero DataStore, luego fallback al usuario
        val activeRole = withContext(Dispatchers.IO) {
            sessionManager.getActiveRole()
                ?: user?.roles?.firstOrNull()
        }

        // Si viene del usuario, lo guardamos
        activeRole?.let {
            sessionManager.saveActiveRole(it)
        }

        Log.e("SPLASH_DEBUG", "UserId=${user?.id}")
        Log.e("SPLASH_DEBUG", "ActiveRole='$activeRole'")

        val destination = when (activeRole?.lowercase()) {
            "dueno-garage" -> Routes.DuenoGarage.route
            "employee" -> Routes.EmployeeHome.route
            "user" -> Routes.Home.route
            else -> Routes.Login.route
        }

        navController.navigate(destination) {
            popUpTo(Routes.Splash.route) { inclusive = true }
        }
    }
}
