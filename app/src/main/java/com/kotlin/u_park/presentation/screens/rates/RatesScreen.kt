package com.kotlin.u_park.presentation.screens.rates

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.kotlin.u_park.domain.model.Rate
import com.kotlin.u_park.presentation.navigation.Routes

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RatesScreen(
    navController: NavController,
    viewModel: RatesViewModel,
    userId: String,
    onCreateRate: (String) -> Unit,
    onEditRate: (String) -> Unit
) {
    val vehicleTypes by viewModel.vehicleTypes
    val grouped by viewModel.groupedRates.collectAsState()
    val garages by viewModel.garages

    var showGarageDialog by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        viewModel.loadAllRates(userId)
        viewModel.loadVehicleTypes()
        viewModel.loadGarages(userId)
    }

    Scaffold(
        containerColor = BackgroundColor,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showGarageDialog = true },
                containerColor = PrimaryRed,
                contentColor = SurfaceColor,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 12.dp
                )
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Nueva tarifa",
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        bottomBar = {
            ModernBottomBarAdmin(
                selectedIndex = 0,
                onItemSelected = { index ->
                    when (index) {
                        1 -> navController.navigate(Routes.DuenoGarage.route)
                        2 -> navController.navigate(Routes.ListaReservas.createRoute("all"))
                        3 -> navController.navigate(Routes.SettingsDueno.route)
                    }
                }
            )
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
                        "Mis Tarifas",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        "Gestión de precios por garage",
                        fontSize = 14.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Empty State or Garage Groups
            if (grouped.isEmpty()) {
                item {
                    ModernEmptyRatesState()
                }
            } else {
                grouped.forEach { (garageName, rates) ->
                    // Garage Header
                    item(key = "header_$garageName") {
                        GarageHeaderCard(
                            garageName = garageName,
                            ratesCount = rates.size
                        )
                    }

                    // Rates List or Empty
                    if (rates.isEmpty()) {
                        item(key = "empty_$garageName") {
                            EmptyRatesForGarage()
                        }
                    } else {
                        items(
                            items = rates,
                            key = { rate -> rate.id ?: "" }
                        ) { rate ->
                            val vehicleTypeName = vehicleTypes
                                .firstOrNull { it.first == rate.vehicleTypeId }
                                ?.second

                            ModernRateCard(
                                rate = rate,
                                vehicleTypeName = vehicleTypeName,
                                onEdit = { onEditRate(rate.id!!) },
                                onDelete = { viewModel.deleteRate(rate.id!!) }
                            )
                        }
                    }
                }
            }
        }

        // Garage Selection Dialog
        if (showGarageDialog) {
            GarageSelectionDialog(
                garages = garages,
                onDismiss = { showGarageDialog = false },
                onGarageSelected = { garageId ->
                    showGarageDialog = false
                    onCreateRate(garageId)
                }
            )
        }
    }
}

@Composable
fun GarageHeaderCard(
    garageName: String,
    ratesCount: Int
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = PrimaryRed,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(SurfaceColor.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.Garage,
                    contentDescription = null,
                    tint = SurfaceColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    garageName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = SurfaceColor
                )
                Text(
                    if (ratesCount == 0) "Sin tarifas"
                    else "$ratesCount tarifa${if (ratesCount != 1) "s" else ""}",
                    fontSize = 13.sp,
                    color = SurfaceColor.copy(alpha = 0.85f),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun ModernRateCard(
    rate: Rate,
    vehicleTypeName: String?,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = SurfaceColor,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with Price and Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(PrimaryRed.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.AttachMoney,
                            contentDescription = null,
                            tint = PrimaryRed,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Text(
                            "RD$ ${rate.baseRate}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryRed
                        )
                        Text(
                            "por ${rate.timeUnit}",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Surface(
                        onClick = onEdit,
                        shape = CircleShape,
                        color = InfoBlue.copy(alpha = 0.1f)
                    ) {
                        Box(
                            modifier = Modifier.size(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Outlined.Edit,
                                contentDescription = "Editar",
                                tint = InfoBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Surface(
                        onClick = { showDeleteDialog = true },
                        shape = CircleShape,
                        color = PrimaryRed.copy(alpha = 0.1f)
                    ) {
                        Box(
                            modifier = Modifier.size(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Outlined.Delete,
                                contentDescription = "Eliminar",
                                tint = PrimaryRed,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Divider(color = BorderColor)

            // Vehicle Type
            InfoRowModern(
                icon = Icons.Outlined.DirectionsCar,
                label = "Tipo de vehículo",
                value = vehicleTypeName ?: "Cualquiera",
                iconColor = TextSecondary
            )

            // Days
            InfoRowModern(
                icon = Icons.Outlined.CalendarToday,
                label = "Días aplicables",
                value = rate.diasAplicables.joinToString(", ") { it.capitalize() },
                iconColor = TextSecondary
            )

            // Special Rate Badge
            rate.specialRate?.let { special ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = WarningOrange.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Star,
                            contentDescription = null,
                            tint = WarningOrange,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            "Tarifa especial: $special",
                            fontSize = 13.sp,
                            color = WarningOrange,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = {
                Icon(
                    Icons.Outlined.Warning,
                    contentDescription = null,
                    tint = PrimaryRed,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    "¿Eliminar tarifa?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "Esta acción no se puede deshacer. La tarifa será eliminada permanentemente.",
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryRed
                    )
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
fun InfoRowModern(
    icon: ImageVector,
    label: String,
    value: String,
    iconColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = iconColor,
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
fun EmptyRatesForGarage() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = SurfaceColor,
        shadowElevation = 1.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "No hay tarifas en este garage",
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ModernEmptyRatesState() {
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
                Icons.Outlined.AttachMoney,
                contentDescription = null,
                modifier = Modifier.size(50.dp),
                tint = TextSecondary.copy(alpha = 0.5f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "Sin tarifas registradas",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Comienza creando tu primera tarifa\npara empezar a gestionar precios",
            fontSize = 15.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
    }
}

@Composable
fun GarageSelectionDialog(
    garages: List<Pair<String, String>>,
    onDismiss: () -> Unit,
    onGarageSelected: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Outlined.Warehouse,
                contentDescription = null,
                tint = PrimaryRed,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                "Selecciona un Garage",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "¿A qué garage deseas agregar la tarifa?",
                    fontSize = 14.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(8.dp))

                garages.forEach { (garageId, garageName) ->
                    Surface(
                        onClick = { onGarageSelected(garageId) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = SurfaceColor,
                        shadowElevation = 1.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(PrimaryRed.copy(alpha = 0.1f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Outlined.Warehouse,
                                    contentDescription = null,
                                    tint = PrimaryRed,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Text(
                                garageName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary,
                                modifier = Modifier.weight(1f)
                            )

                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = TextSecondary.copy(alpha = 0.5f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TextSecondary)
            }
        }
    )
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
                label = "Tarifas",
                isSelected = selectedIndex == 0,
                onClick = { onItemSelected(0) }
            )
            BottomBarItemAdmin(
                icon = Icons.Outlined.Dashboard,
                selectedIcon = Icons.Default.Dashboard,
                label = "Dashboard",
                isSelected = selectedIndex == 1,
                onClick = { onItemSelected(1) }
            )
            BottomBarItemAdmin(
                icon = Icons.Outlined.BookmarkBorder,
                selectedIcon = Icons.Default.Bookmark,
                label = "Reservas",
                isSelected = selectedIndex == 2,
                onClick = { onItemSelected(2) }
            )
            BottomBarItemAdmin(
                icon = Icons.Outlined.Person,
                selectedIcon = Icons.Default.Person,
                label = "Perfil",
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
