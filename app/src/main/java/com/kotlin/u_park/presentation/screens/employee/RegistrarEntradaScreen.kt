package com.kotlin.u_park.presentation.screens.employee

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.kotlin.u_park.domain.model.ReservaConUsuario
import com.kotlin.u_park.presentation.navigation.Routes
import com.kotlin.u_park.presentation.screens.parking.ParkingViewModel

private val PrimaryRed = Color(0xFFE60023)
private val BackgroundGray = Color(0xFFF8F9FA)
private val CardWhite = Color(0xFFFFFFFF)
private val TextPrimary = Color(0xFF1A1A1A)
private val TextSecondary = Color(0xFF6C757D)
private val SuccessGreen = Color(0xFF4CAF50)
private val WarningOrange = Color(0xFFFF9800)

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrarEntradaScreen(
    navController: NavController,
    viewModel: ParkingViewModel,
    garageId: String,
    empleadoId: String
) {
    val ctx = LocalContext.current
    val scrollState = rememberScrollState()

    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var placa by remember { mutableStateOf("") }
    var modoEntrada by remember { mutableStateOf("Normal") }
    var expandedTipo by remember { mutableStateOf(false) }
    var expandedReservas by remember { mutableStateOf(false) }

    val reservas by viewModel.reservasConUsuario.collectAsState()
    var reservaSeleccionada by remember { mutableStateOf<ReservaConUsuario?>(null) }

    val isLoading by viewModel.isLoading.collectAsState()
    val ticket by viewModel.ticket.collectAsState()
    val message by viewModel.message.collectAsState()

    // Cargar reservas cuando se selecciona modo Reserva
    LaunchedEffect(modoEntrada) {
        if (modoEntrada == "Reserva") {
            viewModel.loadReservasConUsuario(garageId)
        }
    }

    LaunchedEffect(ticket) {
        ticket?.let {
            // 👉 Regresar al Home del empleado
            navController.navigate(Routes.EmployeeHome.route) {
                popUpTo(Routes.EmployeeHome.route) { inclusive = true }
                launchSingleTop = true
            }

            // 👉 Limpiar para no volver a navegar accidentalmente
            viewModel.resetTicket()
        }
    }


    LaunchedEffect(message) {
        message?.let { Toast.makeText(ctx, it, Toast.LENGTH_SHORT).show() }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { result -> bitmap = result }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) cameraLauncher.launch(null)
        else Toast.makeText(ctx, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Registrar Entrada",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Text(
                            "Gestión de acceso vehicular",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryRed,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundGray)
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Paso 1: Tipo de Entrada
                StepCard(
                    stepNumber = "1",
                    title = "Tipo de Entrada",
                    isCompleted = modoEntrada.isNotEmpty()
                ) {
                    TipoEntradaSelector(
                        modoEntrada = modoEntrada,
                        expanded = expandedTipo,
                        onExpandChange = { expandedTipo = it },
                        onTipoSelected = { tipo ->
                            modoEntrada = tipo
                            if (tipo == "Normal") {
                                reservaSeleccionada = null
                                placa = ""
                            }
                            expandedTipo = false
                        }
                    )
                }

                // Paso 2: Selección de Reserva (si aplica)
                AnimatedVisibility(
                    visible = modoEntrada == "Reserva",
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    StepCard(
                        stepNumber = "2",
                        title = "Seleccionar Reserva",
                        isCompleted = reservaSeleccionada != null
                    ) {
                        ReservaSelector(
                            reservas = reservas,
                            reservaSeleccionada = reservaSeleccionada,
                            expanded = expandedReservas,
                            onExpandChange = { expandedReservas = it },
                            onReservaSelected = { reserva ->
                                reservaSeleccionada = reserva
                                placa = reserva.vehicles?.plate ?: ""
                                expandedReservas = false
                            }
                        )
                    }
                }

                // Paso 3: Placa del Vehículo
                StepCard(
                    stepNumber = if (modoEntrada == "Reserva") "3" else "2",
                    title = "Placa del Vehículo",
                    isCompleted = placa.isNotBlank()
                ) {
                    PlacaInput(
                        placa = placa,
                        onPlacaChange = { placa = it },
                        enabled = modoEntrada == "Normal"
                    )
                }

                // Paso 4: Fotografía
                StepCard(
                    stepNumber = if (modoEntrada == "Reserva") "4" else "3",
                    title = "Fotografía del Vehículo",
                    isCompleted = bitmap != null
                ) {
                    FotoSection(
                        bitmap = bitmap,
                        onTakePhoto = {
                            val granted = ContextCompat.checkSelfPermission(
                                ctx, Manifest.permission.CAMERA
                            ) == PackageManager.PERMISSION_GRANTED

                            if (!granted) permissionLauncher.launch(Manifest.permission.CAMERA)
                            else cameraLauncher.launch(null)
                        }
                    )
                }

                Spacer(Modifier.height(8.dp))

                // Botón de Confirmación
                ConfirmButton(
                    isLoading = isLoading,
                    enabled = placa.isNotBlank() && bitmap != null &&
                            (modoEntrada == "Normal" || reservaSeleccionada != null),
                    onClick = {
                        if (placa.isBlank()) {
                            Toast.makeText(ctx, "Ingresa la placa", Toast.LENGTH_SHORT).show()
                            return@ConfirmButton
                        }

                        if (bitmap == null) {
                            Toast.makeText(ctx, "Debe tomar una foto", Toast.LENGTH_SHORT).show()
                            return@ConfirmButton
                        }

                        val fotos = listOf(bitmap!!.toByteArray())

                        if (modoEntrada == "Normal") {
                            viewModel.registrarEntrada(
                                garageId = garageId,
                                vehiclePlate = placa.trim(),
                                empleadoId = empleadoId,
                                fotosBytes = fotos
                            )
                        } else {
                            if (reservaSeleccionada == null) {
                                Toast.makeText(ctx, "Selecciona una reserva", Toast.LENGTH_SHORT).show()
                                return@ConfirmButton
                            }

                            viewModel.registrarEntradaDesdeReserva(
                                reserva = reservaSeleccionada!!,
                                fotosBytes = fotos,
                                empleadoId = empleadoId
                            )
                        }
                    }
                )

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun StepCard(
    stepNumber: String,
    title: String,
    isCompleted: Boolean,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = if (isCompleted) SuccessGreen else PrimaryRed.copy(alpha = 0.15f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            stepNumber,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = PrimaryRed
                        )
                    }
                }

                Spacer(Modifier.width(12.dp))

                Text(
                    title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            content()
        }
    }
}

