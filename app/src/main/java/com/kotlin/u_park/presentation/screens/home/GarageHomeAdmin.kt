package com.kotlin.u_park.presentation.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

private val background = Color(0xFFF9F9F9)
private val textGray = Color(0xFF5C5C5C)
private val redSoft = Color(0xFFE60023)
private val redLight = Color(0xFFFFEBEE)
private val cardGradient = Brush.verticalGradient(
    colors = listOf(Color.White, Color(0xFFFDFDFD))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DuenoGarageScreen(navController: NavController) {

    Scaffold(
        bottomBar = {
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
                    onClick = { },
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
                    onClick = { navController.navigate("settingsdueno") },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                    label = { Text("Perfil") }
                )
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(background)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {

            Text(
                text = "Panel de Control",
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Resumen general del garaje",
                color = textGray,
                fontSize = 15.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- Gráficos principales ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DashboardCard(
                    title = "Ingresos",
                    subtitle = "$12,430",
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.BarChart, contentDescription = null, tint = redSoft, modifier = Modifier.size(30.dp))
                }
                DashboardCard(
                    title = "Ocupación",
                    subtitle = "78%",
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PieChart, contentDescription = null, tint = redSoft, modifier = Modifier.size(30.dp))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- Tarifa ---
            LargeCard(title = "Tarifas actuales", value = "₱50 / hora")

            Spacer(modifier = Modifier.height(20.dp))

            // --- Estadísticas ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DashboardCard(
                    title = "Reservas",
                    subtitle = "124",
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.List, contentDescription = null, tint = redSoft, modifier = Modifier.size(30.dp))
                }
                DashboardCard(
                    title = "Carros activos",
                    subtitle = "47",
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = redSoft, modifier = Modifier.size(30.dp))
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun DashboardCard(
    title: String,
    subtitle: String = "",
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .height(130.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(cardGradient)
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            content()
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(title, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = textGray)
                if (subtitle.isNotEmpty()) {
                    Text(subtitle, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = redSoft)
                }
            }
        }
    }
}

@Composable
fun LargeCard(title: String, value: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(cardGradient)
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Column {
                Text(title, fontWeight = FontWeight.Medium, fontSize = 15.sp, color = textGray)
                Text(value, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = redSoft)
            }
        }
    }
}
