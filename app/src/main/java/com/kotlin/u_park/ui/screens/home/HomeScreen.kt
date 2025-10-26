package com.kotlin.u_park.ui.screens.home

import android.annotation.SuppressLint
import androidx.compose.animation.animateColorAsState
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
import kotlinx.coroutines.launch
import com.kotlin.u_park.ui.components.GarageDetailBottomSheet
import androidx.compose.material3.ExperimentalMaterial3Api
import io.github.jan.supabase.postgrest.postgrest

// 🎨 Colores principales
private val RedSoft = Color(0xFFE60023)
private val BackgroundColor = Color(0xFFF5F5F5)

suspend fun fetchGarages(): List<Garage> {
    return try {
        val response = supabase.postgrest["garages"].select()
        response.decodeAs<List<Garage>>()
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
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

    // Estado para el Bottom Sheet
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedGarage by remember { mutableStateOf<Garage?>(null) }

    LaunchedEffect(Unit) {
        authViewModel.restoreCurrentUser()
        coroutineScope.launch {
            garages = fetchGarages()
        }
    }

    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            TopAppBar(
                title = { Text("U-Park", fontWeight = FontWeight.Bold, color = RedSoft) },
                actions = {
                    IconButton(onClick = { }) {
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
                .fillMaxSize()
                .background(BackgroundColor)
                .padding(padding)
        ) {
            // 🔍 Buscador
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar un garaje", color = Color.Gray) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Buscar", tint = Color.Gray)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White)
                    .padding(horizontal = 8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent
                ),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 📋 Lista de Garages
            LazyColumn(
                state = listState,
                verticalArrangement = Arrangement.spacedBy(20.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .background(BackgroundColor)
            ) {
                items(garages) { garage ->
                    GarageCard(
                        garage = garage,
                        onClick = {
                            selectedGarage = garage
                            coroutineScope.launch { sheetState.show() }
                        }
                    )
                }
            }
        }
    }

    // 📄 Bottom Sheet
    if (selectedGarage != null) {
        ModalBottomSheet(
            onDismissRequest = { selectedGarage = null },
            sheetState = sheetState,
        ) {
            GarageDetailBottomSheet(
                garage = selectedGarage!!,
                onDismiss = { selectedGarage = null },
                onReserve = { /* Acción reservar */ },
                onDetails = { /* Acción ver detalles */ }
            )
        }
    }
}

// 🖼️ Card estilo Airbnb
@Composable
fun GarageCard(
    garage: Garage,
    onClick: () -> Unit
) {
    var isFavorite by remember { mutableStateOf(false) }
    val heartColor by animateColorAsState(
        targetValue = if (isFavorite) RedSoft else Color.Black,
        label = "HeartColorAnimation"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(BackgroundColor)
            .clickable { onClick() }
    ) {
        // Imagen destacada
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(garage.image_url)
                .crossfade(true)
                .build(),
            contentDescription = garage.nombre,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(20.dp))
        )

        // ❤️ Botón de corazón flotante
        IconButton(
            onClick = { isFavorite = !isFavorite },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
                .size(32.dp)
                .background(Color.White.copy(alpha = 0.7f), shape = RoundedCornerShape(50))
        ) {
            Icon(
                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = "Favorito",
                tint = heartColor
            )
        }

        // 📍 Información del garaje (debajo de la imagen)
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .background(Color.Transparent)
                .padding(top = 230.dp, start = 8.dp, end = 8.dp, bottom = 8.dp)
        ) {
            Text(
                text = garage.nombre,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = Color.Black
            )
            Text(
                text = "Capacidad: ${garage.capacidad_total}",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}
