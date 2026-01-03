package com.kotlin.u_park.presentation.screens.garage

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.kotlin.u_park.presentation.navigation.Routes
import com.kotlin.u_park.presentation.screens.employee.EmpleadosScreen
import com.kotlin.u_park.presentation.screens.employee.EmpleadosViewModel
import com.kotlin.u_park.presentation.screens.suscriptions.GarageSuscripcionesScreen
import com.kotlin.u_park.presentation.screens.suscriptions.SubscriptionViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun GarageManagementScreen(
    navController: NavController,
    garageId: String,
    empleadosViewModel: EmpleadosViewModel,
    subscriptionViewModel: SubscriptionViewModel
) {
    var selectedTab by remember { mutableStateOf(0) }
    val redPrimary = Color(0xFFE60023)

    Scaffold(
        containerColor = Color(0xFFF5F7FA),
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        Icon(
                            Icons.Default.Group,
                            contentDescription = null,
                            tint = if (selectedTab == 0) redPrimary else Color.Gray
                        )
                    },
                    label = {
                        Text(
                            "Empleados",
                            fontSize = 12.sp,
                            color = if (selectedTab == 0) redPrimary else Color.Gray
                        )
                    }
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        Icon(
                            Icons.Default.CardMembership,
                            contentDescription = null,
                            tint = if (selectedTab == 1) redPrimary else Color.Gray
                        )
                    },
                    label = {
                        Text(
                            "Suscripciones",
                            fontSize = 12.sp,
                            color = if (selectedTab == 1) redPrimary else Color.Gray
                        )
                    }
                )
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                0 -> EmpleadosScreen(
                    garageId = garageId,
                    viewModel = empleadosViewModel,
                    onAgregarEmpleado = {
                        navController.navigate(Routes.AgregarEmpleado.createRoute(garageId))
                    }
                )
                1 -> GarageSuscripcionesScreen(
                    garageId = garageId,
                    viewModel = subscriptionViewModel
                )
            }
        }
    }
}