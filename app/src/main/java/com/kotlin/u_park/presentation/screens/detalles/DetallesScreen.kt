package com.kotlin.u_park.presentation.screens.detalles

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.kotlin.u_park.R
import com.kotlin.u_park.data.remote.supabase
import com.kotlin.u_park.data.repository.GarageRepositoryImpl
import com.kotlin.u_park.data.repository.EmpleadoGarageRepositoryImpl
import com.kotlin.u_park.domain.model.Garage
import com.kotlin.u_park.domain.model.Stats
import com.kotlin.u_park.presentation.navigation.Routes
import com.kotlin.u_park.presentation.screens.home.openGoogleMaps

private val RedSoft = Color(0xFFE60023)
private val BackgroundColor = Color(0xFFF5F5F5)

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetallesScreen(navController: NavController, garageId: String) {

    val context = LocalContext.current
    val garageRepo = remember { GarageRepositoryImpl(supabase) }
    val empleadoRepo = remember { EmpleadoGarageRepositoryImpl(supabase) }

    var garage by remember { mutableStateOf<Garage?>(null) }
    var stats by remember { mutableStateOf<Stats?>(null) }
    var loading by remember { mutableStateOf(true) }

    // Cargar Garage + Stats
    LaunchedEffect(garageId) {
        loading = true
        try {
            garage = garageRepo.getGarageById(garageId)
            stats = empleadoRepo.getStats(garageId)
        } finally {
            loading = false
        }
    }

    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.detalles_del_garage),
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color(0xFF212121)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundColor
                )
            )
        }
    ) { padding ->

        // Loader
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

        // Error: no existe garage
        if (garage == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        stringResource(R.string.garage_no_encontrado),
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { navController.navigateUp() },
                        colors = ButtonDefaults.buttonColors(containerColor = RedSoft)
                    ) {
                        Text(stringResource(R.string.volver))
                    }
                }
            }
            return@Scaffold
        }

        val g = garage!!
        val espaciosLibres = stats?.espaciosLibres ?: 0

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundColor)
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {

            // Imagen
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(g.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = g.nombre,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {

                // Título y dirección
                Text(
                    text = g.nombre ?: stringResource(R.string.sin_nombre),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121)
                )

                Spacer(Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = g.direccion ?: stringResource(R.string.sin_direcci_n),
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }

                Spacer(Modifier.height(20.dp))

                // 🔵 Espacios Disponibles
                EspaciosDisponiblesCard(espacios = espaciosLibres)

                Spacer(Modifier.height(16.dp))

                // Horario
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                stringResource(R.string.horario),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF212121)
                            )
                            Text(
                                g.horario ?: stringResource(R.string.no_especificado),
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Botones de acción
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Reservar
                    Button(
                        onClick = {
                            navController.navigate("registrarReserva/${g.idGarage}")
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = RedSoft),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.reservar),
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Button(
                        onClick = {
                            navController.navigate(
                                Routes.Subscription.createRoute(g.idGarage)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D47A1))
                    ) {
                        Text(stringResource(R.string.suscribirse), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Ir al garage (Google Maps)
                Button(
                    onClick = {
                        val lat = g.latitud ?: return@Button
                        val lng = g.longitud ?: return@Button
                        openGoogleMaps(context, lat, lng)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1976D2)
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Icon(Icons.Default.Map, contentDescription = "map", tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.ir_al_garage), color = Color.White, fontWeight = FontWeight.Medium)
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun EspaciosDisponiblesCard(espacios: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
        elevation = CardDefaults.cardElevation(3.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column {
                Text(
                    stringResource(R.string.espacios_disponibles),
                    color = Color(0xFF0D47A1),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    espacios.toString(),
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1565C0)
                )
            }

            Surface(
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                color = Color(0xFF2196F3).copy(alpha = 0.15f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.LocalParking,
                        contentDescription = null,
                        tint = Color(0xFF1565C0),
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
        }
    }
}
