package com.kotlin.u_park.presentation.screens.rates

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.kotlin.u_park.domain.model.Rate
import com.kotlin.u_park.presentation.navigation.Routes

private val RedSoft = Color(0xFFE60023)
private val BackgroundColor = Color(0xFFF5F5F5)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RatesScreen(
    navController: NavController,
    viewModel: RatesViewModel,
    userId: String,
    onCreateRate: (String) -> Unit,
    onEditRate: (String) -> Unit
) {

    val redPrimary = Color(0xFFE60023)
    val vehicleTypes by viewModel.vehicleTypes
    val grouped by viewModel.groupedRates.collectAsState()
    val garages by viewModel.garages

    // 🔥 Estado para el diálogo de selección
    var showGarageDialog by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        viewModel.loadAllRates(userId)
        viewModel.loadVehicleTypes()
        viewModel.loadGarages(userId)
    }
    LaunchedEffect(userId) {
        println("🖥 [UI] RatesScreen INIT para user → $userId")

        viewModel.loadAllRates(userId)
        viewModel.loadVehicleTypes()
        viewModel.loadGarages(userId)
    }

    Scaffold(

        // ---------------- TOPBAR ----------------
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Mis Tarifas", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "Gestión de precios por garage",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = RedSoft,
                    titleContentColor = Color.White
                )
            )
        },

        // ---------------- FAB ÚNICO ----------------
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showGarageDialog = true },
                containerColor = RedSoft,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("Nueva Tarifa") }
            )
        },

        // ---------------- BOTTOMBAR ----------------
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = true,
                    onClick = {},
                    icon = { Icon(Icons.Outlined.AttachMoney, null, tint = redPrimary) },
                    label = { Text("Tarifas", color = redPrimary, fontSize = 12.sp) }
                )

                NavigationBarItem(
                    selected = false,
                    onClick = {
                        navController.navigate(Routes.DuenoGarage.route)
                    },
                    icon = { Icon(Icons.Filled.Dashboard, null) },
                    label = { Text("Dashboard", fontSize = 12.sp) }
                )

                NavigationBarItem(
                    selected = false,
                    onClick = {
                        navController.navigate(Routes.ListaReservas.createRoute("all"))
                    },
                    icon = { Icon(Icons.Outlined.BookmarkBorder, null) },
                    label = { Text("Reservas", fontSize = 12.sp) }
                )

                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(Routes.SettingsDueno.route) },
                    icon = { Icon(Icons.Outlined.Person, null) },
                    label = { Text("Perfil", fontSize = 12.sp) }
                )
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(BackgroundColor)
        ) {

            // 🔥 SI NO HAY NINGÚN GARAGE
            if (grouped.isEmpty()) {
                EmptyRatesState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                )

            } else {
                println("🖥 [UI] groupedRates tamaño: ${grouped.size}")
                grouped.forEach { (g, r) ->
                    println("   🏷 Garage UI: $g → ${r.size} tarifas")
                }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {

                    grouped.forEach { (garageName, rates) ->

                        // ---------------- HEADER DEL GARAGE ----------------
                        item(key = "header_$garageName") {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = RedSoft
                                ),
                                shape = RoundedCornerShape(12.dp),
                                elevation = CardDefaults.cardElevation(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Outlined.Garage,
                                            null,
                                            tint = Color.White,
                                            modifier = Modifier.size(28.dp)
                                        )
                                        Spacer(Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                garageName,
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Text(
                                                if (rates.isEmpty())
                                                    "Sin tarifas"
                                                else
                                                    "${rates.size} tarifa${if (rates.size != 1) "s" else ""}",
                                                fontSize = 12.sp,
                                                color = Color.White.copy(alpha = 0.8f)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // ---------------- TARIFAS DEL GARAGE ----------------
                        if (rates.isEmpty()) {

                            // 🔥 Mostrar mensaje cuando el garage NO tiene tarifas
                            item(key = "empty_$garageName") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "No hay tarifas en este garage",
                                        fontSize = 14.sp,
                                        color = Color.Gray
                                    )
                                }
                            }

                        } else {

                            items(
                                items = rates,
                                key = { rate -> rate.id ?: "" }
                            ) { rate ->

                                val vehicleTypeName = vehicleTypes
                                    .firstOrNull { it.first == rate.vehicleTypeId }
                                    ?.second

                                RateItemCard(
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
        }

        // 🔥 DIÁLOGO PARA SELECCIONAR GARAGE
        if (showGarageDialog) {
            AlertDialog(
                onDismissRequest = { showGarageDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.Warehouse,
                            null,
                            tint = RedSoft,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text("Selecciona un Garage", fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "¿A qué garage deseas agregar la tarifa?",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )

                        Spacer(Modifier.height(8.dp))

                        // 🔥 Solo garages del dueño
                        garages.forEach { (garageId, garageName) ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showGarageDialog = false
                                        onCreateRate(garageId)
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White
                                ),
                                elevation = CardDefaults.cardElevation(2.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        modifier = Modifier.size(40.dp),
                                        shape = CircleShape,
                                        color = RedSoft.copy(alpha = 0.15f)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                Icons.Outlined.Warehouse,
                                                null,
                                                tint = RedSoft,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }

                                    Spacer(Modifier.width(16.dp))

                                    Text(
                                        garageName,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.weight(1f)
                                    )

                                    Icon(
                                        Icons.Default.ChevronRight,
                                        null,
                                        tint = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showGarageDialog = false }) {
                        Text("Cancelar", color = RedSoft)
                    }
                }
            )
        }
    }
}

@Composable
fun EmptyRatesState(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Surface(
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                color = Color(0xFFE0E0E0)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.AttachMoney,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                "No hay tarifas registradas",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            Text(
                "Comienza creando tu primera tarifa",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun RateItemCard(
    rate: Rate,
    vehicleTypeName: String?,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(14.dp)
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {

            // ---------------- HEADER ----------------
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Row(verticalAlignment = Alignment.CenterVertically) {

                    Surface(
                        modifier = Modifier.size(42.dp),
                        shape = CircleShape,
                        color = RedSoft.copy(alpha = 0.15f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.AttachMoney,
                                contentDescription = null,
                                tint = RedSoft,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(Modifier.width(12.dp))

                    Column {
                        Text(
                            "RD$ ${rate.baseRate}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = RedSoft
                        )
                        Text(
                            "por ${rate.timeUnit}",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }

                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, null, tint = Color(0xFF2196F3))
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, null, tint = RedSoft)
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // ---------------- DETALLES ----------------
            InfoRow(
                icon = Icons.Outlined.DirectionsCar,
                label = "Tipo de vehículo",
                value = vehicleTypeName ?: "Cualquiera"
            )

            Spacer(Modifier.height(6.dp))

            InfoRow(
                icon = Icons.Outlined.CalendarToday,
                label = "Días aplicables",
                value = rate.diasAplicables.joinToString(", ") { it.capitalize() }
            )

            rate.specialRate?.let { special ->
                Spacer(Modifier.height(8.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFFF3E0)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.Star, null, tint = Color(0xFFFF9800))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Tarifa especial: $special",
                            color = Color(0xFFE65100)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {

        Icon(icon, null, tint = Color.Gray, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))

        Text("$label: ", fontSize = 13.sp, color = Color.Gray)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

fun String.capitalize() = replaceFirstChar {
    if (it.isLowerCase()) it.titlecase() else it.toString()
}