package com.kotlin.u_park.presentation.screens.parking

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import com.kotlin.u_park.presentation.utils.formatHora
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.kotlin.u_park.R
import com.kotlin.u_park.domain.model.HistorialParking
import com.kotlin.u_park.presentation.navigation.Routes
import com.kotlin.u_park.ui.theme.*

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

    LaunchedEffect(Unit) {
        viewModel.cargarHistorial(userId)
    }

    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            Surface(
                color = SurfaceColor,
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Text(
                        stringResource(R.string.mi_historial),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        stringResource(R.string.revisa_tus_parkings),
                        fontSize = 14.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        bottomBar = {
            ModernBottomBar(
                selectedIndex = 2,
                onItemSelected = { index ->
                    when (index) {
                        0 -> navController.navigate(Routes.Home.route)
                        1 -> navController.navigate("vehicles")
                        3 -> navController.navigate(Routes.Settings.route)
                    }
                }
            )
        }
    ) { padding ->

        if (loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(
                        color = PrimaryRed,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        stringResource(R.string.cargando_historial),
                        fontSize = 15.sp,
                        color = TextSecondary
                    )
                }
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // 🚗 Active Parking Section
            activo?.let { activeParking ->
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SectionHeader(
                            title = stringResource(R.string.parking_activo),
                            icon = Icons.Outlined.AccessTime,
                            iconColor = SuccessGreen
                        )
                        ActiveParkingCard(activeParking)
                    }
                }
            }

            // 📅 History Section Header
            item {
                SectionHeader(
                    title = stringResource(R.string.historial2),
                    icon = Icons.Outlined.History,
                    iconColor = TextSecondary
                )
            }

            // Empty State or History Items
            if (historial.isEmpty()) {
                item {
                    EmptyHistoryState()
                }
            } else {
                items(historial) { parking ->
                    HistoryParkingCard(parking)
                }
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    icon: ImageVector,
    iconColor: Color = TextPrimary
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(20.dp)
        )
        Text(
            title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
    }
}

@Composable
fun ActiveParkingCard(parking: HistorialParking) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = LightRed,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with pulse animation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(PrimaryRed.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.LocalParking,
                            contentDescription = null,
                            tint = PrimaryRed,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Text(
                            parking.garage_nombre ?: stringResource(R.string.garaje),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            stringResource(R.string.en_progreso),
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }
                }

                // Pulsing indicator
                PulsingDot()
            }

            Divider(color = PrimaryRed.copy(alpha = 0.2f))

            // Vehicle Info
            InfoRow(
                icon = Icons.Outlined.DirectionsCar,
                label = stringResource(R.string.veh_culo5),
                value = "${parking.plate ?: "—"} • ${parking.vehicle_model ?: ""}"
            )

            // Time Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TimeBlock(
                    label = stringResource(R.string.entrada2),
                    time = parking.hora_entrada ?: "—",
                    icon = Icons.Outlined.Login
                )
                TimeBlock(
                    label = stringResource(R.string.salida_estimada),
                    time = parking.hora_salida ?: "—",
                    icon = Icons.Outlined.Logout
                )
            }
        }
    }
}

@Composable
fun HistoryParkingCard(parking: HistorialParking) {
    val (statusColor, statusBg) = when (parking.estado) {
        stringResource(R.string.completada) -> InfoBlue to InfoBlue.copy(alpha = 0.1f)
        stringResource(R.string.cancelada3) -> WarningOrange to WarningOrange.copy(alpha = 0.1f)
        else -> TextSecondary to BackgroundColor
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = SurfaceColor,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(BackgroundColor, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            parking.garage_nombre ?: stringResource(R.string.garaje2),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusBg
                ) {
                    Text(
                        parking.estado.uppercase(),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }

            Divider(color = BorderColor)

            // Vehicle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Outlined.DirectionsCar,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    "${parking.plate ?: "—"} • ${parking.vehicle_model ?: ""}",
                    fontSize = 14.sp,
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium
                )
            }

            // Times
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TimeBlockCompact(
                    label = stringResource(R.string.entrada3),
                    time = formatHora(parking.hora_entrada)
                )
                TimeBlockCompact(
                    label = stringResource(R.string.salida6),
                    time = formatHora(parking.hora_salida)
                )
            }
        }
    }
}

@Composable
fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(18.dp)
        )
        Column {
            Text(
                label,
                fontSize = 12.sp,
                color = TextSecondary,
                fontWeight = FontWeight.Medium
            )
            Text(
                value,
                fontSize = 14.sp,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun TimeBlock(
    label: String,
    time: String,
    icon: ImageVector
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(PrimaryRed.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = PrimaryRed,
                modifier = Modifier.size(16.dp)
            )
        }
        Column {
            Text(
                label,
                fontSize = 11.sp,
                color = TextSecondary,
                fontWeight = FontWeight.Medium
            )
            Text(
                time,
                fontSize = 15.sp,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun TimeBlockCompact(
    label: String,
    time: String
) {
    Column {
        Text(
            label,
            fontSize = 12.sp,
            color = TextSecondary,
            fontWeight = FontWeight.Medium
        )
        Text(
            time,
            fontSize = 14.sp,
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun PulsingDot() {
    val infiniteTransition = rememberInfiniteTransition()
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = SuccessGreen.copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(SuccessGreen.copy(alpha = alpha), CircleShape)
            )
            Text(
                stringResource(R.string.activo),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = SuccessGreen
            )
        }
    }
}

@Composable
fun EmptyHistoryState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(BackgroundColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Outlined.History,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = TextSecondary.copy(alpha = 0.6f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            stringResource(R.string.sin_historial),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            stringResource(R.string.a_n_no_has_realizado_ning_n_parking_comienza_a_usar_u_park_hoy),
            fontSize = 15.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 40.dp),
            lineHeight = 22.sp
        )
    }
}

@Composable
fun ModernBottomBar(
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
            BottomBarItem(
                icon = Icons.Outlined.Home,
                selectedIcon = Icons.Default.Home,
                label = stringResource(R.string.inicio2),
                isSelected = selectedIndex == 0,
                onClick = { onItemSelected(0) }
            )
            BottomBarItem(
                icon = Icons.Outlined.DirectionsCar,
                selectedIcon = Icons.Default.DirectionsCar,
                label = stringResource(R.string.veh_culos8),
                isSelected = selectedIndex == 1,
                onClick = { onItemSelected(1) }
            )
            BottomBarItem(
                icon = Icons.Outlined.History,
                selectedIcon = Icons.Default.History,
                label = stringResource(R.string.historial3),
                isSelected = selectedIndex == 2,
                onClick = { onItemSelected(2) }
            )
            BottomBarItem(
                icon = Icons.Outlined.Person,
                selectedIcon = Icons.Default.Person,
                label = stringResource(R.string.perfil5),
                isSelected = selectedIndex == 3,
                onClick = { onItemSelected(3) }
            )
        }
    }
}

@Composable
fun BottomBarItem(
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