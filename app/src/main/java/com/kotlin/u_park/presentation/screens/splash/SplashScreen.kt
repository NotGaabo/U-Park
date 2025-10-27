package com.kotlin.u_park.presentation.screens.splash

import android.Manifest
import android.content.pm.PackageManager
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
import com.kotlin.u_park.presentation.screens.auth.AuthViewModel
import com.kotlin.u_park.presentation.utils.LocationHelper
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
    authViewModel: AuthViewModel
) {
    val context = LocalContext.current

    var locationGranted by remember { mutableStateOf(false) }
    var askedPermission by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        locationGranted = granted
    }

    // Pedir permiso al inicio
    LaunchedEffect(Unit) {
        val permission = Manifest.permission.ACCESS_FINE_LOCATION
        val granted = ContextCompat.checkSelfPermission(context, permission) ==
                PackageManager.PERMISSION_GRANTED
        if (!granted && !askedPermission) {
            askedPermission = true
            permissionLauncher.launch(permission)
        } else {
            locationGranted = granted
        }
    }

    // Obtener ubicación cuando se concede
    LaunchedEffect(locationGranted) {
        if (locationGranted) {
            val loc = LocationHelper.getCurrentLocation(context)
            if (loc != null) {
                authViewModel.updateLocation(loc.first, loc.second)
            }
        }
    }

    // UI Splash
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
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

    // Navegación después del splash
    LaunchedEffect(locationGranted) {
        delay(2000)
        val hasSession = withContext(Dispatchers.IO) { sessionManager.refreshSessionFromDataStore() }
        val user = if (hasSession) supabase.auth.currentUserOrNull() else null

        val destination = when (user?.let {
            withContext(Dispatchers.IO) { sessionManager.getActiveRole() }?.lowercase()
        }) {
            "dueno-garage" -> "duenogarage"
            "employee" -> "employeeHome"
            "user" -> "home"
            else -> "home"
        }

        navController.navigate(if (user != null) destination else "login") {
            popUpTo("splash") { inclusive = true }
        }
    }
}
