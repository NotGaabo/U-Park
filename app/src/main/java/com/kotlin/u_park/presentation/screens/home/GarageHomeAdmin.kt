package com.kotlin.u_park.presentation.screens.home

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.kotlin.u_park.domain.model.Garage
import com.kotlin.u_park.presentation.navigation.Routes
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
    val context = LocalContext.current

    val sharedPrefs = remember {
        context.getSharedPreferences("garage_prefs", Context.MODE_PRIVATE)
    }

    val redPrimary = Color(0xFFE60023)
    val redLight = Color(0xFFFF6B6B)
    val backgroundGray = Color(0xFFF5F7FA)

    val garages by viewModel.garages.collectAsState(initial = emptyList())
    val isLoading by viewModel.isLoading.collectAsState()
    val hasLoadedInitially by viewModel.hasLoadedInitially.collectAsState()

    var showAddGarage by remember { mutableStateOf(false) }

    // Solo mostramos AddGarage la primera vez si NO tiene garages
    val hasShownAddSheet = remember(userId) {
        sharedPrefs.getBoolean("has_shown_add_sheet_$userId", false)
    }

    // Cargar garajes
    LaunchedEffect(userId) {
        viewModel.loadGaragesByUser(userId)
    }

    // Primera vez → mostrar sheet
    LaunchedEffect(hasLoadedInitially, hasShownAddSheet) {
        if (hasLoadedInitially && !hasShownAddSheet && garages.isEmpty()) {
            showAddGarage = true
            sharedPrefs.edit().putBoolean("has_shown_add_sheet_$userId", true).apply()
        }
    }

    // Pantalla de agregar garage
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

    // Loader
    if (isLoading && !hasLoadedInitially) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundGray),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = redPrimary)
        }
        return
    }

    // UI principal
    Scaffold(
        containerColor = backgroundGray,
        bottomBar = {
            if (garages.isNotEmpty()) {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = false,
                        onClick = {
                            navController.navigate(Routes.Rates.createRoute(userId))
                        },
                        icon = { Icon(Icons.Outlined.AttachMoney, null) },
                        label = { Text("Tarifas", fontSize = 12.sp) }
                    )
                    NavigationBarItem(
                        selected = true,
                        onClick = {},
                        icon = { Icon(Icons.Filled.Dashboard, null, tint = redPrimary) },
                        label = { Text("Dashboard", color = redPrimary, fontSize = 12.sp) }
                    )

                    // 🔥 Reservas — SIEMPRE ENVIAMOS "all"
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
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {

            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(redPrimary, redLight)
                        )
                    )
                    .padding(24.dp)
            ) {
                Column {
                    Text(
                        "Mis Garages",
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        color = Color.White
                    )
                    Text(
                        "${garages.size} registrados",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                if (garages.isNotEmpty()) {
                    GeneralStatsCard(garages, redPrimary)
                    Spacer(modifier = Modifier.height(24.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Tus Garages",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp,
                        color = Color(0xFF2D3436)
                    )

                    IconButton(
                        onClick = { showAddGarage = true },
                        modifier = Modifier
                            .size(40.dp)
                            .background(redPrimary, CircleShape)
                    ) {
                        Icon(Icons.Default.Add, null, tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (garages.isEmpty()) {
                    EmptyGaragesView { showAddGarage = true }
                } else {
                    garages.forEach { garage ->
                        ModernGarageCard(
                            garage = garage,
                            redPrimary = redPrimary,
                            onClick = {
                                navController.navigate(
                                    Routes.Empleados.createRoute(garage.idGarage)
                                )
                            }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun GeneralStatsCard(garages: List<Garage>, redPrimary: Color) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {

            Text(
                "Resumen General",
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    icon = Icons.Filled.LocalParking,
                    value = garages.sumOf { it.capacidadTotal }.toString(),
                    label = "Espacios",
                    color = redPrimary
                )

                StatItem(
                    icon = Icons.Filled.TrendingUp,
                    value = "%",
                    label = "Ocupación",
                    color = Color(0xFF00B894)
                )

                StatItem(
                    icon = Icons.Filled.AttachMoney,
                    value = "RD$",
                    label = "Hoy",
                    color = Color(0xFFFD79A8)
                )
            }
        }
    }
}

@Composable
fun StatItem(icon: ImageVector, value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        Box(
            modifier = Modifier
                .size(48.dp)
                .background(color.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(label, fontSize = 12.sp, color = Color.Gray)
    }
}

@Composable
fun ModernGarageCard(garage: Garage, redPrimary: Color, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .shadow(2.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF5F7FA))
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
                        Icons.Default.DirectionsCar,
                        null,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        tint = Color.Gray.copy(alpha = 0.3f)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {

                Text(
                    garage.nombre,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                    Spacer(Modifier.width(4.dp))
                    Text(
                        garage.direccion ?: "Sin dirección",
                        fontSize = 13.sp,
                        color = Color.Gray,
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {

                    Surface(
                        color = redPrimary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.LocalParking, null, modifier = Modifier.size(14.dp), tint = redPrimary)
                            Spacer(Modifier.width(4.dp))
                            Text("${garage.capacidadTotal} espacios", fontSize = 12.sp, color = redPrimary)
                        }
                    }

                    Surface(
                        color = Color(0xFF00B894).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                Modifier
                                    .size(8.dp)
                                    .background(Color(0xFF00B894), CircleShape)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("Activo", fontSize = 12.sp, color = Color(0xFF00B894))
                        }
                    }
                }
            }

            Icon(Icons.Default.ChevronRight, null, tint = Color.Gray)
        }
    }
}

@Composable
fun EmptyGaragesView(onAddClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Box(
            modifier = Modifier
                .size(120.dp)
                .background(Color(0xFFF5F7FA), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Garage,
                null,
                modifier = Modifier.size(60.dp),
                tint = Color.Gray.copy(alpha = 0.4f)
            )
        }

        Spacer(Modifier.height(24.dp))

        Text("No tienes garages registrados", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
        Text("Agrega tu primer garage para comenzar", fontSize = 14.sp, color = Color.Gray)

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = onAddClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE60023)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.height(50.dp)
        ) {
            Icon(Icons.Default.Add, null)
            Spacer(Modifier.width(8.dp))
            Text("Agregar Garage", fontSize = 16.sp)
        }
    }
}
