package com.kotlin.u_park.presentation.screens.detalles

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.kotlin.u_park.data.repository.GarageRepositoryImpl
import com.kotlin.u_park.data.remote.supabase
import com.kotlin.u_park.domain.model.Garage

@Composable
fun DetallesScreen(navController: NavController, garageId: String) {

    // Repo para cargar el garage
    val repo = remember { GarageRepositoryImpl(supabase) }

    // Estado del garage cargado
    var garage by remember { mutableStateOf<Garage?>(null) }
    var loading by remember { mutableStateOf(true) }

    // Cargar garage desde Supabase
    LaunchedEffect(garageId) {
        loading = true
        try {
            garage = repo.getGarageById(garageId)
        } finally {
            loading = false
        }
    }

    LaunchedEffect(garageId) {
        loading = true
        try {
            garage = repo.getGarageById(garageId)
        } finally {
            loading = false
        }
    }

    // Mostrar loader mientras carga
    if (loading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    // Si no existe
    if (garage == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Text("Garage no encontrado")
        }
        return
    }

    val g = garage!!

    // ---------------- UI ----------------
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        val imageUrl = g.imageUrl?.trim()?.replace("\n", "")

        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = g.nombre,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(12.dp))
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = g.nombre ?: "Sin nombre",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = g.direccion ?: "Sin dirección",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Capacidad: ${g.capacidadTotal}",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Horario: ${g.horario ?: "-"}",
            style = MaterialTheme.typography.bodyMedium
        )

    }
}
