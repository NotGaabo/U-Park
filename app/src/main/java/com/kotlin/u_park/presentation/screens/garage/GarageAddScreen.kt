package com.kotlin.u_park.presentation.screens.garage

import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import coil.compose.rememberAsyncImagePainter
import com.kotlin.u_park.domain.model.Garage
import kotlinx.coroutines.launch
import java.io.File
import android.location.Geocoder
import androidx.compose.ui.res.stringResource
import com.kotlin.u_park.R
import com.kotlin.u_park.presentation.utils.LocationHelper
import kotlinx.coroutines.launch
import java.util.Locale
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
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)


    var nombre by rememberSaveable { mutableStateOf("") }
    var direccion by rememberSaveable { mutableStateOf("") }
    var capacidad by rememberSaveable { mutableStateOf("") }
    var horario by rememberSaveable { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var latitud by rememberSaveable { mutableStateOf(0.0) }
    var longitud by rememberSaveable { mutableStateOf(0.0) }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            val location = LocationHelper.getCurrentLocation(context)
            location?.let { (lat, lon) ->
                latitud = lat
                longitud = lon

                // Convertir coordenadas a dirección legible
                try {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    val addressList = geocoder.getFromLocation(lat, lon, 1)
                    if (!addressList.isNullOrEmpty()) {
                        direccion = addressList[0].getAddressLine(0) ?: ""
                    } else {
                        direccion = context.getString(R.string.ubicaci_n_desconocida)
                    }
                } catch (e: Exception) {
                    direccion = context.getString(R.string.error_obteniendo_direcci_n)
                }
            } ?: run {
                direccion = context.getString(R.string.no_se_pudo_obtener_ubicaci_n)
            }
        }
    }


    val isLoading by viewModel.isLoading.collectAsState()
    val isSuccess by viewModel.isSuccess.collectAsState()

    val redPrimary = Color(0xFFE60023)
    val backgroundGray = Color(0xFFF5F7FA)

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
        showError = false
    }

    // Validación de campos
    val isFormValid = nombre.isNotBlank() &&
            direccion.isNotBlank() &&
            capacidad.isNotBlank() &&
            capacidad.toIntOrNull() != null &&
            horario.isNotBlank()

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
        containerColor = Color.White,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(50.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.Gray.copy(alpha = 0.3f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.95f)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.nuevo_garage),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2D3436)
                    )
                    Text(
                        text = stringResource(R.string.completa_la_informaci_n),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }

                IconButton(
                    onClick = { onDismiss() },
                    modifier = Modifier
                        .size(40.dp)
                        .background(backgroundGray, RoundedCornerShape(10.dp))
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint = Color(0xFF2D3436)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Selector de imagen
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clickable { imagePickerLauncher.launch("image/*") },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (imageUri != null) Color.Transparent else backgroundGray
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(imageUri),
                            contentDescription = "Imagen del garage",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        // Overlay para cambiar imagen
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                                Text(
                                    stringResource(R.string.cambiar_imagen),
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = Color.Gray.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                stringResource(R.string.agregar_foto_del_garage),
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                            Text(
                                stringResource(R.string.toca_para_seleccionar),
                                color = Color.Gray.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Nombre del garage
            Column {
                Text(
                    stringResource(R.string.nombre_del_garage),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF2D3436)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = nombre,
                    onValueChange = {
                        nombre = it
                        showError = false
                    },
                    placeholder = { Text(stringResource(R.string.ej_garage_central), color = Color.Gray.copy(alpha = 0.6f)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = redPrimary,
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = backgroundGray
                    ),
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            Icons.Default.Garage,
                            contentDescription = null,
                            tint = if (nombre.isNotBlank()) redPrimary else Color.Gray
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dirección
            Column {
                Text(
                    stringResource(R.string.direcci_n),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF2D3436)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = direccion,
                    onValueChange = { }, // No editable
                    placeholder = { Text(stringResource(R.string.cargando_ubicaci_n), color = Color.Gray.copy(alpha = 0.6f)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    enabled = false, // 👈 Solo lectura
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = Color.Black,
                        disabledContainerColor = backgroundGray,
                        disabledBorderColor = Color.Gray.copy(alpha = 0.3f),
                        disabledPlaceholderColor = Color.Gray.copy(alpha = 0.5f)
                    ),
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color.Gray
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Capacidad y Horario en fila
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Capacidad
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.capacidad),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF2D3436)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = capacidad,
                        onValueChange = {
                            if (it.all { char -> char.isDigit() }) {
                                capacidad = it
                                showError = false
                            }
                        },
                        placeholder = { Text(stringResource(R.string._50), color = Color.Gray.copy(alpha = 0.6f)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = redPrimary,
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = backgroundGray
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        leadingIcon = {
                            Icon(
                                Icons.Default.LocalParking,
                                contentDescription = null,
                                tint = if (capacidad.isNotBlank()) redPrimary else Color.Gray
                            )
                        }
                    )
                }

                // Horario
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.horario4),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF2D3436)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = horario,
                        onValueChange = {
                            horario = it
                            showError = false
                        },
                        placeholder = { Text(stringResource(R.string._8am_8pm), color = Color.Gray.copy(alpha = 0.6f)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = redPrimary,
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = backgroundGray
                        ),
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = null,
                                tint = if (horario.isNotBlank()) redPrimary else Color.Gray
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Mensaje de error
            if (showError) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFEBEE)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = Color(0xFFD32F2F),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            errorMessage,
                            color = Color(0xFFD32F2F),
                            fontSize = 14.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Botón guardar
            Button(
                onClick = {
                    if (!isFormValid) {
                        showError = true
                        errorMessage = "Por favor completa todos los campos correctamente"
                        return@Button
                    }

                    if (userId.isNullOrEmpty()) {
                        showError = true
                        errorMessage = "Error de sesión. Inicia sesión nuevamente"
                        return@Button
                    }

                    coroutineScope.launch {
                        var imageFile: File? = null
                        imageUri?.let { uri ->
                            try {
                                val inputStream = context.contentResolver.openInputStream(uri)
                                val tempFile = File.createTempFile("garage_", ".jpg", context.cacheDir)
                                tempFile.outputStream().use { output -> inputStream?.copyTo(output) }
                                imageFile = tempFile
                            } catch (e: Exception) {
                                showError = true
                                errorMessage = "Error al cargar la imagen"
                                return@launch
                            }
                        }

                        val newGarage = Garage(
                            idGarage = UUID.randomUUID().toString(),
                            nombre = nombre,
                            direccion = direccion,
                            latitud = latitud,
                            longitud = longitud,
                            capacidadTotal = capacidad.toIntOrNull() ?: 0,
                            horario = horario,
                            imageUrl = null,
                            isActive = true,
                            userId = userId
                        )

                        viewModel.addGarage(newGarage, imageFile)
                    }
                },
                enabled = !isLoading && isFormValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = redPrimary,
                    disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        stringResource(R.string.guardando),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                } else {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.guardar_garage),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}