package com.kotlin.u_park.presentation.screens.vehicles

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.kotlin.u_park.R
import com.kotlin.u_park.domain.model.Vehicle
import com.kotlin.u_park.domain.model.VehicleTypeSimple
import com.kotlin.u_park.presentation.navigation.Routes
import com.kotlin.u_park.presentation.screens.parking.BottomBarItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import com.kotlin.u_park.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleScreen(
    navController: NavController,
    userId: String?,
    viewModel: VehiclesViewModel
) {
    val vehicles by viewModel.vehicles.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var showAddSheet by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        userId?.let { viewModel.loadVehicles(it) }
    }

    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            Surface(
                color = SurfaceColor,
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Text(
                        stringResource(R.string.mis_veh_culos),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        stringResource(R.string.administra_tu_flota),
                        fontSize = 14.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        bottomBar = {
            ModernBottomBar(
                selectedIndex = 1,
                onItemSelected = { index ->
                    when (index) {
                        0 -> navController.navigate("home")
                        2 -> userId?.let { navController.navigate(Routes.HistorialParking.createRoute(it)) }
                        3 -> navController.navigate("settings")
                    }
                }
            )
        },
        floatingActionButton = {
            if (vehicles.isNotEmpty() && !isLoading) {
                FloatingActionButton(
                    onClick = { showAddSheet = true },
                    containerColor = PrimaryRed,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = stringResource(R.string.agregar_veh_culo),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(
                                color = PrimaryRed,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                stringResource(R.string.cargando_veh_culos),
                                fontSize = 15.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }

                vehicles.isEmpty() -> {
                    EmptyVehiclesState(onAddVehicle = { showAddSheet = true })
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "${vehicles.size} vehículo${if (vehicles.size != 1) "s" else ""}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = LightRed
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = PrimaryRed,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            stringResource(R.string.activos2323),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryRed
                                        )
                                    }
                                }
                            }
                        }

                        items(vehicles) { vehicle ->
                            VehicleCard(
                                vehicle = vehicle,
                                onDelete = { viewModel.deleteVehicle(it) }
                            )
                        }
                    }
                }
            }
        }

        if (showAddSheet) {
            AddVehicleSheet(
                userId = userId,
                viewModel = viewModel,
                onDismiss = { showAddSheet = false },
                onSuccess = {
                    showAddSheet = false
                    userId?.let { viewModel.loadVehicles(it) }
                }
            )
        }
    }
}

