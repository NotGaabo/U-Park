package com.kotlin.u_park.ui.screens.home

import android.location.Geocoder
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.kotlin.u_park.domain.model.Garage
import com.kotlin.u_park.data.remote.supabase
import com.kotlin.u_park.data.repository.AuthViewModel
import com.kotlin.u_park.ui.components.GarageDetailBottomSheet
import com.kotlin.u_park.utils.LocationHelper
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.*

// 🎨 Colores principales
private val RedSoft = Color(0xFFE60023)
private val BackgroundColor = Color(0xFFF5F5F5)

// 📡 Obtener garages desde Supabase
suspend fun fetchGarages(): List<Garage> {
    return try {
        val response = supabase.postgrest["garages"].select()
        response.decodeAs<List<Garage>>()
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }
}

// 📏 Calcular distancia entre dos puntos (Haversine)
fun distanceInKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val R = 6371.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2).pow(2.0) + cos(Math.toRadians(lat1)) *
            cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2.0)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return R * c
}

// 📍 Obtener dirección corta desde coordenadas
fun getAddressFromLocationShort(
    context: android.content.Context,
    lat: Double,
    lng: Double
): String? {
    return try {
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses = geocoder.getFromLocation(lat, lng, 1)
        if (!addresses.isNullOrEmpty()) {
            val street = addresses[0].thoroughfare
            val sublocality = addresses[0].subLocality
            when {
                street != null && sublocality != null -> "$street, $sublocality"
                street != null -> street
                else -> sublocality
            }
        } else null
    } catch (e: Exception) {
        null
    }
}

// 👉 Formatear distancia para interfaz
fun formatDistanceForUi(distanceKm: Double): String {
    return if (distanceKm < 1) {
        "${(distanceKm * 1000).toInt()} m"
    } else {
        String.format("%.1f km", distanceKm)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    var garages by remember { mutableStateOf<List<Garage>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedGarage by remember { mutableStateOf<Garage?>(null) }
    val context = LocalContext.current
    var userLocation by remember { mutableStateOf<Pair<Double, Double>?>(null) }

    LaunchedEffect(Unit) {
        authViewModel.restoreCurrentUser()
        coroutineScope.launch {
            val location = LocationHelper.getCurrentLocation(context)
            userLocation = location

            val allGarages = fetchGarages()
            garages = if (location != null) {
                val (lat, lng) = location
                allGarages.filter {
                    it.latitud != null && it.longitud != null &&
                            distanceInKm(lat, lng, it.latitud, it.longitud) <= 1.0
                }.sortedBy {
                    distanceInKm(lat, lng, it.latitud!!, it.longitud!!)
                }
            } else allGarages
        }
    }

    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            TopAppBar(
                title = { Text("U-Park", fontWeight = FontWeight.Bold, color = RedSoft) },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Notifications, "Notificaciones", tint = Color.Black)
                    }
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(Icons.Default.Settings, "Configuración", tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundColor)
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(
                    selected = true,
                    onClick = {},
                    icon = { Icon(Icons.Default.Home, "Home", tint = RedSoft) },
                    label = { Text("Home", color = RedSoft) }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = {},
                    icon = { Icon(Icons.Default.AddCircle, "Agregar") },
                    label = { Text("Agregar") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = {},
                    icon = { Icon(Icons.Default.History, "Historial") },
                    label = { Text("Historial") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("settings") },
                    icon = { Icon(Icons.Default.Person, "Perfil") },
                    label = { Text("Perfil") }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .background(BackgroundColor)
                .padding(padding)
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar un garaje", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Search, "", tint = Color.Gray) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                )
            )

            LazyColumn(
                state = listState,
                verticalArrangement = Arrangement.spacedBy(20.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                val filteredGarages = garages.filter {
                    it.nombre.contains(searchQuery, ignoreCase = true)
                }

                items(filteredGarages) { garage ->
                    GarageCard(
                        garage = garage,
                        userLat = userLocation?.first,
                        userLng = userLocation?.second,
                        onClick = {
                            selectedGarage = garage
                            coroutineScope.launch { sheetState.show() }
                        }
                    )
                }
            }
        }
    }

    if (selectedGarage != null) {
        ModalBottomSheet(
            onDismissRequest = { selectedGarage = null },
            sheetState = sheetState,
        ) {
            GarageDetailBottomSheet(
                garage = selectedGarage!!,
                onDismiss = { selectedGarage = null },
                onReserve = { },
                onDetails = { }
            )
        }
    }
}

@Composable
fun GarageCard(
    garage: Garage,
    userLat: Double?,
    userLng: Double?,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    var isFavorite by remember { mutableStateOf(false) }

    val distanceKm: Double? = remember(userLat, userLng, garage.latitud, garage.longitud) {
        if (userLat != null && userLng != null && garage.latitud != null && garage.longitud != null)
            distanceInKm(userLat, userLng, garage.latitud, garage.longitud)
        else null
    }
    val distanceText = distanceKm?.let { formatDistanceForUi(it) } ?: ""

    val address by produceState<String?>(initialValue = null, garage.latitud, garage.longitud) {
        value = if (garage.latitud != null && garage.longitud != null) {
            getAddressFromLocationShort(context, garage.latitud, garage.longitud)
        } else null
    }

    val locationLine = when {
        !address.isNullOrEmpty() && distanceText.isNotBlank() -> "📍 $address • $distanceText"
        !address.isNullOrEmpty() -> "📍 $address"
        distanceText.isNotBlank() -> "📍 $distanceText"
        else -> "📍 Ubicación desconocida"
    }

    var pressed by remember { mutableStateOf(false) }
    val scaleAnim by animateFloatAsState(if (pressed) 0.98f else 1f, tween(120))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scaleAnim)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Box(modifier = Modifier.height(220.dp)) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(garage.image_url)
                    .crossfade(true)
                    .build(),
                contentDescription = garage.nombre,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
            ) {
                IconButton(onClick = { isFavorite = !isFavorite }) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) Color.Red else Color.White
                    )
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Text(garage.nombre, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Text(locationLine, color = Color(0xFFEEEEEE), fontSize = 13.sp)
                Spacer(Modifier.height(8.dp))
                Surface(
                    color = Color(0x33FFFFFF), shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        text = "🅿️ ${garage.capacidad_total ?: 0} espacios",
                        color = Color.White,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
