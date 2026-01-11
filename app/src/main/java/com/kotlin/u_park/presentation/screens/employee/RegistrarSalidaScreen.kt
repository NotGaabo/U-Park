package com.kotlin.u_park.presentation.screens.employee

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.platform.LocalContext
import com.kotlin.u_park.presentation.utils.PdfGenerator
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.kotlin.u_park.domain.model.SalidaResponse
import com.kotlin.u_park.presentation.screens.parking.ParkingViewModel
import com.kotlin.u_park.presentation.screens.rates.RatesViewModel
import kotlinx.coroutines.launch
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.content.FileProvider
import java.io.File
import java.io.ByteArrayOutputStream
import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import com.kotlin.u_park.R
import com.kotlin.u_park.ui.theme.*

private val GreenSoft = Color(0xFF4CAF50)

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrarSalidaScreen(
    parkingId: String,
    ratesViewModel: RatesViewModel,
    parkingViewModel: ParkingViewModel,
    navController: NavController
) {
    val salida = ratesViewModel.salidaState.value
    val loading by ratesViewModel.loading
    val error by ratesViewModel.errorMessage

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isGeneratingPdf by remember { mutableStateOf(false) }

    val vehiculoNombre by ratesViewModel.vehiculoNombre.collectAsState()
    val garageNombre by ratesViewModel.garageNombre.collectAsState()

    // 🔥 Escuchar mensaje y loading del ViewModel
    val vmMessage by parkingViewModel.message.collectAsState()
    val vmLoading by parkingViewModel.isLoading.collectAsState()

    // Estados para método de pago
    var showPaymentDialog by remember { mutableStateOf(false) }
    var selectedPaymentMethod by remember { mutableStateOf<String?>(null) }
    var transferBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    // Archivo temporal para la foto
    val transferImageFile = remember { mutableStateOf<File?>(null) }

    // Launcher para permisos de cámara
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            transferImageFile.value?.let { file ->
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    file
                )
                showPermissionDialog = false
            }
        } else {
            showPermissionDialog = true
        }
    }

    // Launcher de cámara
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            transferImageFile.value?.let { file ->
                try {
                    if (file.exists()) {
                        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                        if (bitmap != null) {
                            transferBitmap = bitmap
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    // Función para iniciar cámara con permisos
    fun launchCamera() {
        val permission = Manifest.permission.CAMERA
        when {
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED -> {
                val file = createImageFile(context)
                transferImageFile.value = file
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    file
                )
                cameraLauncher.launch(uri)
            }
            else -> {
                cameraPermissionLauncher.launch(permission)
            }
        }
    }

    // Validar ID
    LaunchedEffect(Unit) {
        if (parkingId.isBlank()) {
            navController.popBackStack()
            return@LaunchedEffect
        }
    }

    // Cargar ticket
    LaunchedEffect(parkingId) {
        if (parkingId.isNotBlank()) {
            ratesViewModel.cargarDatosTicket(parkingId)
        }
    }

    // 🔥 Observar mensaje del ViewModel
    LaunchedEffect(vmMessage) {
        if (vmMessage != null) {
            println("📬 Mensaje recibido: $vmMessage")
            if (vmMessage!!.contains("registrada y pagada correctamente")) {
                showSuccessDialog = true
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.ticket_de_salida),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = RedSoft,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->

        when {
            loading -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(BackgroundColor)
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            color = RedSoft,
                            strokeWidth = 3.dp
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            stringResource(R.string.cargando_ticket),
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            error != null -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(BackgroundColor)
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "⚠️",
                                fontSize = 48.sp
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                stringResource(R.string.error),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                error ?: stringResource(R.string.error_desconocido),
                                textAlign = TextAlign.Center,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            salida != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(BackgroundColor)
                        .padding(padding)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {

                        TicketViewImproved(
                            salida = salida,
                            vehiculoNombre = vehiculoNombre ?: stringResource(R.string.veh_culo),
                            garageNombre = garageNombre ?: stringResource(R.string.garage)
                        )

                        // 🔥 Botón con loading
                        Button(
                            onClick = { showPaymentDialog = true },
                            enabled = !vmLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GreenSoft
                            ),
                            shape = RoundedCornerShape(16.dp),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 4.dp
                            )
                        ) {
                            if (vmLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White
                                )
                            } else {
                                Text(
                                    stringResource(R.string.confirmar_salida),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Text(
                            stringResource(R.string.generar_comprobante),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A1A)
                        )

                        if (isGeneratingPdf) {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = RedSoft
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        isGeneratingPdf = true
                                        try {
                                            val pdfFile = PdfGenerator.generateFacturaSalida(
                                                context = context,
                                                ticket = salida,
                                                vehiculoNombre = vehiculoNombre ?: context.getString(
                                                    R.string.ve
                                                ),
                                                garageNombre = garageNombre ?: context.getString(R.string.garag),
                                                saveToDownloads = false
                                            )
                                            PdfGenerator.compartirFactura(context, pdfFile)
                                        } finally {
                                            isGeneratingPdf = false
                                        }
                                    }
                                },
                                enabled = !isGeneratingPdf,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = RedSoft
                                ),
                                border = ButtonDefaults.outlinedButtonBorder.copy(
                                    width = 2.dp
                                )
                            ) {
                                Icon(
                                    Icons.Default.Share,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(stringResource(R.string.compartir), fontWeight = FontWeight.SemiBold)
                            }

                            Button(
                                onClick = {
                                    scope.launch {
                                        isGeneratingPdf = true
                                        try {
                                            PdfGenerator.generateFacturaSalida(
                                                context = context,
                                                ticket = salida,
                                                vehiculoNombre = vehiculoNombre ?: context.getString(R.string.veh_culo),
                                                garageNombre = garageNombre ?: context.getString(R.string.garage),
                                                saveToDownloads = true
                                            )
                                        } finally {
                                            isGeneratingPdf = false
                                        }
                                    }
                                },
                                enabled = !isGeneratingPdf,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = RedSoft
                                ),
                                shape = RoundedCornerShape(16.dp),
                                elevation = ButtonDefaults.buttonElevation(
                                    defaultElevation = 4.dp
                                )
                            ) {
                                Icon(
                                    Icons.Default.Download,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(stringResource(R.string.descargar), fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                if (showPaymentDialog) {
                    PaymentMethodDialog(
                        onDismiss = {
                            transferBitmap = null
                            selectedPaymentMethod = null
                            showPaymentDialog = false
                        },
                        selectedMethod = selectedPaymentMethod,
                        transferBitmap = transferBitmap,
                        onMethodSelected = { method ->
                            selectedPaymentMethod = method
                            if (method == context.getString(R.string.transfer)) {
                                launchCamera()
                            }
                        },
                        onConfirm = {
                            scope.launch {
                                when (selectedPaymentMethod) {
                                    context.getString(R.string.cash) -> {
                                        parkingViewModel.registrarSalidaConPago(
                                            parkingId = parkingId,
                                            metodoPago = context.getString(R.string.efectivo),
                                            comprobanteBytes = null
                                        )
                                    }
                                    context.getString(R.string.transfer2) -> {
                                        val bytes = transferBitmap?.toByteArray()
                                            ?: return@launch

                                        parkingViewModel.registrarSalidaConPago(
                                            parkingId = parkingId,
                                            metodoPago = context.getString(R.string.transferencia),
                                            comprobanteBytes = bytes
                                        )
                                    }
                                }
                                showPaymentDialog = false
                            }
                        }
                    )
                }

                if (showPermissionDialog) {
                    AlertDialog(
                        onDismissRequest = { showPermissionDialog = false },
                        title = { Text(stringResource(R.string.permiso_requerido)) },
                        text = { Text(stringResource(R.string.se_necesita_acceso_a_la_c_mara_para_tomar_la_foto_del_comprobante)) },
                        confirmButton = {
                            TextButton(onClick = { showPermissionDialog = false }) {
                                Text(stringResource(R.string.entendido))
                            }
                        }
                    )
                }

                // 🔥 Diálogo de éxito
                if (showSuccessDialog) {
                    AlertDialog(
                        onDismissRequest = {
                            showSuccessDialog = false
                            parkingViewModel.clearMessage()
                            navController.popBackStack()
                        },
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("✅ ", fontSize = 24.sp)
                                Text(stringResource(R.string.salida_registrada), fontWeight = FontWeight.Bold)
                            }
                        },
                        text = {
                            Text(
                                stringResource(R.string.la_salida_ha_sido_registrada_y_pagada_correctamente),
                                textAlign = TextAlign.Center
                            )
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    showSuccessDialog = false
                                    parkingViewModel.clearMessage()
                                    navController.popBackStack()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GreenSoft
                                )
                            ) {
                                Text(stringResource(R.string.aceptar))
                            }
                        },
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PaymentMethodDialog(
    onDismiss: () -> Unit,
    selectedMethod: String?,
    transferBitmap: Bitmap?,
    onMethodSelected: (String) -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.seleccionar_m_todo_de_pago), fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                PaymentMethodCard(
                    title = stringResource(R.string.efectivo2),
                    icon = Icons.Default.AccountBalanceWallet,
                    color = GreenSoft,
                    isSelected = selectedMethod == stringResource(R.string.cash2),
                    onClick = { onMethodSelected("cash") }
                )

                PaymentMethodCard(
                    title = stringResource(R.string.transferencia2),
                    icon = Icons.Default.CreditCard,
                    color = Color(0xFF2196F3),
                    isSelected = selectedMethod == stringResource(R.string.transfer3),
                    onClick = { onMethodSelected("transfer") }
                )

                if (selectedMethod == stringResource(R.string.transfer4) && transferBitmap != null) {
                    Image(
                        bitmap = transferBitmap.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = selectedMethod == stringResource(R.string.cash5) ||
                        (selectedMethod == stringResource(R.string.transfer6) && transferBitmap != null)
            ) {
                Text(stringResource(R.string.confirmar_pago))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancelar3)) }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun PaymentMethodCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) color.copy(alpha = 0.15f) else Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(2.dp, color)
        } else null,
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = androidx.compose.foundation.shape.CircleShape,
                color = color.copy(alpha = 0.2f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            Text(
                title,
                fontSize = 16.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) color else Color(0xFF1A1A1A),
                modifier = Modifier.weight(1f)
            )

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TicketViewImproved(
    salida: SalidaResponse,
    vehiculoNombre: String,
    garageNombre: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    stringResource(R.string.ticket_de_salida),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = RedSoft
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Ticket #${salida.parking_id.take(8)}",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }

            HorizontalDivider(thickness = 1.dp, color = Color.LightGray)

            InfoSection(title = stringResource(R.string.informaci_n_del_veh_culo)) {
                InfoRowImproved(stringResource(R.string.veh_culo), vehiculoNombre)
                InfoRowImproved(stringResource(R.string.garage), garageNombre)
            }

            HorizontalDivider(thickness = 1.dp, color = Color.LightGray)

            InfoSection(title = stringResource(R.string.horarios)) {
                InfoRowImproved(stringResource(R.string.entrada), formatDateTime(salida.hora_entrada))
                InfoRowImproved(stringResource(R.string.salida), formatDateTime(salida.hora_salida))

                val horasDecimales = salida.duration_hours
                val minutosTotales = (horasDecimales * 60).toInt()
                val horas = minutosTotales / 60
                val minutos = minutosTotales % 60

                InfoRowImproved(stringResource(R.string.duraci_n), "%02d:%02d horas".format(horas, minutos))
            }

            HorizontalDivider(thickness = 1.dp, color = Color.LightGray)

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = RedSoft.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.total_a_pagar),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1A1A1A)
                    )
                    Text(
                        stringResource(R.string.rd, "%,.2f".format(salida.total)),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = RedSoft
                    )
                }
            }
        }
    }
}

@Composable
fun InfoSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray
        )
        content()
    }
}

@Composable
fun InfoRowImproved(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            fontWeight = FontWeight.Medium,
            fontSize = 15.sp,
            color = Color(0xFF1A1A1A)
        )
        Text(
            value,
            textAlign = TextAlign.End,
            fontSize = 15.sp,
            color = Color(0xFF424242),
            modifier = Modifier.weight(1f, fill = false)
        )
    }
}

fun createImageFile(context: Context): File {
    return File(
        context.cacheDir,
        "transfer_${System.currentTimeMillis()}.jpg"
    )
}

@RequiresApi(Build.VERSION_CODES.O)
fun formatDateTime(raw: String): String {
    return try {
        val instant = java.time.Instant.parse(raw)
        val zoned = instant.atZone(java.time.ZoneId.systemDefault())
        val formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a")
        formatter.format(zoned)
    } catch (e: Exception) {
        raw.replace("T", " ")
    }
}