@Composable
fun TipoEntradaSelector(
    modoEntrada: String,
    expanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    onTipoSelected: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SelectableCard(
            title = "Entrada Normal",
            subtitle = "Sin reserva previa",
            icon = Icons.Default.DirectionsCar,
            isSelected = modoEntrada == "Normal",
            onClick = { onTipoSelected("Normal") }
        )

        SelectableCard(
            title = "Entrada por Reserva",
            subtitle = "Con reserva confirmada",
            icon = Icons.Default.EventAvailable,
            isSelected = modoEntrada == "Reserva",
            onClick = { onTipoSelected("Reserva") }
        )
    }
}

@Composable
fun SelectableCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isSelected) Modifier.border(
                    2.dp,
                    PrimaryRed,
                    RoundedCornerShape(12.dp)
                ) else Modifier
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) PrimaryRed.copy(alpha = 0.05f) else Color(0xFFF8F9FA)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = if (isSelected) PrimaryRed.copy(alpha = 0.15f) else Color(0xFFE9ECEF)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = if (isSelected) PrimaryRed else TextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) PrimaryRed else TextPrimary
                )
                Text(
                    subtitle,
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }

            if (isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = PrimaryRed,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservaSelector(
    reservas: List<ReservaConUsuario>,
    reservaSeleccionada: ReservaConUsuario?,
    expanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    onReservaSelected: (ReservaConUsuario) -> Unit
) {
    Column {
        if (reservas.isEmpty()) {
            EmptyReservasCard()
        } else {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = onExpandChange
            ) {
                OutlinedTextField(
                    value = reservaSeleccionada?.let {
                        "${it.vehicles?.users?.nombre ?: "Usuario"} - ${it.vehicles?.plate ?: "Placa"}"
                    } ?: "",
                    onValueChange = {},
                    readOnly = true,
                    placeholder = { Text("Selecciona una reserva", color = TextSecondary) },
                    trailingIcon = {
                        Icon(
                            if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = PrimaryRed
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryRed,
                        unfocusedBorderColor = Color(0xFFDEE2E6),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { onExpandChange(false) }
                ) {
                    reservas.forEach { reserva ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(
                                        "${reserva.vehicles?.users?.nombre} - ${reserva.vehicles?.plate}",
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        "Hora: ${reserva.hora_reserva}",
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                }
                            },
                            onClick = { onReservaSelected(reserva) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = PrimaryRed
                                )
                            }
                        )
                    }
                }
            }

            Text(
                "${reservas.size} reserva${if (reservas.size != 1) "s" else ""} disponible${if (reservas.size != 1) "s" else ""}",
                fontSize = 12.sp,
                color = TextSecondary,
                modifier = Modifier.padding(top = 8.dp, start = 4.dp)
            )
        }
    }
}

