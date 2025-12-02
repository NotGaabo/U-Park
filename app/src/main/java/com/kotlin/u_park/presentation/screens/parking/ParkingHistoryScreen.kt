package com.kotlin.u_park.presentation.screens.parking

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
import androidx.navigation.NavController
import com.kotlin.u_park.domain.model.HistorialParking
import com.kotlin.u_park.presentation.navigation.Routes

private val RedSoft = Color(0xFFE60023)
private val BackgroundColor = Color(0xFFF5F5F5)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParkingHistoryScreen(
    navController: NavController,
    viewModel: ParkingViewModel,
    userId: String
) {

    val historial by viewModel.historial.collectAsState()
    val activo by viewModel.parkingActivo.collectAsState()
    val loading by viewModel.isLoading.collectAsState()

    // cargar historial al entrar a la pantalla
    LaunchedEffect(Unit) {
        viewModel.cargarHistorial(userId)
    }

    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Mis Parkings",
                        fontWeight = FontWeight.Bold,
                        color = RedSoft
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundColor
                )
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White) {

                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(Routes.Home.route) },
                    icon = { Icon(Icons.Default.Home, null) },
                    label = { Text("Home") }
                )

                NavigationBarItem(
                    selected = false,
                    onClick = { /* Opción "Agregar" */ },
                    icon = { Icon(Icons.Default.Add, null) },
                    label = { Text("Agregar") }
                )

                NavigationBarItem(
                    selected = true,
                    onClick = {},
                    icon = { Icon(Icons.Default.History, null, tint = RedSoft) },
                    label = { Text("Historial", color = RedSoft) }
                )

                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(Routes.Settings.route) },
                    icon = { Icon(Icons.Default.Person, null) },
                    label = { Text("Perfil") }
                )
            }
        }
    ) { padding ->

        if (loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = RedSoft)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundColor)
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
        ) {

            // ---------------------
            // 🚗 PARKING ACTIVO
            // ---------------------
            activo?.let { item ->
                item {
                    Column {
                        Text(
                            "🚗 Activo ahora",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.DarkGray
                        )
                        Spacer(Modifier.height(12.dp))
                        ParkingHistorialCard(item, isActive = true)
                    }
                }
            }

            // ---------------------
            // 📅 HISTORIAL
            // ---------------------
            item {
                Text(
                    "📅 Historial",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.DarkGray
                )
            }

            if (historial.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No hay historial de parkings",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                items(historial) { h ->
                    ParkingHistorialCard(h, isActive = false)
                }
            }
        }
    }
}

@Composable
fun ParkingHistorialCard(h: HistorialParking, isActive: Boolean = false) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) Color(0xFFFFF3E0) else Color.White
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {

        Column(modifier = Modifier.padding(16.dp)) {

            // Header con estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    h.garage_nombre ?: "Garaje",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF212121)
                )

                val estadoColor = when (h.estado) {
                    "activa" -> Color(0xFF4CAF50)
                    "completada" -> Color(0xFF2196F3)
                    else -> Color(0xFFFF9800)
                }

                Box(
                    modifier = Modifier
                        .background(
                            estadoColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        h.estado.uppercase(),
                        color = estadoColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            // Dirección
            Text(
                h.garage_direccion ?: "",
                color = Color.Gray,
                fontSize = 13.sp
            )

            Spacer(Modifier.height(12.dp))

            // Información del vehículo
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.DirectionsCar,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    "${h.plate ?: "—"} • ${h.model ?: ""}",
                    fontSize = 14.sp,
                    color = Color(0xFF424242)
                )
            }

            Spacer(Modifier.height(8.dp))

            // Horarios
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "Entrada",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        h.hora_entrada ?: "—",
                        fontSize = 14.sp,
                        color = Color(0xFF212121),
                        fontWeight = FontWeight.Medium
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "Salida",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        h.hora_salida ?: "—",
                        fontSize = 14.sp,
                        color = Color(0xFF212121),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}