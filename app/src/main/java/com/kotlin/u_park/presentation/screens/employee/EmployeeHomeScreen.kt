package com.kotlin.u_park.presentation.screens.employee

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.kotlin.u_park.presentation.navigation.Routes
import com.kotlin.u_park.presentation.screens.parking.ParkingViewModel

private val RedSoft = Color(0xFFE60023)
private val BackgroundColor = Color(0xFFF5F5F5)

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeHomeScreen(
    navController: NavController,
    garageId: String,
    parkingId: String?,
    viewModel: EmpleadosViewModel,
    parkingViewModel: ParkingViewModel   // 🔥 SE AGREGA ESTO
) {
    var selectedTab by remember { mutableStateOf(0) }

    val empleados by viewModel.empleados.collectAsState()
    val actividadReciente by parkingViewModel.actividad.collectAsState()
    // 🔥 Recargar actividad al volver a la pantalla
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                println("🔄 EmployeeHomeScreen ON_RESUME → refrescando actividad...")
                parkingViewModel.loadActividad(garageId)
                viewModel.loadStats(garageId)
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val isLoading by viewModel.isLoading.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val totalEmpleados = empleados.size
    val autosActivos = stats?.autosActivos ?: 0
    val espaciosLibres = stats?.espaciosLibres ?: 0
    val entradasHoy = stats?.entradasHoy ?: 0
    val salidasHoy = stats?.salidasHoy ?: 0
    LaunchedEffect(garageId) {
        garageId?.let {
            viewModel.loadEmpleados(it)
            viewModel.loadStats(it)
            parkingViewModel.loadActividad(it)  // 🔥 AQUI SE CARGA LA ACTIVIDAD REAL
        }
    }


    // Cargar datos al iniciar
    LaunchedEffect(garageId) {
        viewModel.loadEmpleados(garageId)
        viewModel.loadStats(garageId)
        parkingViewModel.loadActividad(garageId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "U-Park Employee",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Text(
                            "Panel de Control",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = RedSoft,
                    titleContentColor = Color.White
                )
            )
        },
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
                            Icons.Default.Home,
                            null,
                            tint = if (selectedTab == 0) RedSoft else Color.Gray
                        )
                    },
                    label = {
                        Text(
                            "Home",
                            color = if (selectedTab == 0) RedSoft else Color.Gray
                        )
                    }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        navController.navigate(
                            Routes.VehiculosDentro.createRoute(garageId)
                        )
                    },
                    icon = {
                        Icon(
                            Icons.Default.DirectionsCar,
                            null,
                            tint = if (selectedTab == 1) RedSoft else Color.Gray
                        )
                    },
                    label = {
                        Text(
                            "Vehículos",
                            color = if (selectedTab == 1) RedSoft else Color.Gray
                        )
                    }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        navController.navigate(Routes.EmployeeSettings.route)
                    },
                    icon = {
                        Icon(
                            Icons.Default.Person,
                            null,
                            tint = if (selectedTab == 1) RedSoft else Color.Gray
                        )
                    },
                    label = {
                        Text(
                            "Perfil",
                            color = if (selectedTab == 1) RedSoft else Color.Gray
                        )
                    }
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundColor)
                .padding(padding)
        ) {
            if (isLoading) {
                // Estado de carga
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            color = RedSoft,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Cargando datos...",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Saludo personalizado
                    item {
                        GreetingCard(
                            empleadoNombre = empleados.firstOrNull()?.users?.nombre ?: "Empleado"
                        )
                    }

                    // Estadísticas principales
                    item {
                        Text(
                            "Estadísticas de Hoy",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A1A),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatCard(
                                icon = Icons.Default.DirectionsCar,
                                title = "Autos Dentro",
                                value = autosActivos.toString(),
                                color = Color(0xFF4CAF50),
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                icon = Icons.Default.CheckCircle,
                                title = "Espacios Libres",
                                value = espaciosLibres.toString(),
                                color = Color(0xFF2196F3),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatCard(
                                icon = Icons.Default.ArrowUpward,
                                title = "Entradas",
                                value = entradasHoy.toString(),
                                color = Color(0xFFFF9800),
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                icon = Icons.Default.ArrowDownward,
                                title = "Salidas",
                                value = salidasHoy.toString(),
                                color = RedSoft,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Información del equipo
                    if (empleados.isNotEmpty()) {
                        item {
                            Text(
                                "Equipo de Trabajo",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A1A1A),
                                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                            )
                        }

                        item {
                            TeamCard(totalEmpleados = totalEmpleados)
                        }
                    }

                    /* =======================================================
 * 🔥 ACTIVIDAD RECIENTE CORREGIDA Y OPTIMIZADA
 * ======================================================= */
                    item {
                        Text(
                            "Actividad Reciente",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A1A),
                            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                        )
                    }

                    val actividadOrdenada = actividadReciente.sortedByDescending { act ->
                        act.hora_salida ?: act.hora_entrada ?: ""}
                        .take(5)

                    if (actividadOrdenada.isEmpty()) {

                        item {
                            EmptyStateCard(
                                message = "No hay actividad reciente",
                                icon = Icons.Default.Info
                            )
                        }

                    } else {

                        items(actividadOrdenada) { item ->

                            val plate = item.vehicles?.plate ?: "Desconocido"
                            val isEntry = item.tipo == "entrada"

                            // Determinar hora correcta según sea entrada o salida:
                            val hora = when {
                                item.hora_salida != null -> formatearHora(item.hora_salida!!)
                                item.hora_entrada != null -> formatearHora(item.hora_entrada!!)
                                else -> "--"
                            }

                            ActivityItem(
                                plate = plate,
                                action = if (isEntry) "Entrada" else "Salida",
                                time = hora,
                                isEntry = isEntry
                            )
                        }
                    }

                    // Acciones rápidas
                    item {
                        Text(
                            "Acciones Rápidas",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A1A),
                            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            QuickActionCard(
                                icon = Icons.Default.Add,
                                title = "Registrar Entrada",
                                color = Color(0xFF4CAF50),
                                modifier = Modifier.weight(1f),
                                onClick = { navController.navigate(Routes.RegistrarEntrada.createRoute(garageId ?: "")) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GreetingCard(empleadoNombre: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            RedSoft.copy(alpha = 0.1f),
                            Color.White
                        )
                    )
                )
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(60.dp),
                shape = CircleShape,
                color = RedSoft.copy(alpha = 0.2f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = RedSoft,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            Column {
                Text(
                    "¡Hola, $empleadoNombre!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Listo para gestionar el parking",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun AlertCard(
    title: String,
    message: String,
    icon: ImageVector,
    color: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    message,
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun TeamCard(totalEmpleados: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                color = Color(0xFF6200EA).copy(alpha = 0.15f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = null,
                        tint = Color(0xFF6200EA),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Total de Empleados",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "$totalEmpleados ${if (totalEmpleados == 1) "empleado" else "empleados"}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
            }
        }
    }
}

@Composable
fun EmptyStateCard(
    message: String,
    icon: ImageVector
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(64.dp),
                shape = CircleShape,
                color = Color(0xFFE0E0E0)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(
                message,
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun StatCard(
    icon: ImageVector,
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = color.copy(alpha = 0.15f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                value,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )

            Spacer(Modifier.height(4.dp))

            Text(
                title,
                fontSize = 12.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/* ---------- COMPONENTES REUSABLES ---------- */

@Composable
fun ActivityItem(plate: String, action: String, time: String, isEntry: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {

            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = if (isEntry) Color(0xFF4CAF50).copy(alpha = 0.15f) else RedSoft.copy(alpha = 0.15f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isEntry) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                        contentDescription = null,
                        tint = if (isEntry) Color(0xFF4CAF50) else RedSoft
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(plate, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(action, fontSize = 13.sp, color = Color.Gray)
            }

            Text(time, fontSize = 13.sp, color = Color.Gray)
        }
    }
}

@Composable
fun QuickActionCard(
    icon: ImageVector,
    title: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )

            Spacer(Modifier.height(8.dp))

            Text(
                title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}
@RequiresApi(Build.VERSION_CODES.O)
fun formatearHora(fecha: String): String {
    return try {
        val dt = java.time.OffsetDateTime.parse(fecha)
        dt.toLocalTime().toString().substring(0,5) // 14:10
    } catch (_: Exception) {
        fecha
    }
}