@Composable
fun EmptyReservasCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = WarningOrange.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.EventBusy,
                contentDescription = null,
                tint = WarningOrange,
                modifier = Modifier.size(48.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "No hay reservas disponibles",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = WarningOrange,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "No se encontraron reservas activas para este estacionamiento",
                fontSize = 13.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlacaInput(
    placa: String,
    onPlacaChange: (String) -> Unit,
    enabled: Boolean
) {
    OutlinedTextField(
        value = placa,
        onValueChange = onPlacaChange,
        label = { Text("Placa del vehículo") },
        placeholder = { Text("Ej: ABC-1234", color = TextSecondary) },
        leadingIcon = {
            Icon(
                Icons.Default.DirectionsCar,
                contentDescription = null,
                tint = PrimaryRed
            )
        },
        enabled = enabled,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryRed,
            unfocusedBorderColor = Color(0xFFDEE2E6),
            disabledBorderColor = Color(0xFFDEE2E6),
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            disabledContainerColor = Color(0xFFF8F9FA)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    )

    if (!enabled) {
        Text(
            "La placa se completará automáticamente desde la reserva",
            fontSize = 12.sp,
            color = TextSecondary,
            modifier = Modifier.padding(top = 8.dp, start = 4.dp)
        )
    }
}

@Composable
fun FotoSection(
    bitmap: Bitmap?,
    onTakePhoto: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (bitmap != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Foto del vehículo",
                        modifier = Modifier.fillMaxSize()
                    )

                    IconButton(
                        onClick = onTakePhoto,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp)
                            .background(PrimaryRed, CircleShape)
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Retomar foto",
                            tint = Color.White
                        )
                    }
                }
            }
        } else {
            Card(
                onClick = onTakePhoto,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Surface(
                        modifier = Modifier.size(72.dp),
                        shape = CircleShape,
                        color = PrimaryRed.copy(alpha = 0.15f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = PrimaryRed,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Text(
                        "Toca para tomar foto",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )

                    Text(
                        "Captura el vehículo completo",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun ConfirmButton(
    isLoading: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = PrimaryRed,
            disabledContainerColor = Color(0xFFDEE2E6)
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
            Spacer(Modifier.width(12.dp))
            Text("Procesando...", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        } else {
            Icon(Icons.Default.Check, contentDescription = null)
            Spacer(Modifier.width(12.dp))
            Text("Confirmar Entrada", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

fun Bitmap.toByteArray(): ByteArray {
    val stream = java.io.ByteArrayOutputStream()
    this.compress(Bitmap.CompressFormat.JPEG, 90, stream)
    return stream.toByteArray()
}