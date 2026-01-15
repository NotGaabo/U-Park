package com.kotlin.u_park.presentation.screens.home

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.kotlin.u_park.R
import com.kotlin.u_park.domain.model.Garage
import com.kotlin.u_park.presentation.navigation.Routes
import com.kotlin.u_park.presentation.screens.garage.GarageAddScreen
import com.kotlin.u_park.presentation.screens.garage.GarageViewModel

// 🎨 Modern Color System
private val PrimaryRed = Color(0xFFE60023)
private val DarkRed = Color(0xFFB8001C)
private val LightRed = Color(0xFFFFE5E9)
private val BackgroundColor = Color(0xFFFAFAFA)
private val SurfaceColor = Color(0xFFFFFFFF)
private val TextPrimary = Color(0xFF0D0D0D)
private val TextSecondary = Color(0xFF6E6E73)
private val BorderColor = Color(0xFFE5E5EA)
private val SuccessGreen = Color(0xFF34C759)
private val InfoBlue = Color(0xFF007AFF)
private val WarningOrange = Color(0xFFFF9500)

@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("MissingPermission")
@Composable
fun DuenoGarageScreen(
    navController: NavController,
    viewModel: GarageViewModel,
    userId: String
) {
    val context = LocalContext.current
    val sharedPrefs = remember {
        context.getSharedPreferences("garage_prefs", Context.MODE_PRIVATE)
    }

    val garages by viewModel.garages.collectAsState(initial = emptyList())
    val isLoading by viewModel.isLoading.collectAsState()
    val hasLoadedInitially by viewModel.hasLoadedInitially.collectAsState()

    var showAddGarage by remember { mutableStateOf(false) }

    val hasShownAddSheet = remember(userId) {
        sharedPrefs.getBoolean("has_shown_add_sheet_$userId", false)
    }

    // Load garages
    LaunchedEffect(userId) {
        viewModel.loadGaragesByUser(userId)
    }

    // Show add sheet on first load
    LaunchedEffect(hasLoadedInitially, hasShownAddSheet) {
        if (hasLoadedInitially && !hasShownAddSheet && garages.isEmpty()) {
            showAddGarage = true
            sharedPrefs.edit().putBoolean("has_shown_add_sheet_$userId", true).apply()
        }
    }

    // Add Garage Screen
    if (showAddGarage) {
        GarageAddScreen(
            userId = userId,
            viewModel = viewModel,
            onDismiss = { showAddGarage = false },
            onSuccess = {
                showAddGarage = false
                viewModel.loadGaragesByUser(userId)
            }
        )
        return
    }

    // Loading State
    if (isLoading && !hasLoadedInitially) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundColor),
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
                    stringResource(R.string.cargando_garages),
                    fontSize = 15.sp,
                    color = TextSecondary
                )
            }
        }
        return
    }

    // Main UI
    Scaffold(
        containerColor = BackgroundColor,
        bottomBar = {
            if (garages.isNotEmpty()) {
                ModernBottomBarAdmin(
                    selectedIndex = 1,
                    onItemSelected = { index ->
                        when (index) {
                            0 -> navController.navigate(Routes.Rates.createRoute(userId))
                            2 -> navController.navigate(Routes.ListaReservas.createRoute("all"))
                            3 -> navController.navigate(Routes.SettingsDueno.route)
                        }
                    }
                )
            }
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
                        stringResource(R.string.mis_garages),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        "${garages.size} ${if (garages.size == 1) stringResource(R.string.garage_registrado) else stringResource(
                            R.string.garages_registrados
                        )}",
                        fontSize = 14.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Stats Card
            if (garages.isNotEmpty()) {
                item {
                    ModernStatsCard(garages)
                }
            }

            // Section Header with Add Button
            item {
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
                            Icons.Outlined.Garage,
                            contentDescription = null,
                            tint = TextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            stringResource(R.string.tus_garages),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Surface(
                        onClick = { showAddGarage = true },
                        shape = CircleShape,
                        color = PrimaryRed,
                        shadowElevation = 4.dp
                    ) {
                        Box(
                            modifier = Modifier.size(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = stringResource(R.string.agregar_garage),
                                tint = SurfaceColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Empty State or Garage List
            if (garages.isEmpty()) {
                item {
                    ModernEmptyState { showAddGarage = true }
                }
            } else {
                items(garages) { garage ->
                    ModernGarageCard(
                        garage = garage,
                        onClick = {
                            navController.navigate(
                                Routes.GarageDashboard.createRoute(
                                    garageId = garage.idGarage,
                                    garageName = garage.nombre
                                )
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ModernStatsCard(garages: List<Garage>) {
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Outlined.Analytics,
                    contentDescription = null,
                    tint = PrimaryRed,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    stringResource(R.string.resumen_general),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    icon = Icons.Outlined.LocalParking,
                    value = garages.sumOf { it.capacidadTotal }.toString(),
                    label = stringResource(R.string.espacios),
                    color = PrimaryRed
                )

                StatItem(
                    icon = Icons.Outlined.TrendingUp,
                    value = "${garages.size}",
                    label = stringResource(R.string.garages),
                    color = SuccessGreen
                )

                StatItem(
                    icon = Icons.Outlined.CheckCircle,
                    value = "${garages.size}",
                    label = stringResource(R.string.activos3),
                    color = InfoBlue
                )
            }
        }
    }
}

@Composable
fun StatItem(
    icon: ImageVector,
    value: String,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(color.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }

        Text(
            value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
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
fun ModernGarageCard(
    garage: Garage,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = SurfaceColor,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Garage Image
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(BackgroundColor)
            ) {
                if (!garage.imageUrl.isNullOrEmpty()) {
                    Image(
                        painter = rememberAsyncImagePainter(garage.imageUrl),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        Icons.Outlined.Garage,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        tint = TextSecondary.copy(alpha = 0.4f)
                    )
                }
            }

            // Garage Info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    garage.nombre,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Outlined.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = TextSecondary
                    )
                    Text(
                        garage.direccion ?: stringResource(R.string.sin_direcci_n2),
                        fontSize = 13.sp,
                        color = TextSecondary,
                        maxLines = 1
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = PrimaryRed.copy(alpha = 0.1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Outlined.LocalParking,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = PrimaryRed
                            )
                            Text(
                                "${garage.capacidadTotal}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryRed
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SuccessGreen.copy(alpha = 0.1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(SuccessGreen, CircleShape)
                            )
                            Text(
                                stringResource(R.string.activo),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SuccessGreen
                            )
                        }
                    }
                }
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextSecondary.copy(alpha = 0.5f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun ModernEmptyState(onAddClick: () -> Unit) {
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
                Icons.Outlined.Garage,
                contentDescription = null,
                modifier = Modifier.size(50.dp),
                tint = TextSecondary.copy(alpha = 0.5f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            stringResource(R.string.sin_garages_registrados),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            stringResource(R.string.agrega_tu_primer_garage_para_comenzar_a_gestionar_tus_espacios),
            fontSize = 15.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onAddClick,
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .height(52.dp)
                .padding(horizontal = 40.dp),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 4.dp,
                pressedElevation = 8.dp
            )
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                stringResource(R.string.agregar_garage2),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
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
                label = stringResource(R.string.tarifas2),
                isSelected = selectedIndex == 0,
                onClick = { onItemSelected(0) }
            )
            BottomBarItemAdmin(
                icon = Icons.Outlined.Dashboard,
                selectedIcon = Icons.Default.Dashboard,
                label = stringResource(R.string.dashboard2),
                isSelected = selectedIndex == 1,
                onClick = { onItemSelected(1) }
            )
            BottomBarItemAdmin(
                icon = Icons.Outlined.BookmarkBorder,
                selectedIcon = Icons.Default.Bookmark,
                label = stringResource(R.string.reservas3),
                isSelected = selectedIndex == 2,
                onClick = { onItemSelected(2) }
            )
            BottomBarItemAdmin(
                icon = Icons.Outlined.Person,
                selectedIcon = Icons.Default.Person,
                label = stringResource(R.string.perfil2),
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