package com.kotlin.u_park.presentation.screens.garage

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.kotlin.u_park.R
import com.kotlin.u_park.presentation.navigation.Routes
import com.kotlin.u_park.presentation.screens.employee.EmpleadosScreen
import com.kotlin.u_park.presentation.screens.employee.EmpleadosViewModel
import com.kotlin.u_park.presentation.screens.suscriptions.GarageSuscripcionesScreen
import com.kotlin.u_park.presentation.screens.suscriptions.SubscriptionViewModel

// 🎨 Color System
private val PrimaryRed = Color(0xFFE60023)
private val BackgroundColor = Color(0xFFFAFAFA)
private val SurfaceColor = Color(0xFFFFFFFF)
private val TextPrimary = Color(0xFF0D0D0D)
private val TextSecondary = Color(0xFF6E6E73)

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GarageManagementScreen(
    navController: NavController,
    garageId: String,
    garageName: String,
    empleadosViewModel: EmpleadosViewModel,
    subscriptionViewModel: SubscriptionViewModel
) {
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            garageName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = TextPrimary
                        )
                        Text(
                            if (selectedTab == 0) stringResource(R.string.empleados3) else stringResource(
                                R.string.suscripciones
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, null, tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceColor
                )
            )
        },
        bottomBar = {
            ModernBottomBarManagement(
                selectedIndex = selectedTab,
                onItemSelected = { selectedTab = it }
            )
        }
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

@Composable
private fun ModernBottomBarManagement(
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = SurfaceColor,
        shadowElevation = 12.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomBarItemManagement(
                icon = Icons.Outlined.Group,
                selectedIcon = Icons.Default.Group,
                label = stringResource(R.string.empleados4),
                isSelected = selectedIndex == 0,
                onClick = { onItemSelected(0) }
            )
            BottomBarItemManagement(
                icon = Icons.Outlined.CardMembership,
                selectedIcon = Icons.Default.CardMembership,
                label = stringResource(R.string.suscripciones),
                isSelected = selectedIndex == 1,
                onClick = { onItemSelected(1) }
            )
        }
    }
}

@Composable
private fun BottomBarItemManagement(
    icon: ImageVector,
    selectedIcon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        modifier = Modifier.size(80.dp, 56.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                if (isSelected) selectedIcon else icon,
                contentDescription = label,
                tint = if (isSelected) PrimaryRed else TextSecondary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) PrimaryRed else TextSecondary
            )
        }
    }
}