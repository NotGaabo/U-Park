package com.kotlin.u_park.presentation.screens.profile

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import io.github.jan.supabase.SupabaseClient

@Composable
fun SettingsEmployeeScreen(
    garageId: String,
    navController: NavController,
    supabase: SupabaseClient,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current.applicationContext as Application
    val viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(context, supabase)
    )

    SettingsScreenContent(
        navController = navController,
        viewModel = viewModel,
        onSignOut = onSignOut,
        isEmployeeView = true,
        garageId = garageId
    )
}