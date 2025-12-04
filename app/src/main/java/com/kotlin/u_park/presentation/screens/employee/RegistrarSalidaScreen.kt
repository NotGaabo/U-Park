package com.kotlin.u_park.presentation.screens.employee

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ticket de Salida") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->

        when {
            loading -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            error != null -> {
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Error: $error")
                }
            }

            salida != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {

                    // ---------------- TICKET ----------------
                    TicketView(
                        salida = salida,
                        vehiculoNombre = vehiculoNombre ?: "Vehículo",
                        garageNombre = garageNombre ?: "Garage"
                    )

                    // --------- CONFIRMAR SALIDA ---------
                    Button(
                        onClick = {
                            scope.launch {

                                // Recargar ticket por si acaso
                                ratesViewModel.cargarDatosTicket(parkingId)

                                // 🔥 LLAMADA FINAL: registra la salida CON empleadoId
                                parkingViewModel.registrarSalida(parkingId)

                                navController.popBackStack()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                    ) {
                        Text("Confirmar Salida")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (isGeneratingPdf) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // ---------------- BOTONES PDF ----------------
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {

                        // Compartir
                        Button(
                            onClick = {
                                scope.launch {
                                    isGeneratingPdf = true
                                    try {
                                        val pdfFile = PdfGenerator.generateFacturaSalida(
                                            context = context,
                                            ticket = salida,
                                            vehiculoNombre = vehiculoNombre ?: "Vehículo",
                                            garageNombre = garageNombre ?: "Garage",
                                            saveToDownloads = false
                                        )
                                        PdfGenerator.compartirFactura(context, pdfFile)
                                    } finally {
                                        isGeneratingPdf = false
                                    }
                                }
                            },
                            enabled = !isGeneratingPdf,
                            modifier = Modifier.weight(1f).padding(end = 8.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Compartir")
                        }

                        // Descargar
                        Button(
                            onClick = {
                                scope.launch {
                                    isGeneratingPdf = true
                                    try {
                                        PdfGenerator.generateFacturaSalida(
                                            context = context,
                                            ticket = salida,
                                            vehiculoNombre = vehiculoNombre ?: "Vehículo",
                                            garageNombre = garageNombre ?: "Garage",
                                            saveToDownloads = true
                                        )
                                    } finally {
                                        isGeneratingPdf = false
                                    }
                                }
                            },
                            enabled = !isGeneratingPdf,
                            modifier = Modifier.weight(1f).padding(start = 8.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Descargar")
                        }
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TicketView(
    salida: SalidaResponse,
    vehiculoNombre: String,
    garageNombre: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {

        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("TICKET DE SALIDA", fontSize = 22.sp, fontWeight = FontWeight.Bold)

            Text("Ticket #${salida.parking_id.take(8)}", color = Color.Gray)

            HorizontalDivider()

            InfoRow("Vehículo", vehiculoNombre)
            InfoRow("Garage", garageNombre)
            InfoRow("Entrada", formatDateTime(salida.hora_entrada))
            InfoRow("Salida", formatDateTime(salida.hora_salida))

            // ---- DURACIÓN CORREGIDA ----
            val horasDecimales = salida.duration_hours
            val minutosTotales = (horasDecimales * 60).toInt()
            val horas = minutosTotales / 60
            val minutos = minutosTotales % 60

            InfoRow("Duración", "%02d:%02d horas".format(horas, minutos))

            HorizontalDivider()

            Text(
                "Total: RD$ ${"%,.2f".format(salida.total)}",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFE60023)
            )
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontWeight = FontWeight.SemiBold)
        Text(value, textAlign = TextAlign.End)
    }
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
