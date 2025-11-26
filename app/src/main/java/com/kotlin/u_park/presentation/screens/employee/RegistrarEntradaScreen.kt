package com.kotlin.u_park.presentation.screens.employee

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.kotlin.u_park.presentation.screens.parking.ParkingViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RegistrarEntradaScreen(
    navController: NavController,
    viewModel: ParkingViewModel,
    garageId: String,
    empleadoId: String
) {
    val ctx = LocalContext.current

    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var placa by remember { mutableStateOf("") }

    val isLoading by viewModel.isLoading.collectAsState()
    val ticket by viewModel.ticket.collectAsState()
    val message by viewModel.message.collectAsState()

    // Cuando ya tengamos un ticket, navegar
    LaunchedEffect(ticket) {
        ticket?.let {
            navController.navigate("ticket/${it.parkingId}")
        }
    }

    // Manejar mensajes de error o éxito
    LaunchedEffect(message) {
        message?.let {
            Toast.makeText(ctx, it, Toast.LENGTH_SHORT).show()
        }
    }

    // ---------- Cámara ----------
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(),
        onResult = { result -> bitmap = result }
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) cameraLauncher.launch(null)
            else Toast.makeText(ctx, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text("Registrar Entrada", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = placa,
            onValueChange = { placa = it },
            label = { Text("Placa del vehículo") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {

            // Botón tomar foto
            Button(onClick = {
                val permission = Manifest.permission.CAMERA
                val granted = ContextCompat.checkSelfPermission(ctx, permission) == PackageManager.PERMISSION_GRANTED
                if (!granted) permissionLauncher.launch(permission)
                else cameraLauncher.launch(null)
            }) {
                Icon(Icons.Default.CameraAlt, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Tomar Foto")
            }

            // Botón confirmar entrada
            Button(
                onClick = {
                    if (placa.isBlank()) {
                        Toast.makeText(ctx, "Ingresa la placa", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (bitmap == null) {
                        Toast.makeText(ctx, "Toma al menos 1 foto", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val fotoBytes = listOf(bitmap!!.toByteArray())

                    viewModel.registrarEntrada(
                        garageId = garageId,
                        vehicleId = placa.trim(),
                        empleadoId = empleadoId,
                        fotosBytes = fotoBytes
                    )
                }
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isLoading) "Cargando..." else "Confirmar Entrada")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        bitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.size(260.dp)
            )
        }
    }
}

fun Bitmap.toByteArray(): ByteArray {
    val stream = java.io.ByteArrayOutputStream()
    this.compress(Bitmap.CompressFormat.JPEG, 90, stream)
    return stream.toByteArray()
}

