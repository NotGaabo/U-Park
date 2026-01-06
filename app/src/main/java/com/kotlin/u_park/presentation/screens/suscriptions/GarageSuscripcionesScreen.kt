package com.kotlin.u_park.presentation.screens.suscriptions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kotlin.u_park.domain.model.SubscriptionRequestWithDetails
import com.kotlin.u_park.ui.theme.*

@Composable
fun GarageSuscripcionesScreen(
    garageId: String,
    viewModel: SubscriptionViewModel
) {
    val solicitudes by viewModel.solicitudes.collectAsState()
    val loading = viewModel.loading

    LaunchedEffect(garageId) {
        viewModel.loadSolicitudesByGarage(garageId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
            .padding(20.dp)
    ) {
        Text(
            "Solicitudes de Suscripción",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2D3436)
        )

        Spacer(Modifier.height(8.dp))

        Text(
            "Gestiona las solicitudes de tus clientes",
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(Modifier.height(20.dp))

        if (loading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFFE60023))
            }
        } else if (solicitudes.isEmpty()) {
            EmptySolicitudesView()
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(solicitudes) { solicitud ->
                    SolicitudCard(
                        solicitud = solicitud,
                        onAprobar = {
                            viewModel.aprobarSolicitud(solicitud.id, garageId)
                        },
                        onRechazar = {
                            viewModel.rechazarSolicitud(solicitud.id, garageId)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SolicitudCard(
    solicitud: SubscriptionRequestWithDetails,
    onAprobar: () -> Unit,
    onRechazar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Usuario
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        solicitud.user?.nombre ?: "Usuario",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        solicitud.user?.telefono ?: "-",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }

                // Badge de estado
                Surface(
                    color = when (solicitud.status) {
                        "pendiente" -> Color(0xFFFFA500).copy(alpha = 0.1f)
                        "aprobada" -> Color(0xFF00B894).copy(alpha = 0.1f)
                        else -> Color.Red.copy(alpha = 0.1f)
                    },
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        solicitud.status.uppercase(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (solicitud.status) {
                            "pendiente" -> Color(0xFFFFA500)
                            "aprobada" -> Color(0xFF00B894)
                            else -> Color.Red
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Divider()

            Spacer(Modifier.height(12.dp))

            // Plan
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.CardMembership,
                    contentDescription = null,
                    tint = Color(0xFFE60023),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(
                        solicitud.plan?.name ?: "Plan",
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "RD$ ${solicitud.plan?.price} • ${solicitud.plan?.max_vehicles} vehículos",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }
            }

            // Botones solo si está pendiente
            if (solicitud.status == "pendiente") {
                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onRechazar,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.Red
                        )
                    ) {
                        Icon(Icons.Default.Close, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Rechazar")
                    }

                    Button(
                        onClick = onAprobar,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00B894)
                        )
                    ) {
                        Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Aprobar")
                    }
                }
            }
        }
    }
}

@Composable
fun EmptySolicitudesView() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Inbox,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = Color.Gray.copy(alpha = 0.3f)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "No hay solicitudes",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Gray
            )
            Text(
                "Las solicitudes aparecerán aquí",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}