@Composable
fun EmptyVehiclesState(onAddVehicle: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(BackgroundColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Outlined.DirectionsCar,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = TextSecondary.copy(alpha = 0.4f)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            stringResource(R.string.no_tienes_veh_culos),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            stringResource(R.string.agrega_tu_primer_veh_culo_para_comenzar_a_usar_u_park_y_reservar_espacios),
            fontSize = 15.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onAddVehicle,
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(56.dp)
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                stringResource(R.string.agregar_veh_culo2),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun VehicleCard(
    vehicle: Vehicle,
    onDelete: (String) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = SurfaceColor,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(LightRed, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.DirectionsCar,
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                    tint = PrimaryRed
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    vehicle.model ?: stringResource(R.string.sin_modelo3),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                VehicleDetailRow(
                    icon = Icons.Outlined.Tag,
                    text = vehicle.plate
                )

                if (vehicle.color != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    VehicleDetailRow(
                        icon = Icons.Outlined.Palette,
                        text = vehicle.color
                    )
                }

                if (vehicle.year != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    VehicleDetailRow(
                        icon = Icons.Outlined.CalendarToday,
                        text = vehicle.year.toString()
                    )
                }
            }

            Surface(
                onClick = { showDeleteDialog = true },
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFFEECEB),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Outlined.Delete,
                        contentDescription = "Eliminar",
                        tint = Color(0xFFD32F2F),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        DeleteVehicleDialog(
            vehicleModel = vehicle.model ?: stringResource(R.string.este_veh_culo),
            onConfirm = {
                onDelete(vehicle.id ?: "")
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
}

@Composable
fun VehicleDetailRow(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = TextSecondary
        )
        Text(
            text,
            fontSize = 14.sp,
            color = TextSecondary,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun DeleteVehicleDialog(
    vehicleModel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceColor,
        shape = RoundedCornerShape(24.dp),
        icon = {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(Color(0xFFFEECEB), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.DeleteForever,
                    contentDescription = null,
                    tint = Color(0xFFD32F2F),
                    modifier = Modifier.size(32.dp)
                )
            }
        },
        title = {
            Text(
                stringResource(R.string.eliminar_veh_culo),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Text(
                stringResource(
                    R.string.se_eliminar_permanentemente_esta_acci_n_no_se_puede_deshacer,
                    vehicleModel
                ),
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD32F2F)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.s_eliminar), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.cancelar23), color = TextPrimary, fontWeight = FontWeight.SemiBold)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddVehicleSheet(
    userId: String?,
    viewModel: VehiclesViewModel,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var plate by rememberSaveable { mutableStateOf("") }
    var model by rememberSaveable { mutableStateOf("") }
    var color by rememberSaveable { mutableStateOf("") }
    var year by rememberSaveable { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var selectedType by remember { mutableStateOf<VehicleTypeSimple?>(null) }

    val vehicleTypes by viewModel.vehicleTypes.collectAsState()
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val isLoading by viewModel.isLoading.collectAsState()
    val isSuccess by viewModel.isSuccess.collectAsState()

    var showSuccessDialog by remember { mutableStateOf(false) }

    val isFormValid = plate.isNotBlank() && model.isNotBlank() &&
            color.isNotBlank() && selectedType != null

    LaunchedEffect(Unit) {
        viewModel.loadVehicleTypes()
    }

    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            viewModel.resetStatus()
            showSuccessDialog = true
        }
    }

    if (showSuccessDialog) {
        SuccessVehicleDialog(
            onDismiss = {
                showSuccessDialog = false
                onSuccess()
            }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceColor,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
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
                        stringResource(R.string.nuevo_veh_culo),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        stringResource(R.string.completa_la_informaci_n3),
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                }

                Surface(
                    onClick = onDismiss,
                    shape = CircleShape,
                    color = BackgroundColor,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = TextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Preview Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = LightRed
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.DirectionsCar,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = PrimaryRed.copy(alpha = 0.6f)
                        )
                        Text(
                            stringResource(R.string.tu_veh_culo),
                            fontSize = 16.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Form Fields
            FormTextField(
                label = stringResource(R.string.placa23),
                value = plate,
                onValueChange = { plate = it.uppercase(); showError = false },
                placeholder = stringResource(R.string.abc_123432),
                icon = Icons.Outlined.Tag
            )

            Spacer(modifier = Modifier.height(16.dp))

            FormTextField(
                label = stringResource(R.string.modelo),
                value = model,
                onValueChange = { model = it; showError = false },
                placeholder = stringResource(R.string.toyota_corolla),
                icon = Icons.Outlined.CarRepair
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    FormTextField(
                        label = stringResource(R.string.color),
                        value = color,
                        onValueChange = { color = it; showError = false },
                        placeholder = stringResource(R.string.rojo),
                        icon = Icons.Outlined.Palette
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    FormTextField(
                        label = stringResource(R.string.a_o),
                        value = year,
                        onValueChange = {
                            if (it.all { char -> char.isDigit() } && it.length <= 4) {
                                year = it
                            }
                        },
                        placeholder = stringResource(R.string._2024),
                        icon = Icons.Outlined.CalendarToday,
                        keyboardType = KeyboardType.Number
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Vehicle Type Dropdown
            Column {
                Text(
                    stringResource(R.string.tipo_de_veh_culo3),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedType?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        placeholder = { Text(stringResource(R.string.selecciona_un_tipo), color = TextSecondary) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.Category,
                                contentDescription = null,
                                tint = if (selectedType != null) PrimaryRed else TextSecondary
                            )
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryRed,
                            unfocusedBorderColor = BorderColor,
                            focusedContainerColor = SurfaceColor,
                            unfocusedContainerColor = BackgroundColor
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        vehicleTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.name) },
                                onClick = {
                                    selectedType = type
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Error Message
            AnimatedVisibility(
                visible = showError,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFEECEB)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = Color(0xFFD32F2F),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            errorMessage,
                            color = Color(0xFFD32F2F),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Submit Button
            Button(
                onClick = {
                    if (!isFormValid) {
                        showError = true
                        errorMessage = "Completa todos los campos obligatorios (*)"
                        return@Button
                    }

                    if (userId.isNullOrEmpty()) {
                        showError = true
                        errorMessage = "Error de sesión"
                        return@Button
                    }

                    coroutineScope.launch {
                        val newVehicle = Vehicle(
                            id = UUID.randomUUID().toString(),
                            user_id = userId,
                            plate = plate,
                            model = model,
                            color = color,
                            year = year.toIntOrNull(),
                            type_id = selectedType!!.id
                        )
                        viewModel.addVehicle(newVehicle)
                    }
                },
                enabled = !isLoading && isFormValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryRed,
                    disabledContainerColor = BorderColor
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.guardar_veh_culo),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun FormTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column {
        Text(
            label,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = TextSecondary) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            leadingIcon = {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = if (value.isNotBlank()) PrimaryRed else TextSecondary
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryRed,
                unfocusedBorderColor = BorderColor,
                focusedContainerColor = SurfaceColor,
                unfocusedContainerColor = BackgroundColor
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
        )
    }
}

@Composable
fun SuccessVehicleDialog(onDismiss: () -> Unit) {
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
                    stringResource(R.string.veh_culo_agregado),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    stringResource(R.string.tu_veh_culo_ha_sido_registrado_exitosamente),
                    fontSize = 14.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
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


@Composable
fun ModernBottomBar(
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = SurfaceColor,
        shadowElevation = 12.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomBarItem(
                icon = Icons.Outlined.Home,
                selectedIcon = Icons.Default.Home,
                label = stringResource(R.string.inicio23),
                isSelected = selectedIndex == 0,
                onClick = { onItemSelected(0) }
            )
            BottomBarItem(
                icon = Icons.Outlined.DirectionsCar,
                selectedIcon = Icons.Default.DirectionsCar,
                label = stringResource(R.string.veh_culos23),
                isSelected = selectedIndex == 1,
                onClick = { onItemSelected(1) }
            )
            BottomBarItem(
                icon = Icons.Outlined.History,
                selectedIcon = Icons.Default.History,
                label = stringResource(R.string.historial3232),
                isSelected = selectedIndex == 2,
                onClick = { onItemSelected(2) }
            )
            BottomBarItem(
                icon = Icons.Outlined.Person,
                selectedIcon = Icons.Default.Person,
                label = stringResource(R.string.perfil12),
                isSelected = selectedIndex == 3,
                onClick = { onItemSelected(3) }
            )
        }
    }
}