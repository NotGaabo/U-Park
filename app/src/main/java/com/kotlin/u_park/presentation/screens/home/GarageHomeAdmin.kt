package com.kotlin.u_park.presentation.screens.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.kotlin.u_park.domain.model.Garage
import com.kotlin.u_park.presentation.screens.garage.GarageViewModel
import java.io.File
import kotlinx.coroutines.launch
import java.util.UUID

@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("MissingPermission")
@Composable
fun DuenoGarageScreen(
    onSave: () -> Unit,
    viewModel: GarageViewModel,
    userId: String
) {
    val context = LocalContext.current
    val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    var nombre by remember { mutableStateOf("") }
    var capacidad by remember { mutableStateOf("") }
    var latitud by remember { mutableStateOf("") }
    var longitud by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var imageFile by remember { mutableStateOf<File?>(null) }

    val scope = rememberCoroutineScope()

    // 🔹 Selector de imagen
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
        uri?.let {
            val inputStream = context.contentResolver.openInputStream(it)
            val file = File(context.cacheDir, "garage_image.jpg")
            inputStream?.use { input -> file.outputStream().use { output -> input.copyTo(output) } }
            imageFile = file
        }
    }

    // 🔹 Permisos de ubicación
    val locationPermissionGranted = remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted -> locationPermissionGranted.value = granted }

    LaunchedEffect(Unit) {
        when {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> locationPermissionGranted.value = true
            else -> launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // 🔹 Obtener ubicación actual
    LaunchedEffect(locationPermissionGranted.value) {
        if (locationPermissionGranted.value) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    latitud = it.latitude.toString()
                    longitud = it.longitude.toString()
                }
            }
        }
    }

    val isSuccess by viewModel.isSuccess.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Agrega un nuevo garage",
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 🔹 Imagen seleccionada o placeholder
        Box(
            modifier = Modifier
                .size(width = 220.dp, height = 130.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, Color.Black, RoundedCornerShape(12.dp))
                .clickable { imagePicker.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            if (imageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(imageUri),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text("Seleccionar imagen", color = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        GarageField("Nombre", nombre) { nombre = it }
        GarageField("Latitud", latitud) { latitud = it }
        GarageField("Longitud", longitud) { longitud = it }
        GarageField("Capacidad", capacidad) { capacidad = it }

        Spacer(modifier = Modifier.height(25.dp))

        Button(
            onClick = {
                if (nombre.isNotBlank() && latitud.isNotBlank() && longitud.isNotBlank() && capacidad.isNotBlank()) {
                    scope.launch {
                        val newGarage = Garage(
                            idGarage = UUID.randomUUID().toString(),
                            nombre = nombre,
                            direccion = "Dirección no especificada", // puedes agregar un campo editable si quieres
                            latitud = latitud.toDoubleOrNull() ?: 0.0,
                            longitud = longitud.toDoubleOrNull() ?: 0.0,
                            capacidadTotal = capacidad.toIntOrNull() ?: 0,
                            horario = null, // o agrega un campo de horario en el formulario
                            fechaCreacion = null,
                            imageUrl = null,
                            isActive = true,
                            userId = userId
                        )
                        viewModel.addGarage(newGarage, imageFile)
                    }
                }
            },
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE60023)),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
            } else {
                Text("Guardar", color = Color.White, fontSize = 16.sp)
            }
        }


        if (isSuccess == true) {
            LaunchedEffect(Unit) {
                viewModel.resetStatus()
                onSave()
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun GarageField(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth()
    )
}
