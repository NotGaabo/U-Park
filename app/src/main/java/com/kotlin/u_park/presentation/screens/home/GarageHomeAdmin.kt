package com.kotlin.u_park.presentation.screens.home

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.kotlin.u_park.domain.model.Garage
import com.kotlin.u_park.presentation.screens.garage.GarageAddScreen
import com.kotlin.u_park.presentation.screens.garage.GarageViewModel

@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("MissingPermission")
@Composable
fun DuenoGarageScreen(
    navController: NavController,
    viewModel: GarageViewModel,
    userId: String
) {
    val redSoft = Color(0xFFE60023)
    val garages by viewModel.garages.collectAsState(initial = emptyList())

    var showAddGarage by remember { mutableStateOf(false) }

    // Mostrar BottomSheet automáticamente si no hay garajes
    LaunchedEffect(garages) {
        if (garages.isEmpty()) showAddGarage = true
    }

    // Mostrar pantalla para agregar garage
    if (showAddGarage) {
        GarageAddScreen(
            userId = userId,
            viewModel = viewModel,
            onDismiss = { showAddGarage = false },
            onSuccess = { showAddGarage = false }
        )
        return
    }

    // Mostrar dashboard solo si existen garajes
    Scaffold(
        bottomBar = {
            if (garages.isNotEmpty()) {
                NavigationBar(containerColor = Color.White) {
                    NavigationBarItem(
                        selected = false,
                        onClick = { navController.navigate("empleados") },
                        icon = { Icon(Icons.Default.Group, contentDescription = "Empleados") },
                        label = { Text("Empleados") }
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = { navController.navigate("tarifas") },
                        icon = { Icon(Icons.Default.AttachMoney, contentDescription = "Tarifas") },
                        label = { Text("Tarifas") }
                    )
                    NavigationBarItem(
                        selected = true,
                        onClick = { /* ya estamos aquí */ },
                        icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard", tint = redSoft) },
                        label = { Text("Dashboard", color = redSoft) }
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = { navController.navigate("reservas") },
                        icon = { Icon(Icons.Default.Book, contentDescription = "Reservas") },
                        label = { Text("Reservas") }
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = { navController.navigate("settings") },
                        icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                        label = { Text("Perfil") }
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Dashboard de Garage",
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Mostrar lista de garajes del dueño
            garages.forEach { garage: Garage ->
                GarageCard(garage = garage)
                GarageStatsCard()
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { showAddGarage = true },
                colors = ButtonDefaults.buttonColors(containerColor = redSoft),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Agregar otro Garage", color = Color.White)
            }
        }
    }
}

@Composable
fun GarageCard(garage: Garage) {
    val imagePainter = rememberAsyncImagePainter(garage.imageUrl ?: "")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { /* abrir detalles */ },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = imagePainter,
                contentDescription = "Imagen del garage",
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(garage.nombre, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(
                    "Capacidad: ${garage.capacidadTotal}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun GarageStatsCard() {
    val redSoft = Color(0xFFE60023)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8F8)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Estadísticas",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = redSoft
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Ocupación actual: 70%")
            Text("Reservas activas: 5")
            Text("Ganancias del día: RD$1200")
        }
    }
}
