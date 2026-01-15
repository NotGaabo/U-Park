package com.kotlin.u_park.presentation.screens.employee

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import com.kotlin.u_park.R
import com.kotlin.u_park.domain.model.Rate
import com.kotlin.u_park.domain.model.ReservaConUsuario
import com.kotlin.u_park.presentation.navigation.Routes
import com.kotlin.u_park.presentation.screens.parking.ParkingViewModel
import java.io.File
import android.content.Context
import androidx.compose.runtime.saveable.rememberSaveable

private val PrimaryRed = Color(0xFFE60023)
private val BackgroundGray = Color(0xFFF8F9FA)
private val CardWhite = Color(0xFFFFFFFF)
private val TextPrimary = Color(0xFF1A1A1A)
private val TextSecondary = Color(0xFF6C757D)
private val SuccessGreen = Color(0xFF4CAF50)
private val WarningOrange = Color(0xFFFF9800)

fun createImageFile2(context: Context, tipo: TipoFoto): File {
    val baseDir = File(context.cacheDir, "camera")
    if (!baseDir.exists()) baseDir.mkdirs()

    val prefix = when (tipo) {
        TipoFoto.VEHICULO -> "vehiculo"
        TipoFoto.COMPROBANTE -> "comprobante"
    }

    val file = File(baseDir, "${prefix}_${System.currentTimeMillis()}.jpg")
    file.createNewFile()
    return file
}

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

    val rates by viewModel.rates.collectAsState()
    val selectedRate by viewModel.selectedRate.collectAsState()

    var fotosEntradaPaths by rememberSaveable { mutableStateOf<List<String>>(emptyList()) }

    val fotosEntrada = remember(fotosEntradaPaths) {
        fotosEntradaPaths.mapNotNull { path ->
            try {
                BitmapFactory.decodeFile(path)
            } catch (e: Exception) {
                null
            }
        }
    }

    var placa by rememberSaveable { mutableStateOf("") }
    var modoEntrada by rememberSaveable { mutableStateOf("Normal") }
    var expandedTipo by remember { mutableStateOf(false) }
    var expandedReservas by remember { mutableStateOf(false) }

    val reservas by viewModel.reservasConUsuario.collectAsState()
    var reservaSeleccionada by remember { mutableStateOf<ReservaConUsuario?>(null) }

    val isLoading by viewModel.isLoading.collectAsState()
    val ticket by viewModel.ticket.collectAsState()
    val message by viewModel.message.collectAsState()

    LaunchedEffect(modoEntrada, reservaSeleccionada) {
        if (modoEntrada == "Reserva") {
            viewModel.loadReservasConUsuario(garageId)
            placa = reservaSeleccionada?.vehicles?.plate ?: placa
        }
    }

    LaunchedEffect(garageId) {
        viewModel.loadRatesByGarage(garageId)
    }

    LaunchedEffect(ticket) {
        ticket?.let {
            navController.navigate(Routes.EmployeeHome.route) {
                popUpTo(Routes.EmployeeHome.route) { inclusive = true }
                launchSingleTop = true
            }

            viewModel.resetTicket()
        }
    }

    LaunchedEffect(message) {
        message?.let { Toast.makeText(ctx, it, Toast.LENGTH_SHORT).show() }
    }

    val imageFile = remember { mutableStateOf<File?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            imageFile.value?.let { file ->
                try {
                    if (file.exists() && file.length() > 0) {
                        // Guardar el path en lugar del bitmap
                        fotosEntradaPaths = fotosEntradaPaths + file.absolutePath
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val file = createImageFile2(ctx, TipoFoto.VEHICULO)
            imageFile.value = file
            val uri = FileProvider.getUriForFile(ctx, "${ctx.packageName}.provider", file)
            cameraLauncher.launch(uri)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            stringResource(R.string.registrar_entrada2),
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Text(
                            stringResource(R.string.gesti_n_de_acceso_vehicular),
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
                StepCard(
                    stepNumber = "1",
                    title = stringResource(R.string.tipo_de_entrada),
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

                AnimatedVisibility(
                    visible = modoEntrada == stringResource(R.string.reserva),
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    StepCard(
                        stepNumber = "2",
                        title = stringResource(R.string.seleccionar_reserva),
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

                StepCard(
                    stepNumber = if (modoEntrada == "Reserva") "3" else "2",
                    title = stringResource(R.string.placa_del_veh_culo),
                    isCompleted = placa.isNotBlank()
                ) {
                    PlacaInput(
                        placa = placa,
                        onPlacaChange = { placa = it },
                        enabled = modoEntrada == "Normal"
                    )
                }

                StepCard(
                    stepNumber = if (modoEntrada == "Reserva") "4" else "3",
                    title = stringResource(R.string.seleccionar_tarifa),
                    isCompleted = selectedRate != null
                ) {
                    RateSelector(
                        rates = rates,
                        selectedRate = selectedRate,
                        onRateSelected = { viewModel.selectRate(it) }
                    )
                }

                StepCard(
                    stepNumber = if (modoEntrada == "Reserva") "5" else "4",
                    title = stringResource(R.string.fotograf_a_del_veh_culo),
                    isCompleted = fotosEntrada.isNotEmpty()
                ) {
                    MultipleFotosSection(
                        fotos = fotosEntrada,
                        onTakePhoto = {
                            val permission = Manifest.permission.CAMERA

                            if (ContextCompat.checkSelfPermission(ctx, permission) == PackageManager.PERMISSION_GRANTED) {
                                val file = createImageFile2(ctx, TipoFoto.VEHICULO)
                                imageFile.value = file

                                val uri = FileProvider.getUriForFile(
                                    ctx,
                                    "${ctx.packageName}.provider",
                                    file
                                )

                                cameraLauncher.launch(uri)
                            } else {
                                permissionLauncher.launch(permission)
                            }
                        },
                        onRemovePhoto = { index ->
                            val pathsToKeep = fotosEntradaPaths.filterIndexed { i, _ -> i != index }
                            fotosEntradaPaths = pathsToKeep
                        }
                    )
                }

                Spacer(Modifier.height(8.dp))

                ConfirmButton(
                    isLoading = isLoading,
                    enabled = placa.isNotBlank() && fotosEntrada.isNotEmpty() &&
                            (modoEntrada == "Normal" || reservaSeleccionada != null),
                    onClick = {
                        if (selectedRate == null) {
                            Toast.makeText(ctx, "Selecciona una tarifa", Toast.LENGTH_SHORT).show()
                            return@ConfirmButton
                        }

                        if (placa.isBlank()) {
                            Toast.makeText(ctx, "Ingresa la placa", Toast.LENGTH_SHORT).show()
                            return@ConfirmButton
                        }

                        if (fotosEntrada.isEmpty()) {
                            Toast.makeText(ctx, "Debe tomar al menos una foto", Toast.LENGTH_SHORT).show()
                            return@ConfirmButton
                        }

                        // Convertir todas las fotos a bytes
                        val fotosBytes = fotosEntrada.map { it.toByteArray() }

                        if (modoEntrada == "Normal") {
                            viewModel.registrarEntrada(
                                garageId = garageId,
                                vehiclePlate = placa.trim(),
                                empleadoId = empleadoId,
                                rateId = selectedRate!!.id!!,
                                fotosBytes = fotosBytes
                            )
                        } else {
                            if (reservaSeleccionada == null) {
                                Toast.makeText(ctx, "Selecciona una reserva", Toast.LENGTH_SHORT).show()
                                return@ConfirmButton
                            }

                            viewModel.registrarEntradaDesdeReserva(
                                reserva = reservaSeleccionada!!,
                                fotosBytes = fotosBytes,
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

// 🔥 NUEVO: Componente para múltiples fotos
@Composable
fun MultipleFotosSection(
    fotos: List<Bitmap>,
    onTakePhoto: () -> Unit,
    onRemovePhoto: (Int) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Botón para agregar foto
        Card(
            onClick = onTakePhoto,
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = CircleShape,
                    color = PrimaryRed.copy(alpha = 0.15f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = PrimaryRed,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    if (fotos.isEmpty()) stringResource(R.string.toca_para_tomar_foto)
                    else "Agregar otra foto",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )

                if (fotos.isNotEmpty()) {
                    Text(
                        "${fotos.size} foto${if (fotos.size != 1) "s" else ""} capturada${if (fotos.size != 1) "s" else ""}",
                        fontSize = 12.sp,
                        color = SuccessGreen
                    )
                }
            }
        }

        // Lista horizontal de fotos capturadas
        if (fotos.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(fotos.size) { index ->
                    FotoThumbnail(
                        bitmap = fotos[index],
                        index = index,
                        onRemove = { onRemovePhoto(index) }
                    )
                }
            }
        }
    }
}

@Composable
fun FotoThumbnail(
    bitmap: Bitmap,
    index: Int,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .height(140.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Foto ${index + 1}",
                modifier = Modifier.fillMaxSize()
            )

            // Badge con número
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .size(28.dp),
                shape = CircleShape,
                color = PrimaryRed
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        "${index + 1}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            // Botón eliminar
            IconButton(
                onClick = onRemove,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(32.dp)
                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Eliminar",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// Mantener los componentes existentes sin cambios...
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
            Text(stringResource(R.string.procesando), fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        } else {
            Icon(Icons.Default.Check, contentDescription = null)
            Spacer(Modifier.width(12.dp))
            Text(stringResource(R.string.confirmar_entrada), fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RateSelector(
    rates: List<Rate>,
    selectedRate: Rate?,
    onRateSelected: (Rate) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.AttachMoney,
                        contentDescription = null,
                        tint = PrimaryRed
                    )

                    Spacer(Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = selectedRate?.let { "${it.baseRate} / ${it.timeUnit}" }
                                ?: stringResource(R.string.selecciona_una_tarifa),
                            fontWeight = FontWeight.SemiBold,
                            color = if (selectedRate == null) TextSecondary else TextPrimary
                        )

                        if (selectedRate != null) {
                            Text(
                                text = "${stringResource(R.string.horario2)} ${selectedRate.horaInicio ?: "00:00"} - ${selectedRate.horaFin ?: "24:00"}",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Icon(
                        if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = PrimaryRed
                    )
                }
            }

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                rates.forEach { rate ->
                    DropdownMenuItem(
                        onClick = {
                            onRateSelected(rate)
                            expanded = false
                        },
                        text = {
                            Column {
                                Text(
                                    "${rate.baseRate} / ${rate.timeUnit}",
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    "${stringResource(R.string.horario2)} ${rate.horaInicio ?: "00:00"} - ${rate.horaFin ?: "24:00"}",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                        },
                        leadingIcon = {
                            Icon(Icons.Default.MonetizationOn, null, tint = PrimaryRed)
                        }
                    )
                }
            }
        }

        if (rates.isEmpty()) {
            Text(
                stringResource(R.string.no_hay_tarifas_activas_para_este_garaje),
                fontSize = 12.sp,
                color = TextSecondary
            )
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
            title = stringResource(R.string.entrada_normal),
            subtitle = stringResource(R.string.sin_reserva_previa),
            icon = Icons.Default.DirectionsCar,
            isSelected = modoEntrada == stringResource(R.string.normal),
            onClick = { onTipoSelected("Normal") }
        )

        SelectableCard(
            title = stringResource(R.string.entrada_por_reserva),
            subtitle = stringResource(R.string.con_reserva_confirmada),
            icon = Icons.Default.EventAvailable,
            isSelected = modoEntrada == stringResource(R.string.reserva),
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
                        "${it.users?.nombre ?: stringResource(R.string.usuario)} - ${it.vehicles?.plate ?: stringResource(
                            R.string.sin_placa
                        )}"
                    } ?: "",
                    onValueChange = {},
                    readOnly = true,
                    placeholder = { Text(stringResource(R.string.selecciona_una_reserva), color = TextSecondary) },
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
                                        "${reserva.users?.nombre ?: stringResource(R.string.usuario)} - ${reserva.vehicles?.plate ?: stringResource(
                                            R.string.sin_placa
                                        )}",
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
                stringResource(
                    R.string.reserva_disponible,
                    reservas.size,
                    if (reservas.size != 1) "s" else "",
                    if (reservas.size != 1) "s" else ""
                ),
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
                stringResource(R.string.no_hay_reservas_disponibles),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = WarningOrange,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(4.dp))
            Text(
                stringResource(R.string.no_se_encontraron_reservas_activas_para_este_estacionamiento),
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
        label = { Text(stringResource(R.string.placa_del_veh_culo)) },
        placeholder = { Text(stringResource(R.string.ej_abc_1234), color = TextSecondary) },
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
            disabledContainerColor = Color(0xFFF8F9FA),

            // 🔥 FIX DEL TEXTO INVISIBLE
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            disabledTextColor = TextSecondary,
            cursorColor = PrimaryRed
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    )

    if (!enabled) {
        Text(
            stringResource(R.string.la_placa_se_completar_autom_ticamente_desde_la_reserva),
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
                        contentDescription = stringResource(R.string.foto_del_veh_culo),
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
                            contentDescription = stringResource(R.string.retomar_foto),
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
                        stringResource(R.string.toca_para_tomar_foto),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )

                    Text(
                        stringResource(R.string.captura_el_veh_culo_completo),
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

fun Bitmap.toByteArray(): ByteArray {
    val stream = java.io.ByteArrayOutputStream()
    this.compress(Bitmap.CompressFormat.JPEG, 90, stream)
    return stream.toByteArray()
}