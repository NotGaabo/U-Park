package com.kotlin.u_park.presentation.screens.profile

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.kotlin.u_park.data.remote.SessionManager
import com.kotlin.u_park.data.remote.supabase
import com.kotlin.u_park.domain.model.User
import io.github.jan.supabase.SupabaseClient

@Composable
fun SettingsScreenDueno(
    navController: NavController,
    currentUser: User?,
    sessionManager: SessionManager,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current.applicationContext as Application

    // Crear el ViewModel usando el mismo que usa SettingsScreen
    val viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(context, supabase)
    )

    // Reutilizar la misma UI pero con la flag isDuenoView = true
    SettingsScreenContent(
        navController = navController,
        viewModel = viewModel,
        onSignOut = onSignOut,
        isDuenoView = true
    )
}