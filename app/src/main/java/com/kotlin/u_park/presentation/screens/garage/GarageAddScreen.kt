package com.kotlin.u_park.presentation.screens.garage

import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.kotlin.u_park.domain.model.Garage
import kotlinx.coroutines.launch
import java.io.File
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GarageAddScreen(
    userId: String?,
    viewModel: GarageViewModel,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    var nombre by rememberSaveable { mutableStateOf("") }
    var direccion by rememberSaveable { mutableStateOf("") }
    var capacidad by rememberSaveable { mutableStateOf("") }
    var horario by rememberSaveable { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val isLoading by viewModel.isLoading.collectAsState()
    val isSuccess by viewModel.isSuccess.collectAsState()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> imageUri = uri }

    // Acción cuando se guarda correctamente
    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            viewModel.resetStatus()
            onSuccess()
            onDismiss()
        }
    }

    ModalBottomSheet(
        onDismissRequest = { onDismiss() },
        sheetState = sheetState,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(40.dp)
                    .height(5.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Agregar nuevo garaje",
                style = MaterialTheme.typography.titleLarge
            )

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre del garaje") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = direccion,
                onValueChange = { direccion = it },
                label = { Text("Dirección") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = capacidad,
                onValueChange = { capacidad = it },
                label = { Text("Capacidad total") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = horario,
                onValueChange = { horario = it },
                label = { Text("Horario (ej: 8am - 8pm)") },
                modifier = Modifier.fillMaxWidth()
            )

            imageUri?.let {
                Image(
                    painter = rememberAsyncImagePainter(it),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentScale = ContentScale.Crop
                )
            }

            Button(
                onClick = { imagePickerLauncher.launch("image/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Seleccionar imagen")
            }

            Button(
                onClick = {
                    if (userId.isNullOrEmpty()) return@Button
                    coroutineScope.launch {
                        var imageFile: File? = null
                        imageUri?.let { uri ->
                            val inputStream = context.contentResolver.openInputStream(uri)
                            val tempFile = File.createTempFile("garage_", ".jpg", context.cacheDir)
                            tempFile.outputStream().use { output -> inputStream?.copyTo(output) }
                            imageFile = tempFile
                        }

                        val newGarage = Garage(
                            idGarage = UUID.randomUUID().toString(),
                            nombre = nombre,
                            direccion = direccion,
                            latitud = 0.0,
                            longitud = 0.0,
                            capacidadTotal = capacidad.toIntOrNull() ?: 0,
                            horario = horario,
                            imageUrl = null,
                            isActive = true,
                            userId = userId
                        )

                        viewModel.addGarage(newGarage, imageFile)
                    }
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isLoading) "Guardando..." else "Guardar garaje")
            }

            if (!isLoading && !isSuccess) {
                Text(
                    "Error al guardar. Intenta nuevamente.",
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
