package com.kotlin.u_park.presentation.screens.rates

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

private val RedSoft = Color(0xFFE60023)
private val BackgroundColor = Color(0xFFF5F5F5)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RateFormScreen(
    navController: NavController,
    viewModel: RatesViewModel,
    userId: String, // 🔥 NUEVO: necesitamos el userId
    garageId: String,
    rateId: String,
    onSaved: () -> Unit
) {
    val editing by viewModel.editingRate
    val vehicleTypes by viewModel.vehicleTypes
    val garages by viewModel.garages
    val saving by viewModel.saving
    val saveSuccess by viewModel.saveSuccess

    // 🔥 NUEVO: Observar cuando termine de guardar exitosamente
    LaunchedEffect(saveSuccess) {
        if (saveSuccess) {
            viewModel.saveSuccess.value = false // Reset
            onSaved()
        }
    }

    // Load needed data
    LaunchedEffect(userId) {
        viewModel.loadVehicleTypes()
        viewModel.loadGarages(userId) // 🔥 Cargar con userId
    }

    LaunchedEffect(rateId) {
        if (rateId != "new") {
            // 🔥 Buscar en groupedRates
            val allRates = viewModel.groupedRates.value.values.flatten()
            val found = allRates.firstOrNull { it.id == rateId }
            viewModel.setEditing(found)
        } else {
            viewModel.setEditing(null)
        }
    }

    // Dropdown expands
    var expandedUnit by remember { mutableStateOf(false) }
    var expandedVehicleType by remember { mutableStateOf(false) }
    var expandedGarage by remember { mutableStateOf(false) }

    // Fields
    var baseRateText by remember(editing) { mutableStateOf(editing?.baseRate?.toString() ?: "") }

    val units = listOf("hora", "día", "semana", "mes")
    var selectedUnit by remember(editing) { mutableStateOf(editing?.timeUnit ?: "hora") }

    var selectedVehicleType by remember(editing) { mutableStateOf<Int?>(editing?.vehicleTypeId) }

    var selectedGarageId by remember(editing) { mutableStateOf(editing?.garageId ?: garageId) }

    val dias = listOf("lunes", "martes", "miércoles", "jueves", "viernes", "sábado", "domingo")
    var selectedDays by remember(editing) { mutableStateOf(editing?.diasAplicables ?: dias) }

    // 🔥 NUEVO: Validación
    var showError by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            if (rateId == "new") "Nueva Tarifa" else "Editar Tarifa",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Text(
                            "Configuración de precios",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = RedSoft,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundColor)
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // -------------------------
            // HEADER CARD
            // -------------------------
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    RedSoft.copy(alpha = 0.1f),
                                    Color.White
                                )
                            )
                        )
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(60.dp),
                        shape = CircleShape,
                        color = RedSoft.copy(alpha = 0.2f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.AttachMoney,
                                contentDescription = null,
                                tint = RedSoft,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    Spacer(Modifier.width(16.dp))

                    Column {
                        Text(
                            if (rateId == "new") "Crear nueva tarifa" else "Modificar tarifa",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A1A)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Completa la información requerida",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            // 🔥 NUEVO: Mostrar errores
            if (showError) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFEBEE)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.Warning,
                            null,
                            tint = Color(0xFFD32F2F)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Por favor completa todos los campos requeridos",
                            color = Color(0xFFD32F2F)
                        )
                    }
                }
            }

            // -------------------------
            // SECTION: GARAGE
            // -------------------------
            SectionHeader(
                icon = Icons.Outlined.Warehouse,
                title = "Información del Garage"
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    ExposedDropdownMenuBox(
                        expanded = expandedGarage,
                        onExpandedChange = { expandedGarage = it }
                    ) {
                        OutlinedTextField(
                            value = garages.firstOrNull { it.first == selectedGarageId }?.second
                                ?: "Seleccionar Garage",
                            onValueChange = {},
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            readOnly = true,
                            label = { Text("Garage") },
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.Warehouse,
                                    contentDescription = null,
                                    tint = RedSoft
                                )
                            },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedGarage)
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = RedSoft,
                                focusedLabelColor = RedSoft
                            ),
                            shape = RoundedCornerShape(12.dp),
                            isError = showError && selectedGarageId.isEmpty()
                        )

                        ExposedDropdownMenu(
                            expanded = expandedGarage,
                            onDismissRequest = { expandedGarage = false }
                        ) {
                            garages.forEach { (id, name) ->
                                DropdownMenuItem(
                                    text = { Text(name) },
                                    onClick = {
                                        selectedGarageId = id
                                        expandedGarage = false
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Outlined.CheckCircle,
                                            contentDescription = null,
                                            tint = if (selectedGarageId == id) RedSoft else Color.Transparent
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // -------------------------
            // SECTION: TARIFA
            // -------------------------
            SectionHeader(
                icon = Icons.Outlined.PriceChange,
                title = "Configuración de Tarifa"
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    // PRECIO BASE
                    OutlinedTextField(
                        value = baseRateText,
                        onValueChange = { baseRateText = it },
                        label = { Text("Precio Base (RD$)") },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.AttachMoney,
                                contentDescription = null,
                                tint = RedSoft
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RedSoft,
                            focusedLabelColor = RedSoft
                        ),
                        shape = RoundedCornerShape(12.dp),
                        isError = showError && (baseRateText.toDoubleOrNull() == null || baseRateText.isEmpty())
                    )

                    // UNIDAD (SELECT)
                    ExposedDropdownMenuBox(
                        expanded = expandedUnit,
                        onExpandedChange = { expandedUnit = it }
                    ) {
                        OutlinedTextField(
                            value = selectedUnit,
                            onValueChange = {},
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            readOnly = true,
                            label = { Text("Unidad de tiempo") },
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.Schedule,
                                    contentDescription = null,
                                    tint = RedSoft
                                )
                            },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedUnit)
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = RedSoft,
                                focusedLabelColor = RedSoft
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        ExposedDropdownMenu(
                            expanded = expandedUnit,
                            onDismissRequest = { expandedUnit = false }
                        ) {
                            units.forEach { unit ->
                                DropdownMenuItem(
                                    text = { Text(unit.replaceFirstChar { it.uppercase() }) },
                                    onClick = {
                                        selectedUnit = unit
                                        expandedUnit = false
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Outlined.CheckCircle,
                                            contentDescription = null,
                                            tint = if (selectedUnit == unit) RedSoft else Color.Transparent
                                        )
                                    }
                                )
                            }
                        }
                    }

                    // VEHICLE TYPE SELECT
                    ExposedDropdownMenuBox(
                        expanded = expandedVehicleType,
                        onExpandedChange = { expandedVehicleType = it }
                    ) {
                        OutlinedTextField(
                            value = vehicleTypes.firstOrNull { it.first == selectedVehicleType }?.second
                                ?: "Cualquiera",
                            onValueChange = {},
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            readOnly = true,
                            label = { Text("Tipo de vehículo") },
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.DirectionsCar,
                                    contentDescription = null,
                                    tint = RedSoft
                                )
                            },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedVehicleType)
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = RedSoft,
                                focusedLabelColor = RedSoft
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        ExposedDropdownMenu(
                            expanded = expandedVehicleType,
                            onDismissRequest = { expandedVehicleType = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Cualquiera") },
                                onClick = {
                                    selectedVehicleType = null
                                    expandedVehicleType = false
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Outlined.CheckCircle,
                                        contentDescription = null,
                                        tint = if (selectedVehicleType == null) RedSoft else Color.Transparent
                                    )
                                }
                            )

                            vehicleTypes.forEach { (id, name) ->
                                DropdownMenuItem(
                                    text = { Text(name) },
                                    onClick = {
                                        selectedVehicleType = id
                                        expandedVehicleType = false
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Outlined.CheckCircle,
                                            contentDescription = null,
                                            tint = if (selectedVehicleType == id) RedSoft else Color.Transparent
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // -------------------------
            // SECTION: DÍAS
            // -------------------------
            SectionHeader(
                icon = Icons.Outlined.CalendarMonth,
                title = "Días Aplicables"
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    dias.forEach { dia ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = if (selectedDays.contains(dia)) {
                                RedSoft.copy(alpha = 0.08f)
                            } else {
                                Color.Transparent
                            }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = selectedDays.contains(dia),
                                    onCheckedChange = { checked ->
                                        selectedDays =
                                            if (checked) selectedDays + dia else selectedDays - dia
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = RedSoft,
                                        checkmarkColor = Color.White
                                    )
                                )
                                Text(
                                    dia.replaceFirstChar { it.uppercase() },
                                    fontSize = 15.sp,
                                    color = if (selectedDays.contains(dia)) {
                                        Color(0xFF1A1A1A)
                                    } else {
                                        Color.Gray
                                    },
                                    fontWeight = if (selectedDays.contains(dia)) {
                                        FontWeight.Medium
                                    } else {
                                        FontWeight.Normal
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // -------------------------
            // ACTIONS
            // -------------------------
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // LIMPIAR FORMULARIO
                OutlinedButton(
                    onClick = {
                        baseRateText = ""
                        selectedUnit = "hora"
                        selectedVehicleType = null
                        selectedDays = dias
                        showError = false
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = RedSoft
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        width = 1.5.dp
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !saving
                ) {
                    Icon(
                        Icons.Outlined.RestartAlt,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Limpiar", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                }

                // GUARDAR
                Button(
                    onClick = {
                        // 🔥 Validación
                        val price = baseRateText.toDoubleOrNull()
                        if (price == null || price <= 0 || selectedGarageId.isEmpty()) {
                            showError = true
                            return@Button
                        }

                        showError = false

                        println("🔥 Guardando con garageId: $selectedGarageId") // DEBUG

                        viewModel.saveRate(
                            garageId = selectedGarageId,
                            baseRate = price,
                            timeUnit = selectedUnit,
                            vehicleTypeId = selectedVehicleType,
                            diasAplicables = selectedDays,
                            specialRate = null
                        )
                        // 🔥 Ya NO llamamos a onSaved() aquí
                        // Se llamará automáticamente cuando saveSuccess sea true
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RedSoft,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 8.dp
                    ),
                    enabled = !saving
                ) {
                    if (saving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            Icons.Outlined.Save,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (saving) "Guardando..." else "Guardar",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Espacio al final
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun SectionHeader(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Surface(
            modifier = Modifier.size(32.dp),
            shape = CircleShape,
            color = RedSoft.copy(alpha = 0.15f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = RedSoft,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        Text(
            title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A)
        )
    }
}