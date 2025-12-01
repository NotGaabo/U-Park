package com.kotlin.u_park.presentation.screens.rates

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RateFormScreen(
    navController: NavController,
    viewModel: RatesViewModel,
    garageId: String,
    rateId: String,
    onSaved: () -> Unit
) {
    val editing by viewModel.editingRate
    val vehicleTypes by viewModel.vehicleTypes
    val garages by viewModel.garages

    // Load needed data
    LaunchedEffect(true) {
        viewModel.loadVehicleTypes()
        viewModel.loadGarages()
    }

    LaunchedEffect(rateId) {
        val found = viewModel.rates.value.firstOrNull { it.id == rateId }
        viewModel.setEditing(found)
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

    val dias = listOf("lunes","martes","miércoles","jueves","viernes","sábado","domingo")
    var selectedDays by remember(editing) { mutableStateOf(editing?.diasAplicables ?: dias) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (rateId == "new") "Nueva Tarifa" else "Editar Tarifa") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // -------------------------
            // SECTION: GARAGE
            // -------------------------
            Text("Información del Garage", style = MaterialTheme.typography.titleMedium)

            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {

                    // GARAGE SELECT
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
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedGarage) }
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
            Text("Tarifa", style = MaterialTheme.typography.titleMedium)

            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    Modifier
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    // PRECIO BASE
                    OutlinedTextField(
                        value = baseRateText,
                        onValueChange = { baseRateText = it },
                        label = { Text("Precio Base (RD$)") },
                        modifier = Modifier.fillMaxWidth()
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
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedUnit) }
                        )

                        ExposedDropdownMenu(
                            expanded = expandedUnit,
                            onDismissRequest = { expandedUnit = false }
                        ) {
                            units.forEach { unit ->
                                DropdownMenuItem(
                                    text = { Text(unit) },
                                    onClick = {
                                        selectedUnit = unit
                                        expandedUnit = false
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
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedVehicleType) }
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
                                }
                            )

                            vehicleTypes.forEach { (id, name) ->
                                DropdownMenuItem(
                                    text = { Text(name) },
                                    onClick = {
                                        selectedVehicleType = id
                                        expandedVehicleType = false
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
            Text("Días aplicables", style = MaterialTheme.typography.titleMedium)

            ElevatedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    dias.forEach { dia ->
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = selectedDays.contains(dia),
                                onCheckedChange = { checked ->
                                    selectedDays =
                                        if (checked) selectedDays + dia else selectedDays - dia
                                }
                            )
                            Text(dia.replaceFirstChar { it.uppercase() })
                        }
                    }
                }
            }

            // -------------------------
            // ➕ AGREGAR TARIFA EXTRA
            // -------------------------
            OutlinedButton(
                onClick = {
                    // Acción temporaria (la puedes cambiar)
                    baseRateText = ""
                    selectedUnit = "hora"
                    selectedVehicleType = null
                    selectedDays = dias
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Agregar otra tarifa")
            }

            // -------------------------
            // GUARDAR
            // -------------------------
            Button(
                onClick = {
                    viewModel.saveRate(
                        garageId = selectedGarageId,
                        baseRate = baseRateText.toDoubleOrNull() ?: 0.0,
                        timeUnit = selectedUnit,
                        vehicleTypeId = selectedVehicleType,
                        diasAplicables = selectedDays,
                        specialRate = null
                    )
                    onSaved()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar Tarifa")
            }
        }
    }
}
