package com.kotlin.u_park.presentation.screens.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.kotlin.u_park.data.remote.supabase
import com.kotlin.u_park.data.repository.VehiclesRepositoryImpl
import com.kotlin.u_park.domain.model.Vehicle
import com.kotlin.u_park.presentation.screens.parking.ParkingViewModel
import com.kotlin.u_park.presentation.screens.vehicles.VehiclesViewModel
import com.kotlin.u_park.presentation.screens.vehicles.VehiclesViewModelFactory
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.time.ZoneId
import java.time.Instant
import java.time.format.DateTimeFormatter

// 🎨 Color System (matching Home)
private val PrimaryRed = Color(0xFFE60023)
private val DarkRed = Color(0xFFB8001C)
private val LightRed = Color(0xFFFFE5E9)
private val BackgroundColor = Color(0xFFFAFAFA)
private val SurfaceColor = Color(0xFFFFFFFF)
private val TextPrimary = Color(0xFF0D0D0D)
private val TextSecondary = Color(0xFF6E6E73)
private val BorderColor = Color(0xFFE5E5EA)
private val SuccessGreen = Color(0xFF34C759)
private val WarningOrange = Color(0xFFFF9500)

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrarReservaScreen(
    navController: NavController,
    viewModel: ParkingViewModel,
    garageId: String,
    userId: String
) {
    val ctx = LocalContext.current

    // Vehicle setup
    val vehicleRepo = remember { VehiclesRepositoryImpl(supabase) }
    val vehiclesViewModel: VehiclesViewModel = viewModel(factory = VehiclesViewModelFactory(vehicleRepo))

    LaunchedEffect(Unit) {
        vehiclesViewModel.loadVehicles(userId)
    }

    val vehicles by vehiclesViewModel.vehicles.collectAsState()
    var selectedVehicle by remember { mutableStateOf<Vehicle?>(null) }
    var showVehicleSheet by remember { mutableStateOf(false) }

    // Date & Time
    val datePickerState = rememberDatePickerState()
    var showDatePicker by remember { mutableStateOf(false) }
    val timeState = rememberTimePickerState()
    var showTimePicker by remember { mutableStateOf(false) }
    var selectedTime by remember { mutableStateOf<LocalTime?>(null) }

    val loading by viewModel.isLoading.collectAsState()
    val msg by viewModel.message.collectAsState()

    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorSnackbar by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Handle messages
    LaunchedEffect(msg) {
        msg?.let {
            if (it == "Reserva creada") {
                showSuccessDialog = true
            } else {
                errorMessage = it
                showErrorSnackbar = true
            }
        }
    }

    // Success Dialog with Auto-Return
    if (showSuccessDialog) {
        SuccessReservationDialog(
            onDismiss = {
                showSuccessDialog = false
                navController.navigate("home") {
                    popUpTo("home") { inclusive = true }
                }
            }
        )
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(showErrorSnackbar) {
        if (showErrorSnackbar) {
            snackbarHostState.showSnackbar(
                message = errorMessage,
                duration = SnackbarDuration.Short
            )
            showErrorSnackbar = false
        }
    }

    Scaffold(
        containerColor = BackgroundColor,
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = Color(0xFFFEECEB),
                    contentColor = Color(0xFFD32F2F),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        topBar = {
            Surface(
                color = SurfaceColor,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        onClick = { navController.popBackStack() },
                        shape = CircleShape,
                        color = BackgroundColor,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Volver",
                                tint = TextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            "Nueva Reserva",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            "Completa los detalles",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {

            // 🚗 Vehicle Selection Card
            SectionCard(
                title = "Vehículo",
                icon = Icons.Outlined.DirectionsCar,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                SelectionButton(
                    value = selectedVehicle?.let { "${it.plate} - ${it.model ?: ""}" } ?: "Seleccionar vehículo",
                    icon = Icons.Outlined.DirectionsCar,
                    placeholder = selectedVehicle == null,
                    onClick = { showVehicleSheet = true }
                )
            }

            // 📅 Date Selection Card
            SectionCard(
                title = "Fecha",
                icon = Icons.Outlined.CalendarToday,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                SelectionButton(
                    value = datePickerState.selectedDateMillis?.let {
                        val date = Instant.ofEpochMilli(it)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        date.format(DateTimeFormatter.ofPattern("dd 'de' MMMM, yyyy"))
                    } ?: "Seleccionar fecha",
                    icon = Icons.Outlined.CalendarToday,
                    placeholder = datePickerState.selectedDateMillis == null,
                    onClick = { showDatePicker = true }
                )
            }

            // ⏰ Time Selection Card
            SectionCard(
                title = "Hora de llegada",
                icon = Icons.Outlined.AccessTime,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                SelectionButton(
                    value = selectedTime?.format(DateTimeFormatter.ofPattern("hh:mm a"))
                        ?: "Seleccionar hora",
                    icon = Icons.Outlined.AccessTime,
                    placeholder = selectedTime == null,
                    onClick = { showTimePicker = true }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 📋 Summary Card
            AnimatedVisibility(
                visible = selectedVehicle != null && datePickerState.selectedDateMillis != null && selectedTime != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                ReservationSummary(
                    vehicle = selectedVehicle,
                    date = datePickerState.selectedDateMillis,
                    time = selectedTime
                )
            }

            Spacer(modifier = Modifier.height(100.dp))
        }

        // 🎯 Floating Action Button
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                shape = RoundedCornerShape(16.dp),
                shadowElevation = 8.dp,
                color = SurfaceColor
            ) {
                Button(
                    onClick = {
                        val fechaMillis = datePickerState.selectedDateMillis

                        if (selectedVehicle == null) {
                            errorMessage = "Selecciona un vehículo"
                            showErrorSnackbar = true
                            return@Button
                        }
                        if (fechaMillis == null) {
                            errorMessage = "Selecciona la fecha"
                            showErrorSnackbar = true
                            return@Button
                        }
                        if (selectedTime == null) {
                            errorMessage = "Selecciona la hora"
                            showErrorSnackbar = true
                            return@Button
                        }

                        val hoy = Instant.now().atZone(ZoneId.systemDefault()).toLocalDate()
                        val fechaSeleccionada = Instant.ofEpochMilli(fechaMillis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()

                        if (fechaSeleccionada.isBefore(hoy)) {
                            errorMessage = "No puedes seleccionar una fecha pasada"
                            showErrorSnackbar = true
                            return@Button
                        }

                        if (fechaSeleccionada.isEqual(hoy)) {
                            val ahora = LocalTime.now()
                            if (selectedTime!!.isBefore(ahora)) {
                                errorMessage = "La hora no puede ser en el pasado"
                                showErrorSnackbar = true
                                return@Button
                            }
                        }

                        val fechaFinal = fechaSeleccionada
                            .atTime(selectedTime!!)
                            .atZone(ZoneId.systemDefault())
                            .toOffsetDateTime()
                            .toString()

                        viewModel.crearReserva(
                            garageId = garageId,
                            vehicleId = selectedVehicle!!.id ?: "",
                            fecha = fechaFinal,
                            userId = userId
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !loading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryRed,
                        disabledContainerColor = BorderColor
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    if (loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                "Confirmar Reserva",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    // Vehicle Bottom Sheet
    if (showVehicleSheet) {
        ModalBottomSheet(
            onDismissRequest = { showVehicleSheet = false },
            containerColor = SurfaceColor,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            VehicleSelectionSheet(
                vehicles = vehicles,
                selectedVehicle = selectedVehicle,
                onVehicleSelected = { vehicle ->
                    selectedVehicle = vehicle
                    showVehicleSheet = false
                }
            )
        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Confirmar", color = PrimaryRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = PrimaryRed,
                    todayContentColor = PrimaryRed,
                    todayDateBorderColor = PrimaryRed
                )
            )
        }
    }

    // Time Picker Dialog
    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedTime = LocalTime.of(timeState.hour, timeState.minute)
                    showTimePicker = false
                }) {
                    Text("Confirmar", color = PrimaryRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            },
            title = {
                Text(
                    "Seleccionar hora",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                TimePicker(
                    state = timeState,
                    colors = TimePickerDefaults.colors(
                        clockDialSelectedContentColor = Color.White,
                        selectorColor = PrimaryRed,
                        timeSelectorSelectedContainerColor = PrimaryRed
                    )
                )
            },
            containerColor = SurfaceColor,
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
fun SectionCard(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = PrimaryRed,
                modifier = Modifier.size(20.dp)
            )
            Text(
                title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }
        content()
    }
}

@Composable
fun SelectionButton(
    value: String,
    icon: ImageVector,
    placeholder: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = SurfaceColor,
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(BackgroundColor, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = if (placeholder) TextSecondary else PrimaryRed,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Text(
                    value,
                    fontSize = 15.sp,
                    fontWeight = if (placeholder) FontWeight.Normal else FontWeight.SemiBold,
                    color = if (placeholder) TextSecondary else TextPrimary
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ReservationSummary(
    vehicle: Vehicle?,
    date: Long?,
    time: LocalTime?
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(16.dp),
        color = LightRed
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(PrimaryRed.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Receipt,
                        contentDescription = null,
                        tint = PrimaryRed,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Text(
                    "Resumen de reserva",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            Divider(color = PrimaryRed.copy(alpha = 0.2f))

            SummaryRow(
                label = "Vehículo",
                value = "${vehicle?.plate} - ${vehicle?.model ?: ""}"
            )

            SummaryRow(
                label = "Fecha",
                value = date?.let {
                    Instant.ofEpochMilli(it)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                } ?: ""
            )

            SummaryRow(
                label = "Hora",
                value = time?.format(DateTimeFormatter.ofPattern("hh:mm a")) ?: ""
            )
        }
    }
}

@Composable
fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            fontSize = 14.sp,
            color = TextSecondary,
            fontWeight = FontWeight.Medium
        )
        Text(
            value,
            fontSize = 14.sp,
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun VehicleSelectionSheet(
    vehicles: List<Vehicle>,
    selectedVehicle: Vehicle?,
    onVehicleSelected: (Vehicle) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp)
    ) {
        Text(
            "Seleccionar vehículo",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
        )

        if (vehicles.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Outlined.DirectionsCar,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = TextSecondary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "No tienes vehículos registrados",
                    fontSize = 15.sp,
                    color = TextSecondary
                )
            }
        } else {
            vehicles.forEach { vehicle ->
                VehicleItem(
                    vehicle = vehicle,
                    isSelected = selectedVehicle?.id == vehicle.id,
                    onClick = { onVehicleSelected(vehicle) }
                )
            }
        }
    }
}

@Composable
fun VehicleItem(
    vehicle: Vehicle,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) LightRed else SurfaceColor,
        border = BorderStroke(1.dp, if (isSelected) PrimaryRed else BorderColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (isSelected) PrimaryRed.copy(alpha = 0.15f) else BackgroundColor,
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.DirectionsCar,
                    contentDescription = null,
                    tint = if (isSelected) PrimaryRed else TextSecondary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    vehicle.plate,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    vehicle.model ?: "Sin modelo",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }

            if (isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = PrimaryRed,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun SuccessReservationDialog(onDismiss: () -> Unit) {
    var progress by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        while (progress < 1f) {
            delay(30)
            progress += 0.02f
        }
        delay(500)
        onDismiss()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceColor,
        shape = RoundedCornerShape(24.dp),
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(SuccessGreen.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = SuccessGreen,
                        modifier = Modifier.size(48.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    "¡Reserva confirmada!",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    "Tu espacio ha sido reservado exitosamente. Te esperamos en el garaje.",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = SuccessGreen,
                    trackColor = SuccessGreen.copy(alpha = 0.2f)
                )
            }
        },
        confirmButton = {}
    )
}