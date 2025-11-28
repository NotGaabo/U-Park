package com.kotlin.u_park.presentation.screens.home

import android.app.TimePickerDialog
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kotlin.u_park.data.remote.supabase
import com.kotlin.u_park.data.repository.VehiclesRepositoryImpl
import com.kotlin.u_park.domain.model.Vehicle
import com.kotlin.u_park.presentation.screens.parking.ParkingViewModel
import com.kotlin.u_park.presentation.screens.vehicles.VehiclesViewModel
import com.kotlin.u_park.presentation.screens.vehicles.VehiclesViewModelFactory
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.Instant

// Colores del diseño
private val redPrimary = Color(0xFFE74C3C)
private val greenSuccess = Color(0xFF27AE60)
private val grayLight = Color(0xFFF8F9FA)
private val grayMedium = Color(0xFFECF0F1)
private val darkText = Color(0xFF2C3E50)

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrarReservaScreen(
    viewModel: ParkingViewModel,
    garageId: String,
    userId: String
) {
    val ctx = LocalContext.current

    // ---- VEHICULOS ----
    val vehicleRepo = remember { VehiclesRepositoryImpl(supabase) }
    val vm: VehiclesViewModel = viewModel(factory = VehiclesViewModelFactory(vehicleRepo))

    LaunchedEffect(Unit) {
        vm.loadVehicles(userId)
    }

    val vehicles by vm.vehicles.collectAsState()
    var selectedVehicle by remember { mutableStateOf<Vehicle?>(null) }
    var expanded by remember { mutableStateOf(false) }

    // ---- FECHA ----
    val datePickerState = rememberDatePickerState()
    var showDatePicker by remember { mutableStateOf(false) }

    // ---- HORA ----
    val timeState = rememberTimePickerState()
    var showTimePicker by remember { mutableStateOf(false) }
    var selectedTime by remember { mutableStateOf<LocalTime?>(null) }

    val loading by viewModel.isLoading.collectAsState()
    val msg by viewModel.message.collectAsState()

    LaunchedEffect(msg) {
        msg?.let { Toast.makeText(ctx, it, Toast.LENGTH_SHORT).show() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Crear Reserva",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = darkText
                )
            )
        },
        containerColor = grayLight
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Card principal con formulario
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Icono decorativo
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = redPrimary.copy(alpha = 0.1f),
                        modifier = Modifier.size(80.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.EventAvailable,
                                contentDescription = null,
                                tint = redPrimary,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    Text(
                        "Completa los datos",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = darkText
                    )
                    Text(
                        "Selecciona tu vehículo y horario",
                        fontSize = 14.sp,
                        color = darkText.copy(alpha = 0.6f)
                    )

                    Spacer(Modifier.height(32.dp))

                    // -------------------------
                    // SELECT DE VEHICULOS
                    // -------------------------
                    Text(
                        "Vehículo",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = darkText,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(Modifier.height(8.dp))

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = selectedVehicle?.plate ?: "",
                            onValueChange = {},
                            readOnly = true,
                            placeholder = { Text("Selecciona un vehículo") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.DirectionsCar,
                                    contentDescription = null,
                                    tint = redPrimary
                                )
                            },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = grayMedium,
                                focusedBorderColor = redPrimary
                            )
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            vehicles.forEach { v ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.DirectionsCar,
                                                contentDescription = null,
                                                tint = redPrimary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Text("${v.plate} - ${v.model ?: ""}")
                                        }
                                    },
                                    onClick = {
                                        selectedVehicle = v
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // -------------------------
                    // FECHA
                    // -------------------------
                    Text(
                        "Fecha",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = darkText,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White,
                            contentColor = darkText
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            width = 1.dp,
                            brush = androidx.compose.ui.graphics.SolidColor(grayMedium)
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CalendarToday,
                                    contentDescription = null,
                                    tint = redPrimary
                                )
                                Text(
                                    datePickerState.selectedDateMillis?.let {
                                        Instant.ofEpochMilli(it)
                                            .atZone(ZoneId.systemDefault())
                                            .toLocalDate()
                                            .toString()
                                    } ?: "Seleccionar fecha",
                                    fontWeight = FontWeight.Normal
                                )
                            }
                            Icon(
                                imageVector = Icons.Filled.ArrowDropDown,
                                contentDescription = null,
                                tint = darkText.copy(alpha = 0.5f)
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // -------------------------
                    // HORA
                    // -------------------------
                    Text(
                        "Hora",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = darkText,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { showTimePicker = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White,
                            contentColor = darkText
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            width = 1.dp,
                            brush = androidx.compose.ui.graphics.SolidColor(grayMedium)
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AccessTime,
                                    contentDescription = null,
                                    tint = redPrimary
                                )
                                Text(
                                    selectedTime?.toString() ?: "Seleccionar hora",
                                    fontWeight = FontWeight.Normal
                                )
                            }
                            Icon(
                                imageVector = Icons.Filled.ArrowDropDown,
                                contentDescription = null,
                                tint = darkText.copy(alpha = 0.5f)
                            )
                        }
                    }

                    Spacer(Modifier.height(32.dp))

                    // Botón de crear
                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        onClick = {
                            val fechaMillis = datePickerState.selectedDateMillis
                            if (selectedVehicle == null) {
                                Toast.makeText(ctx, "Selecciona un vehículo", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (fechaMillis == null) {
                                Toast.makeText(ctx, "Selecciona la fecha", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (selectedTime == null) {
                                Toast.makeText(ctx, "Selecciona la hora", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val fechaFinal = Instant.ofEpochMilli(fechaMillis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                                .atTime(selectedTime!!)
                                .atOffset(ZoneOffset.UTC)
                                .toString()

                            viewModel.crearReserva(
                                garageId = garageId,
                                vehicleId = selectedVehicle!!.id,
                                fecha = fechaFinal,
                                userId = userId
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = redPrimary
                        ),
                        enabled = !loading
                    ) {
                        if (loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(12.dp))
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null
                            )
                            Spacer(Modifier.width(8.dp))
                        }
                        Text(
                            if (loading) "Guardando..." else "Crear Reserva",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // -------------------------
            // DEBUG (Opcional - con mejor diseño)
            // -------------------------
            if (vehicles.isNotEmpty() || true) { // Cambiar a false para ocultar
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.7f)
                    )
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = null,
                                tint = darkText.copy(alpha = 0.6f),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                "Información de Debug",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = darkText.copy(alpha = 0.8f)
                            )
                        }

                        Spacer(Modifier.height(12.dp))
                        Divider(color = grayMedium)
                        Spacer(Modifier.height(12.dp))

                        Text(
                            "User ID: $userId",
                            fontSize = 12.sp,
                            color = darkText.copy(alpha = 0.7f)
                        )
                        Text(
                            "Vehículos encontrados: ${vehicles.size}",
                            fontSize = 12.sp,
                            color = darkText.copy(alpha = 0.7f)
                        )

                        if (vehicles.isNotEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            vehicles.forEach { v ->
                                Text(
                                    "• ${v.plate} | id=${v.id}",
                                    fontSize = 11.sp,
                                    color = darkText.copy(alpha = 0.6f)
                                )
                            }
                        }

                        if (vehicles.isEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "⚠️ No se encontraron vehículos para este usuario",
                                color = redPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }

    // Date Picker
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("OK", color = redPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar", color = darkText.copy(alpha = 0.6f))
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = redPrimary,
                    todayContentColor = redPrimary,
                    todayDateBorderColor = redPrimary
                )
            )
        }
    }

    // Time Picker
    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedTime = LocalTime.of(timeState.hour, timeState.minute)
                    showTimePicker = false
                }) {
                    Text("OK", color = redPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancelar", color = darkText.copy(alpha = 0.6f))
                }
            },
            title = {
                Text(
                    "Seleccionar hora",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                TimePicker(
                    state = timeState,
                    colors = TimePickerDefaults.colors(
                        clockDialSelectedContentColor = Color.White,
                        selectorColor = redPrimary,
                        timeSelectorSelectedContainerColor = redPrimary
                    )
                )
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}