package com.kotlin.u_park.presentation.screens.garage

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kotlin.u_park.domain.repository.GarageRepository
import com.kotlin.u_park.presentation.screens.parking.ParkingViewModel

// Colores del diseño
private val redPrimary = Color(0xFFE74C3C)
private val greenSuccess = Color(0xFF27AE60)
private val orangeWarning = Color(0xFFF39C12)
private val blueInfo = Color(0xFF3498DB)
private val grayLight = Color(0xFFF8F9FA)
private val grayMedium = Color(0xFFECF0F1)
private val darkText = Color(0xFF2C3E50)

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ListaReservasScreen(
    viewModel: ParkingViewModel,
    garageId: String,
    userId: String,
    garageRepository: GarageRepository
) {
    // 🔥 Ahora sí: reservas con usuario incluido
    val reservas by viewModel.reservasConUsuario.collectAsState()

    LaunchedEffect(garageId) {
        viewModel.loadReservasConUsuario(garageId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Reservas",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = darkText
                )
            )
        },
        containerColor = grayLight
    ) { padding ->
        if (reservas.isEmpty()) {
            EmptyStateReservas(Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(reservas) { reserva ->
                    ReservaCard(
                        vehicleId = reserva.vehicles?.plate ?: reserva.vehicle_id ?: "Sin placa",
                        usuario = reserva.vehicles?.users?.nombre ?: "Usuario desconocido",
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
private fun EmptyStateReservas(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.BookmarkBorder,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = grayMedium
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "No hay reservas",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = darkText.copy(alpha = 0.6f)
        )
        Text(
            "Las reservas aparecerán aquí",
            fontSize = 14.sp,
            color = darkText.copy(alpha = 0.4f)
        )
    }
}

@Composable
fun ReservaCard(
    vehicleId: String,
    usuario: String,
    estado: String,
    fecha: String,
    onActivar: () -> Unit,
    onCancelar: () -> Unit
)
 {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Header con estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.DirectionsCar,
                        contentDescription = null,
                        tint = redPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        vehicleId,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = darkText
                    )
                }

                EstadoChip(estado)
            }

            Spacer(Modifier.height(16.dp))

            // Información de fecha
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.AccessTime,
                    contentDescription = null,
                    tint = darkText.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    fecha,
                    fontSize = 14.sp,
                    color = darkText.copy(alpha = 0.7f)
                )
            }

            Spacer(Modifier.height(20.dp))

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
                        contentColor = redPrimary
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        width = 1.5.dp,
                        brush = androidx.compose.ui.graphics.SolidColor(redPrimary)
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
                        containerColor = greenSuccess
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
private fun EstadoChip(estado: String) {
    val (color, icon) = when (estado.lowercase()) {
        "activa", "activo" -> greenSuccess to Icons.Filled.CheckCircle
        "pendiente" -> orangeWarning to Icons.Filled.Schedule
        "cancelada", "cancelado" -> redPrimary to Icons.Filled.Cancel
        else -> blueInfo to Icons.Filled.Info
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
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
                estado.capitalize(),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}