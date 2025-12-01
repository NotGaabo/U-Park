package com.kotlin.u_park.presentation.screens.employee

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.kotlin.u_park.presentation.navigation.Routes
import com.kotlin.u_park.presentation.screens.parking.ParkingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehiculosDentroScreen(
    navController: NavController,
    parkingViewModel: ParkingViewModel,
    garageId: String
) {
    val vehiculosDentro by parkingViewModel.vehiculosDentro.collectAsState()
    val actividad by parkingViewModel.actividad.collectAsState()

    /* ================================================
     * 🔥 1. Recargar datos cuando la pantalla vuelva
     * ================================================ */
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                println("🔄 ON_RESUME → Recargando vehículos y actividad...")
                parkingViewModel.actualizarVehiculosDentro()
                parkingViewModel.loadActividad(garageId)
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Vehículos Dentro",
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )
                        Text(
                            "${vehiculosDentro.size} vehículo${if (vehiculosDentro.size != 1) "s" else ""} estacionado${if (vehiculosDentro.size != 1) "s" else ""}",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFE60023),
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { padding ->

        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {

            if (vehiculosDentro.isEmpty()) {
                EmptySalidaState()
            } else {

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    items(vehiculosDentro) { plate ->

                        /* ============================
                         * DEBUG LOG
                         * ============================ */
                        LaunchedEffect(plate, actividad) {
                            println("========= DEBUG SALIDA =========")
                            println("Vehículo dentro: $plate")
                            println("Lista actividad:")
                            actividad.forEach {
                                println(
                                    "  -> act.id=${it.id}, tipo=${it.tipo}, placa=${it.vehicles?.plate}, salida=${it.hora_salida}"
                                )
                            }
                        }

                        /* ============================
                         * OBTENER PARKING ACTIVO
                         * ============================ */
                        val parkingId = actividad.firstOrNull { act ->
                            act.vehicles?.plate == plate &&
                                    (act.tipo == "entrada" || act.tipo == "reserva") &&
                                    act.hora_salida == null
                        }?.id ?: ""

                        if (parkingId.isBlank()) {
                            println("⚠️ No se encontró parking ACTIVO para placa: $plate")
                        } else {
                            println("✔ Parking activo encontrado para $plate: $parkingId")
                        }

                        VehiculoCardSalida(
                            plate = plate,
                            onClick = {
                                if (parkingId.isNotBlank()) {
                                    navController.navigate(
                                        Routes.RegistrarSalida.createRoute(parkingId)
                                    )
                                } else {
                                    println("❌ NO SE NAVEGA: parkingId vacío")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptySalidaState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Icon(
            Icons.Outlined.DirectionsCar,
            contentDescription = null,
            tint = Color(0xFFE60023).copy(alpha = 0.6f),
            modifier = Modifier.size(80.dp)
        )

        Spacer(Modifier.height(24.dp))

        Text(
            "No hay vehículos dentro",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF333333)
        )

        Spacer(Modifier.height(8.dp))

        Text(
            "En este momento no hay vehículos estacionados.",
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun VehiculoCardSalida(
    plate: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                Icons.Default.DirectionsCar,
                contentDescription = null,
                tint = Color(0xFFE60023),
                modifier = Modifier.size(32.dp)
            )

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Placa",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    plate,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color(0xFF333333)
                )
            }

            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE60023)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Salida")
            }
        }
    }
}
