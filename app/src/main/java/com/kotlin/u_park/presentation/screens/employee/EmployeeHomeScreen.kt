package com.kotlin.u_park.presentation.screens.employee

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.kotlin.u_park.R
import com.kotlin.u_park.presentation.navigation.Routes
import com.kotlin.u_park.presentation.screens.parking.ParkingViewModel
import com.kotlin.u_park.presentation.screens.profile.BottomBarItemModern
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import com.kotlin.u_park.ui.theme.*


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeHomeScreen(
    navController: NavController,
    garageId: String,
    parkingId: String?,
    viewModel: EmpleadosViewModel,
    parkingViewModel: ParkingViewModel
) {
    val empleados by viewModel.empleados.collectAsState()
    val actividadReciente by parkingViewModel.actividad.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    // Auto-refresh cuando vuelve la pantalla
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                parkingViewModel.loadActividad(garageId)
                viewModel.loadStats(garageId)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val isLoading by viewModel.isLoading.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val autosActivos = stats?.autosActivos ?: 0
    val espaciosLibres = stats?.espaciosLibres ?: 0
    val entradasHoy = stats?.entradasHoy ?: 0
    val salidasHoy = stats?.salidasHoy ?: 0

    LaunchedEffect(garageId) {
        viewModel.loadEmpleados(garageId)
        viewModel.loadStats(garageId)
        parkingViewModel.loadActividad(garageId)
    }

    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            stringResource(R.string.panel_de_control),
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = TextPrimary
                        )
                        Text(
                            getCurrentGreeting(),
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceColor
                )
            )
        },
        bottomBar = {
            ModernBottomBarEmployee(
                selectedIndex = 0,
                onItemSelected = { index ->
                    when (index) {
                        1 -> navController.navigate(Routes.VehiculosDentro.createRoute(garageId))
                        2 -> garageId?.let {
                            navController.navigate(Routes.EmployeeSettings.createRoute(it))
                        }
                        3 -> navController.navigate(Routes.ParkingRecords.createRoute(garageId))
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundColor)
                .padding(padding)
        ) {
            if (isLoading) {
                LoadingState()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Hero Card con estadísticas principales
                    item {
                        HeroStatsCard(
                            autosActivos = autosActivos,
                            espaciosLibres = espaciosLibres,
                            entradasHoy = entradasHoy,
                            salidasHoy = salidasHoy
                        )
                    }

                    // Stats Grid
                    item {
                        Text(
                            stringResource(R.string.estad_sticas_en_tiempo_real),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ModernStatCard(
                                icon = Icons.Default.DirectionsCar,
                                title = "Dentro",
                                value = autosActivos.toString(),
                                color = SuccessGreen,
                                modifier = Modifier.weight(1f)
                            )
                            ModernStatCard(
                                icon = Icons.Default.CheckCircle,
                                title = "Libres",
                                value = espaciosLibres.toString(),
                                color = InfoBlue,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ModernStatCard(
                                icon = Icons.Default.Login,
                                title = "Entradas",
                                value = entradasHoy.toString(),
                                color = WarningOrange,
                                modifier = Modifier.weight(1f)
                            )
                            ModernStatCard(
                                icon = Icons.Default.Logout,
                                title = "Salidas",
                                value = salidasHoy.toString(),
                                color = PrimaryRed,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Acción rápida destacada
                    item {
                        Card(
                            onClick = { navController.navigate(Routes.RegistrarEntrada.createRoute(garageId)) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = PrimaryRed
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .background(
                                            Color.White.copy(alpha = 0.2f),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.AddCircle,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                Spacer(Modifier.width(20.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        stringResource(R.string.registrar_entrada),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        stringResource(R.string.toca_para_registrar_un_nuevo_veh_culo),
                                        fontSize = 13.sp,
                                        color = Color.White.copy(alpha = 0.9f)
                                    )
                                }
                                Icon(
                                    Icons.Default.ArrowForward,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                            }
                        }
                    }

                    // Actividad Reciente
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                stringResource(R.string.actividad_reciente),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            TextButton(
                                onClick = { navController.navigate(Routes.VehiculosDentro.createRoute(garageId)) }
                            ) {
                                Text(stringResource(R.string.ver_todo), color = PrimaryRed)
                            }
                        }
                    }

                    val actividadOrdenada = actividadReciente
                        .sortedByDescending { it.hora_salida ?: it.hora_entrada ?: "" }
                        .take(5)

                    if (actividadOrdenada.isEmpty()) {
                        item {
                            EmptyActivityState()
                        }
                    } else {
                        items(actividadOrdenada) { item ->
                            val plate = item.vehicles?.plate ?: "Desconocido"
                            val isEntry = item.tipo == "entrada"
                            val hora = when {
                                item.hora_salida != null -> formatearHora(item.hora_salida!!)
                                item.hora_entrada != null -> formatearHora(item.hora_entrada!!)
                                else -> "--"
                            }
                            ModernActivityItem(
                                plate = plate,
                                action = if (isEntry) "Entrada" else "Salida",
                                time = hora,
                                isEntry = isEntry
                            )
                        }
                    }

                    // Espacio inferior
                    item { Spacer(Modifier.height(20.dp)) }
                }
            }
        }
    }
}

@Composable
fun HeroStatsCard(
    autosActivos: Int,
    espaciosLibres: Int,
    entradasHoy: Int,
    salidasHoy: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            LightRed,
                            SurfaceColor
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(PrimaryRed.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Dashboard,
                            contentDescription = null,
                            tint = PrimaryRed,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            stringResource(R.string.estado_del_parqueo),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary
                        )
                        Text(
                            stringResource(R.string.resumen_de_hoy),
                            fontSize = 14.sp,
                            color = TextSecondary.copy(alpha = 0.7f)
                        )
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun QuickStat(
    value: String,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            value,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            label,
            fontSize = 12.sp,
            color = TextSecondary,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun ModernStatCard(
    icon: ImageVector,
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val progress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(1000)
    )

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = SurfaceColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(color.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                value,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )

            Spacer(Modifier.height(4.dp))

            Text(
                title,
                fontSize = 13.sp,
                color = TextSecondary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun ModernActivityItem(
    plate: String,
    action: String,
    time: String,
    isEntry: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (isEntry) SuccessGreen.copy(alpha = 0.15f)
                        else PrimaryRed.copy(alpha = 0.15f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (isEntry) Icons.Default.Login else Icons.Default.Logout,
                    contentDescription = null,
                    tint = if (isEntry) SuccessGreen else PrimaryRed,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    plate,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    action,
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    time,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                Text(
                    stringResource(R.string.hoy),
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
fun EmptyActivityState() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceColor
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color(0xFFF3F4F6), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = Color(0xFF9CA3AF),
                    modifier = Modifier.size(40.dp)
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                stringResource(R.string.sin_actividad_reciente),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Text(
                stringResource(R.string.los_movimientos_aparecer_n_aqu),
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ModernBottomBarEmployee(
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
            BottomBarItemModern(
                icon = Icons.Outlined.Home,
                selectedIcon = Icons.Default.Home,
                label = "Home",
                isSelected = selectedIndex == 0,
                onClick = { onItemSelected(0) }
            )
            BottomBarItemModern(
                icon = Icons.Outlined.DirectionsCar,
                selectedIcon = Icons.Default.DirectionsCar,
                label = "Vehículos",
                isSelected = selectedIndex == 1,
                onClick = { onItemSelected(1) }
            )
            BottomBarItemModern(
                icon = Icons.Outlined.CarCrash,
                selectedIcon = Icons.Default.CarCrash,
                label = "Incidencias",
                isSelected = selectedIndex == 3,
                onClick = { onItemSelected(3) }
            )
            BottomBarItemModern(
                icon = Icons.Outlined.Person,
                selectedIcon = Icons.Default.Person,
                label = "Perfil",
                isSelected = selectedIndex == 2,
                onClick = { onItemSelected(2) }
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun formatearHora(fecha: String): String {
    return try {
        val dt = java.time.OffsetDateTime.parse(fecha)
        dt.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"))
    } catch (_: Exception) {
        fecha.take(5)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun getCurrentGreeting(): String {
    val hour = LocalTime.now().hour
    return when (hour) {
        in 0..11 -> "Buenos días"
        in 12..17 -> "Buenas tardes"
        else -> "Buenas noches"
    }
}