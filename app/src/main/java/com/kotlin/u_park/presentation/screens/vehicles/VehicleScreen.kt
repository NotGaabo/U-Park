package com.kotlin.u_park.presentation.screens.vehicles

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.kotlin.u_park.domain.model.Vehicle
import com.kotlin.u_park.domain.model.VehicleTypeSimple
import com.kotlin.u_park.presentation.navigation.Routes
import kotlinx.coroutines.launch
import java.util.*


private val RedSoft = Color(0xFFE60023)
private val BackgroundColor = Color(0xFFF5F5F5)
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

    val redPrimary = Color(0xFFE60023)
    val backgroundGray = Color(0xFFF5F7FA)

    LaunchedEffect(userId) {
        userId?.let {
            viewModel.loadVehicles(it)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Tus Vehiculos", fontWeight = FontWeight.Bold, color = RedSoft)
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundColor)
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(
                    selected = false,
                    onClick = {
                        navController.navigate("home")
                    },
                    icon = { Icon(Icons.Default.Home, null) },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = true,
                    onClick = {},
                    icon = { Icon(Icons.Default.CarCrash, null, tint = RedSoft) },
                    label = { Text("Vehiculos", color = RedSoft) }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = {
                        userId?.let {
                            navController.navigate(
                                Routes.HistorialParking.createRoute(it)
                            )
                        }
                    },
                    icon = { Icon(Icons.Default.History, null) },
                    label = { Text("Historial") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("settings") },
                    icon = { Icon(Icons.Default.Person, null) },
                    label = { Text("Perfil") }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSheet = true },
                containerColor = redPrimary,
                contentColor = Color.White,
                modifier = Modifier.size(64.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Agregar vehículo",
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        containerColor = backgroundGray
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = redPrimary)
                    }
                }
                vehicles.isEmpty() -> {
                    EmptyVehiclesState(onAddVehicle = { showAddSheet = true })
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.DirectionsCar,
                contentDescription = null,
                modifier = Modifier.size(120.dp),
                tint = Color.Gray.copy(alpha = 0.3f)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "No tienes vehículos registrados",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2D3436)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Agrega tu primer vehículo para empezar",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onAddVehicle,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE60023)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(48.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Agregar Vehículo",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleCard(
    vehicle: Vehicle,
    onDelete: (String) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val redPrimary = Color(0xFFE60023)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF5F7FA)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.DirectionsCar,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = redPrimary
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    vehicle.model ?: "Sin modelo",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2D3436)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CarRepair,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        vehicle.plate,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
                if (vehicle.color != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Palette,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            vehicle.color,
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
                if (vehicle.year != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            vehicle.year.toString(),
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            IconButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        Color(0xFFFFEBEE),
                        RoundedCornerShape(10.dp)
                    )
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = Color(0xFFD32F2F),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFD32F2F),
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    "¿Eliminar vehículo?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("Esta acción no se puede deshacer. ¿Deseas continuar?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete(vehicle.id ?: "")
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD32F2F)
                    )
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar", color = Color.Gray)
                }
            }
        )
    }
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

    // 🔹 Estados del formulario
    var plate by rememberSaveable { mutableStateOf("") }
    var model by rememberSaveable { mutableStateOf("") }
    var color by rememberSaveable { mutableStateOf("") }
    var year by rememberSaveable { mutableStateOf("") }

    // 🔹 Estados del SELECT (CORRECTO AQUÍ)
    var expanded by remember { mutableStateOf(false) }
    var selectedType by remember { mutableStateOf<VehicleTypeSimple?>(null) }

    val vehicleTypes by viewModel.vehicleTypes.collectAsState()

    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val isLoading by viewModel.isLoading.collectAsState()
    val isSuccess by viewModel.isSuccess.collectAsState()

    val redPrimary = Color(0xFFE60023)
    val backgroundGray = Color(0xFFF5F7FA)

    val isFormValid =
        plate.isNotBlank() &&
                model.isNotBlank() &&
                color.isNotBlank() &&
                selectedType != null


    LaunchedEffect(Unit) {
        viewModel.loadVehicleTypes()
    }

    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            viewModel.resetStatus()
            onSuccess()
        }
    }


    ModalBottomSheet(
        onDismissRequest = onDismiss,
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Nuevo Vehículo",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2D3436)
                    )
                    Text(
                        text = "Completa la información",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }

                IconButton(
                    onClick = onDismiss,
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

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = backgroundGray
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.DirectionsCar,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Color.Gray.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Información del vehículo",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column {
                Text(
                    "Placa *",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF2D3436)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = plate,
                    onValueChange = {
                        plate = it.uppercase()
                        showError = false
                    },
                    placeholder = { Text("ABC-1234", color = Color.Gray.copy(alpha = 0.6f)) },
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
                            Icons.Default.Pin,
                            contentDescription = null,
                            tint = if (plate.isNotBlank()) redPrimary else Color.Gray
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column {
                Text(
                    "Modelo *",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF2D3436)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = model,
                    onValueChange = {
                        model = it
                        showError = false
                    },
                    placeholder = { Text("Toyota Corolla", color = Color.Gray.copy(alpha = 0.6f)) },
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
                            Icons.Default.CarRepair,
                            contentDescription = null,
                            tint = if (model.isNotBlank()) redPrimary else Color.Gray
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Color *",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF2D3436)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = color,
                        onValueChange = {
                            color = it
                            showError = false
                        },
                        placeholder = { Text("Rojo", color = Color.Gray.copy(alpha = 0.6f)) },
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
                                Icons.Default.Palette,
                                contentDescription = null,
                                tint = if (color.isNotBlank()) redPrimary else Color.Gray
                            )
                        }
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Año",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF2D3436)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = year,
                        onValueChange = {
                            if (it.all { char -> char.isDigit() } && it.length <= 4) {
                                year = it
                            }
                        },
                        placeholder = { Text("2024", color = Color.Gray.copy(alpha = 0.6f)) },
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
                                Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = if (year.isNotBlank()) redPrimary else Color.Gray
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column {
                Text(
                    "Tipo de Vehículo *",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF2D3436)
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
                        placeholder = {
                            Text(
                                "Selecciona un tipo",
                                color = Color.Gray.copy(alpha = 0.6f)
                            )
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Category,
                                contentDescription = null,
                                tint = if (selectedType != null) redPrimary else Color.Gray
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = redPrimary,
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = backgroundGray
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

            Button(
                onClick = {
                    if (!isFormValid) {
                        showError = true
                        errorMessage = "Por favor completa los campos obligatorios (*)"
                        return@Button
                    }

                    if (userId.isNullOrEmpty()) {
                        showError = true
                        errorMessage = "Error de sesión. Inicia sesión nuevamente"
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
                        "Guardando...",
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
                        "Guardar Vehículo",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}