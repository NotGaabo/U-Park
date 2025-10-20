package com.kotlin.u_park.ui.screens.home
import android.net.Uri
import androidx.compose.foundation.background
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
import com.kotlin.u_park.data.repository.AuthRepository
import com.kotlin.u_park.data.remote.supabase
import io.github.jan.supabase.postgrest.postgrest

suspend fun fetchGarages(): List<Garage> {
    return try {
        val response = supabase.postgrest["garages"]
            .select()
        response.decodeAs<List<Garage>>()
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController) {

    var garages by remember { mutableStateOf<List<Garage>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        garages = fetchGarages()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("U-Park", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { }) { Icon(Icons.Default.Notifications, null) }
                    IconButton(onClick = { }) { Icon(Icons.Default.Settings, null) }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(icon = { Icon(Icons.Default.DirectionsCar, null) }, label = { Text("Home") }, selected = true, onClick = { })
                NavigationBarItem(icon = { Icon(Icons.Default.AddCircle, null) }, label = { Text("Agregar") }, selected = false, onClick = { })
                NavigationBarItem(icon = { Icon(Icons.Default.History, null) }, label = { Text("Historial") }, selected = false, onClick = { })
                NavigationBarItem(icon = { Icon(Icons.Default.Person, null) }, label = { Text("Perfil") }, selected = false, onClick = { })
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Buscar garage") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Garages disponibles",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(garages) { garage ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp))
                                .padding(16.dp)
                        ) {
                            Column {
                                // 🔹 Convierte la URL de la BD en una válida
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

                                Text(text = garage.nombre, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                Text(garage.direccion)
                                Text("Capacidad: ${garage.capacidad_total} vehículos")
                                Text("Horario: ${garage.horario}")

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Button(onClick = { }, modifier = Modifier.weight(1f)) { Text("Detalles") }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(onClick = { }, modifier = Modifier.weight(1f)) { Text("Reservar") }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
