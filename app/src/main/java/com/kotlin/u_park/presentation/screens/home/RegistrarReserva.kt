package com.kotlin.u_park.presentation.screens.home

import android.app.TimePickerDialog
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text("Crear Reserva", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(20.dp))

        // -------------------------
        // SELECT DE VEHICULOS
        // -------------------------
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedVehicle?.plate ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Seleccionar vehículo") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()         // 👈 SIN ESTO NO FUNCIONA
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                vehicles.forEach { v ->
                    DropdownMenuItem(
                        text = { Text("${v.plate} - ${v.model ?: ""}") },
                        onClick = {
                            selectedVehicle = v
                            expanded = false
                        }
                    )
                }
            }
    }

        Spacer(Modifier.height(20.dp))

        // -------------------------
        // FECHA
        // -------------------------
        Button(
            onClick = { showDatePicker = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                datePickerState.selectedDateMillis?.let {
                    Instant.ofEpochMilli(it)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                        .toString()
                } ?: "Seleccionar fecha"
            )
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { showTimePicker = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(selectedTime?.toString() ?: "Seleccionar hora")
        }

        Spacer(Modifier.height(20.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
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
            }
        ) {
            Text(if (loading) "Guardando..." else "Crear Reserva")
        }

        // -------------------------
        // DEBUG
        // -------------------------
        Spacer(Modifier.height(30.dp))
        Divider()
        Text("DEBUG", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(10.dp))

        Text("User ID: $userId")
        Text("Vehículos encontrados: ${vehicles.size}")

        vehicles.forEach { v ->
            Text("• ${v.plate}  | id=${v.id} | user_id=${v.user_id}")
        }

        if (vehicles.isEmpty()) {
            Text(
                "⚠️ No se encontraron vehículos para este usuario",
                color = MaterialTheme.colorScheme.error
            )
        }
    }

    // Date Picker
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("OK")
                }
            }
        ) { DatePicker(state = datePickerState) }
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
                    Text("OK")
                }
            },
            title = { Text("Seleccionar hora") },
            text = { TimePicker(state = timeState) }
        )
    }
}

