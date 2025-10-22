package com.kotlin.u_park.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import io.github.jan.supabase.postgrest.postgrest

private val RedSoft = Color(0xFFFF4D4D)
private val BackgroundColor = Color.White

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
fun HomeScreen(navController: NavController) {
    var garages by remember { mutableStateOf<List<Garage>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        garages = fetchGarages()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("U-Park", fontWeight = FontWeight.Bold, color = RedSoft) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundColor,
                    titleContentColor = RedSoft,
                    actionIconContentColor = RedSoft
                ),
                actions = {
                    IconButton(onClick = { /* notificaciones */ }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notificaciones")
                    }
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = BackgroundColor) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.DirectionsCar, contentDescription = "Home", tint = RedSoft) },
                    label = { Text("Home", color = RedSoft) },
                    selected = true,
                    onClick = { }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.AddCircle, contentDescription = "Agregar", tint = RedSoft) },
                    label = { Text("Agregar", color = RedSoft) },
                    selected = false,
                    onClick = { }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.History, contentDescription = "Historial", tint = RedSoft) },
                    label = { Text("Historial", color = RedSoft) },
                    selected = false,
                    onClick = { }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Perfil", tint = RedSoft) },
                    label = { Text("Perfil", color = RedSoft) },
                    selected = false,
                    onClick = { navController.navigate("settings") }
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(BackgroundColor)
                .padding(16.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()
            ) {
                // SEARCH FIELD
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(RedSoft.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                    label = { Text("Buscar garage", color = Color.Black) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = RedSoft)
                    },
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Garages disponibles",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = RedSoft,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(garages) { garage ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .border(
                                    width = 1.dp,
                                    color = RedSoft.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                        ) {
                            Column(modifier = Modifier.background(BackgroundColor).padding(16.dp)) {
                                val imageUrl = garage.image_url?.trim()?.replace("\n", "")
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(imageUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = garage.nombre,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = garage.nombre,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                                Text(garage.direccion, color = Color.DarkGray)
                                Text("Capacidad: ${garage.capacidad_total} vehículos", color = Color.DarkGray)
                                Text("Horario: ${garage.horario}", color = Color.DarkGray)

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Button(
                                        onClick = { navController.navigate("detalles") },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = RedSoft)
                                    ) { Text("Detalles", color = Color.White) }

                                    Button(
                                        onClick = { /* Reservar */ },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                                    ) { Text("Reservar", color = Color.White) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
