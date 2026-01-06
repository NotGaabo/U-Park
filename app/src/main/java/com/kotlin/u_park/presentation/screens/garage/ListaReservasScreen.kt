package com.kotlin.u_park.presentation.screens.garage

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.kotlin.u_park.domain.repository.GarageRepository
import com.kotlin.u_park.presentation.navigation.Routes
import com.kotlin.u_park.presentation.screens.parking.ParkingViewModel
import com.kotlin.u_park.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ListaReservasScreen(
    viewModel: ParkingViewModel,
    garageId: String,
    userId: String,
    garageRepository: GarageRepository,
    navController: NavController
) {
    val reservas by viewModel.reservasConUsuario.collectAsState()

    LaunchedEffect(garageId) {
        viewModel.loadReservasConUsuario(garageId)
    }

    Scaffold(
        containerColor = BackgroundColor,
        bottomBar = {
            ModernBottomBarAdmin(
                selectedIndex = 2,
                onItemSelected = { index ->
                    when (index) {
                        0 -> navController.navigate(Routes.Rates.route)
                        1 -> navController.navigate(Routes.DuenoGarage.route)
                        3 -> navController.navigate(Routes.SettingsDueno.route)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header Section
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Reservas",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        "${reservas.size} ${if (reservas.size == 1) "reserva activa" else "reservas activas"}",
                        fontSize = 14.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Empty State or Reservas List
            if (reservas.isEmpty()) {
                item {
                    EmptyStateReservas()
                }
            } else {
                items(reservas) { reserva ->
                    ModernReservaCard(
                        vehicleId = reserva.vehicles?.plate ?: reserva.vehicle_id ?: "Sin placa",
                        usuario = reserva.users?.nombre ?: "Usuario desconocido",
                        estado = reserva.estado ?: "",
                        fecha = reserva.hora_reserva ?: "",
                        onActivar = { reserva.id?.let { viewModel.activarReserva(it) } },
                        onCancelar = { reserva.id?.let { viewModel.cancelarReserva(it) } }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyStateReservas() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(BackgroundColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Outlined.BookmarkBorder,
                contentDescription = null,
                modifier = Modifier.size(50.dp),
                tint = TextSecondary.copy(alpha = 0.5f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "Sin reservas activas",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Las reservas aparecerán aquí\ncuando los usuarios las realicen",
            fontSize = 15.sp,
            color = TextSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            lineHeight = 22.sp
        )
    }
}

@Composable
fun ModernReservaCard(
    vehicleId: String,
    usuario: String,
    estado: String,
    fecha: String,
    onActivar: () -> Unit,
    onCancelar: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = SurfaceColor,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header con placa y estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(PrimaryRed.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DirectionsCar,
                            contentDescription = null,
                            tint = PrimaryRed,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Text(
                            vehicleId,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            "Vehículo",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }

                ModernEstadoChip(estado)
            }

            Divider(color = BorderColor, thickness = 1.dp)

            // Información del usuario
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
                Column {
                    Text(
                        "Cliente",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Text(
                        usuario,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                }
            }

            // Información de fecha
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.AccessTime,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
                Column {
                    Text(
                        "Fecha de reserva",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Text(
                        fecha,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                }
            }

            // Botones de acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onCancelar,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = PrimaryRed
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        width = 1.5.dp,
                        brush = androidx.compose.ui.graphics.SolidColor(PrimaryRed)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Cancelar", fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = onActivar,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SuccessGreen
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 2.dp
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Activar", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun ModernEstadoChip(estado: String) {
    val (color, icon) = when (estado.lowercase()) {
        "activa", "activo" -> SuccessGreen to Icons.Filled.CheckCircle
        "pendiente" -> WarningOrange to Icons.Filled.Schedule
        "cancelada", "cancelado" -> PrimaryRed to Icons.Filled.Cancel
        else -> InfoBlue to Icons.Filled.Info
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
            Text(
                estado.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase() else it.toString()
                },
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun ModernBottomBarAdmin(
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
            BottomBarItemAdmin(
                icon = Icons.Outlined.AttachMoney,
                selectedIcon = Icons.Default.AttachMoney,
                label = "Tarifas",
                isSelected = selectedIndex == 0,
                onClick = { onItemSelected(0) }
            )
            BottomBarItemAdmin(
                icon = Icons.Outlined.Dashboard,
                selectedIcon = Icons.Default.Dashboard,
                label = "Dashboard",
                isSelected = selectedIndex == 1,
                onClick = { onItemSelected(1) }
            )
            BottomBarItemAdmin(
                icon = Icons.Outlined.BookmarkBorder,
                selectedIcon = Icons.Default.Bookmark,
                label = "Reservas",
                isSelected = selectedIndex == 2,
                onClick = { onItemSelected(2) }
            )
            BottomBarItemAdmin(
                icon = Icons.Outlined.Person,
                selectedIcon = Icons.Default.Person,
                label = "Perfil",
                isSelected = selectedIndex == 3,
                onClick = { onItemSelected(3) }
            )
        }
    }
}

@Composable
fun BottomBarItemAdmin(
    icon: ImageVector,
    selectedIcon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val animatedColor by animateColorAsState(
        targetValue = if (isSelected) PrimaryRed else TextSecondary,
        animationSpec = tween(300)
    )

    Surface(
        onClick = onClick,
        color = Color.Transparent,
        modifier = Modifier.size(64.dp, 56.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                if (isSelected) selectedIcon else icon,
                contentDescription = label,
                tint = animatedColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = animatedColor
            )
        }
    }
}