package com.kotlin.u_park.presentation.screens.rates

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.kotlin.u_park.R

// 🎨 Modern Color System
private val PrimaryRed = Color(0xFFE60023)
private val DarkRed = Color(0xFFB8001C)
private val LightRed = Color(0xFFFFE5E9)
private val BackgroundColor = Color(0xFFFAFAFA)
private val SurfaceColor = Color(0xFFFFFFFF)
private val TextPrimary = Color(0xFF0D0D0D)
private val TextSecondary = Color(0xFF6E6E73)
private val BorderColor = Color(0xFFE5E5EA)
private val SuccessGreen = Color(0xFF34C759)
private val InfoBlue = Color(0xFF007AFF)
private val WarningOrange = Color(0xFFFF9500)
private val ErrorRed = Color(0xFFFF3B30)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RateFormScreen(
    navController: NavController,
    viewModel: RatesViewModel,
    userId: String,
    garageId: String,
    rateId: String,
    onSaved: () -> Unit
) {
    val editing by viewModel.editingRate
    val vehicleTypes by viewModel.vehicleTypes
    val garages by viewModel.garages
    val saving by viewModel.saving
    val saveSuccess by viewModel.saveSuccess

    // Auto-navigate on success
    LaunchedEffect(saveSuccess) {
        if (saveSuccess) {
            viewModel.saveSuccess.value = false
            onSaved()
        }
    }

    // Load data
    LaunchedEffect(userId) {
        viewModel.loadVehicleTypes()
        viewModel.loadGarages(userId)
    }

    LaunchedEffect(rateId) {
        if (rateId != "new") {
            val allRates = viewModel.groupedRates.value.values.flatten()
            val found = allRates.firstOrNull { it.id == rateId }
            viewModel.setEditing(found)
        } else {
            viewModel.setEditing(null)
        }
    }

    // Form state
    var expandedUnit by remember { mutableStateOf(false) }
    var expandedVehicleType by remember { mutableStateOf(false) }
    var expandedGarage by remember { mutableStateOf(false) }

    var baseRateText by remember(editing) { mutableStateOf(editing?.baseRate?.toString() ?: "") }

    val units = listOf("hora", "día", "semana", "mes")
    var selectedUnit by remember(editing) { mutableStateOf(editing?.timeUnit ?: "hora") }

    var selectedVehicleType by remember(editing) { mutableStateOf<Int?>(editing?.vehicleTypeId) }
    var selectedGarageId by remember(editing) { mutableStateOf(editing?.garageId ?: garageId) }

    val dias = listOf(stringResource(R.string.lunes),
        stringResource(R.string.martes),
        stringResource(R.string.mi_rcoles),
        stringResource(R.string.jueves), stringResource(R.string.viernes),
        stringResource(R.string.s_bado), stringResource(R.string.domingo)
    )
    var selectedDays by remember(editing) { mutableStateOf(editing?.diasAplicables ?: dias) }

    var showError by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            Surface(
                color = SurfaceColor,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Surface(
                        onClick = { navController.popBackStack() },
                        shape = CircleShape,
                        color = BackgroundColor
                    ) {
                        Box(
                            modifier = Modifier.size(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Volver",
                                tint = TextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            if (rateId == "new") stringResource(R.string.nueva_tarifa) else stringResource(
                                R.string.editar_tarifa
                            ),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            stringResource(R.string.configuraci_n_de_precios),
                            fontSize = 13.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // Error Banner
            if (showError) {
                item {
                    ErrorBanner()
                }
            }

            // Garage Section
            item {
                SectionHeaderModern(
                    icon = Icons.Outlined.Warehouse,
                    title = stringResource(R.string.informaci_n_del_garage)
                )
            }

            item {
                FormCard {
                    ExposedDropdownMenuBox(
                        expanded = expandedGarage,
                        onExpandedChange = { expandedGarage = it }
                    ) {
                        OutlinedTextField(
                            value = garages.firstOrNull { it.first == selectedGarageId }?.second
                                ?: stringResource(R.string.seleccionar_garage),
                            onValueChange = {},
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            readOnly = true,
                            label = { Text(stringResource(R.string.garage2)) },
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.Warehouse,
                                    contentDescription = null,
                                    tint = if (showError && selectedGarageId.isEmpty()) ErrorRed else PrimaryRed
                                )
                            },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedGarage)
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryRed,
                                focusedLabelColor = PrimaryRed,
                                unfocusedBorderColor = BorderColor,
                                errorBorderColor = ErrorRed
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
                                            tint = if (selectedGarageId == id) PrimaryRed else Color.Transparent
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Rate Configuration Section
            item {
                SectionHeaderModern(
                    icon = Icons.Outlined.PriceChange,
                    title = stringResource(R.string.configuraci_n_de_tarifa)
                )
            }

            item {
                FormCard {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        // Base Rate
                        OutlinedTextField(
                            value = baseRateText,
                            onValueChange = { baseRateText = it },
                            label = { Text(stringResource(R.string.precio_base_rd)) },
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.AttachMoney,
                                    contentDescription = null,
                                    tint = if (showError && (baseRateText.toDoubleOrNull() == null || baseRateText.isEmpty())) ErrorRed else PrimaryRed
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryRed,
                                focusedLabelColor = PrimaryRed,
                                unfocusedBorderColor = BorderColor,
                                errorBorderColor = ErrorRed
                            ),
                            shape = RoundedCornerShape(12.dp),
                            isError = showError && (baseRateText.toDoubleOrNull() == null || baseRateText.isEmpty())
                        )

                        // Time Unit
                        ExposedDropdownMenuBox(
                            expanded = expandedUnit,
                            onExpandedChange = { expandedUnit = it }
                        ) {
                            OutlinedTextField(
                                value = selectedUnit.capitalize(),
                                onValueChange = {},
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                readOnly = true,
                                label = { Text(stringResource(R.string.unidad_de_tiempo)) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Outlined.Schedule,
                                        contentDescription = null,
                                        tint = PrimaryRed
                                    )
                                },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedUnit)
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryRed,
                                    focusedLabelColor = PrimaryRed,
                                    unfocusedBorderColor = BorderColor
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )

                            ExposedDropdownMenu(
                                expanded = expandedUnit,
                                onDismissRequest = { expandedUnit = false }
                            ) {
                                units.forEach { unit ->
                                    DropdownMenuItem(
                                        text = { Text(unit.capitalize()) },
                                        onClick = {
                                            selectedUnit = unit
                                            expandedUnit = false
                                        },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Outlined.CheckCircle,
                                                contentDescription = null,
                                                tint = if (selectedUnit == unit) PrimaryRed else Color.Transparent
                                            )
                                        }
                                    )
                                }
                            }
                        }

                        // Vehicle Type
                        ExposedDropdownMenuBox(
                            expanded = expandedVehicleType,
                            onExpandedChange = { expandedVehicleType = it }
                        ) {
                            OutlinedTextField(
                                value = vehicleTypes.firstOrNull { it.first == selectedVehicleType }?.second
                                    ?: stringResource(R.string.cualquiera),
                                onValueChange = {},
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                readOnly = true,
                                label = { Text(stringResource(R.string.tipo_de_veh_culo)) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Outlined.DirectionsCar,
                                        contentDescription = null,
                                        tint = PrimaryRed
                                    )
                                },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedVehicleType)
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryRed,
                                    focusedLabelColor = PrimaryRed,
                                    unfocusedBorderColor = BorderColor
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )

                            ExposedDropdownMenu(
                                expanded = expandedVehicleType,
                                onDismissRequest = { expandedVehicleType = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.cualquiera2)) },
                                    onClick = {
                                        selectedVehicleType = null
                                        expandedVehicleType = false
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Outlined.CheckCircle,
                                            contentDescription = null,
                                            tint = if (selectedVehicleType == null) PrimaryRed else Color.Transparent
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
                                                tint = if (selectedVehicleType == id) PrimaryRed else Color.Transparent
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Days Section
            item {
                SectionHeaderModern(
                    icon = Icons.Outlined.CalendarMonth,
                    title = stringResource(R.string.d_as_aplicables)
                )
            }

            item {
                FormCard {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        dias.forEach { dia ->
                            val isSelected = selectedDays.contains(dia)

                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) PrimaryRed.copy(alpha = 0.08f) else Color.Transparent
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = { checked ->
                                            selectedDays = if (checked) selectedDays + dia else selectedDays - dia
                                        },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = PrimaryRed,
                                            checkmarkColor = SurfaceColor
                                        )
                                    )
                                    Text(
                                        dia.capitalize(),
                                        fontSize = 15.sp,
                                        color = if (isSelected) TextPrimary else TextSecondary,
                                        fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Action Buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Clear Button
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
                            contentColor = PrimaryRed
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
                        Text(
                            stringResource(R.string.limpiar),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Save Button
                    Button(
                        onClick = {
                            val price = baseRateText.toDoubleOrNull()
                            if (price == null || price <= 0 || selectedGarageId.isEmpty()) {
                                showError = true
                                return@Button
                            }

                            showError = false
                            viewModel.saveRate(
                                garageId = selectedGarageId,
                                baseRate = price,
                                timeUnit = selectedUnit,
                                vehicleTypeId = selectedVehicleType,
                                diasAplicables = selectedDays,
                                specialRate = null
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryRed,
                            contentColor = SurfaceColor
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
                                color = SurfaceColor,
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
                            if (saving) stringResource(R.string.guardando2) else stringResource(R.string.guardar),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeaderModern(
    icon: ImageVector,
    title: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(PrimaryRed.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = PrimaryRed,
                modifier = Modifier.size(20.dp)
            )
        }

        Text(
            title,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
    }
}

@Composable
fun FormCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = SurfaceColor,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
fun ErrorBanner() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = ErrorRed.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Outlined.Warning,
                contentDescription = null,
                tint = ErrorRed,
                modifier = Modifier.size(24.dp)
            )
            Text(
                stringResource(R.string.por_favor_completa_todos_los_campos_requeridos),
                fontSize = 14.sp,
                color = ErrorRed,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

fun String.capitalize() = replaceFirstChar {
    if (it.isLowerCase()) it.titlecase() else it.toString()
}