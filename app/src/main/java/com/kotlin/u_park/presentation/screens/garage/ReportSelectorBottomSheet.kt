package com.kotlin.u_park.presentation.screens.garage

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kotlin.u_park.domain.model.ReportPeriod
import com.kotlin.u_park.domain.model.ReportType
import java.time.LocalDate
import java.time.LocalDateTime

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportSelectorBottomSheet(
    onDismiss: () -> Unit,
    onGenerateReport: (ReportType, LocalDateTime, LocalDateTime) -> Unit
) {
    var selectedReportType by remember { mutableStateOf<ReportType?>(null) }
    var selectedPeriod by remember { mutableStateOf<ReportPeriod?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var customStartDate by remember { mutableStateOf<LocalDate?>(null) }
    var customEndDate by remember { mutableStateOf<LocalDate?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Generar Reporte",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar")
                }
            }

            // Paso 1: Tipo de reporte
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "1. Tipo de reporte",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ReportTypeCard(
                        modifier = Modifier.weight(1f),
                        title = "Ocupación",
                        icon = Icons.Default.DirectionsCar,
                        isSelected = selectedReportType == ReportType.OCCUPANCY,
                        onClick = { selectedReportType = ReportType.OCCUPANCY }
                    )

                    ReportTypeCard(
                        modifier = Modifier.weight(1f),
                        title = "Ingresos",
                        icon = Icons.Default.AttachMoney,
                        isSelected = selectedReportType == ReportType.INCOME,
                        onClick = { selectedReportType = ReportType.INCOME }
                    )
                }
            }

            // Paso 2: Período
            if (selectedReportType != null) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "2. Período del reporte",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ReportPeriod.values().forEach { period ->
                            PeriodButton(
                                period = period,
                                isSelected = selectedPeriod == period,
                                onClick = {
                                    selectedPeriod = period
                                    if (period == ReportPeriod.CUSTOM) {
                                        showDatePicker = true
                                    }
                                }
                            )
                        }
                    }

                    // Mostrar fechas seleccionadas si es personalizado
                    if (selectedPeriod == ReportPeriod.CUSTOM &&
                        customStartDate != null &&
                        customEndDate != null) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "${customStartDate} → ${customEndDate}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // Paso 3: Botón generar
            if (selectedReportType != null && selectedPeriod != null) {
                val canGenerate = if (selectedPeriod == ReportPeriod.CUSTOM) {
                    customStartDate != null && customEndDate != null
                } else {
                    true
                }

                Button(
                    onClick = {
                        val (startDate, endDate) = calculateDateRange(
                            selectedPeriod!!,
                            customStartDate,
                            customEndDate
                        )
                        onGenerateReport(selectedReportType!!, startDate, endDate)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = canGenerate,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Generar PDF",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    // DatePicker para rango personalizado (implementación simplificada)
    if (showDatePicker) {
        AlertDialog(
            onDismissRequest = { showDatePicker = false },
            title = { Text("Seleccionar rango de fechas") },
            text = { Text("Implementa aquí tu DateRangePicker") },
            confirmButton = {
                TextButton(onClick = {
                    // Establecer fechas ejemplo
                    customStartDate = LocalDate.now().minusMonths(1)
                    customEndDate = LocalDate.now()
                    showDatePicker = false
                }) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun ReportTypeCard(
    modifier: Modifier = Modifier,
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(100.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(16.dp)
                    )
                } else Modifier
            ),
        color = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
private fun PeriodButton(
    period: ReportPeriod,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(12.dp)
                    )
                } else Modifier
            ),
        color = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = period.displayName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
            )
            if (isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun calculateDateRange(
    period: ReportPeriod,
    customStart: LocalDate?,
    customEnd: LocalDate?
): Pair<LocalDateTime, LocalDateTime> {
    return when (period) {
        ReportPeriod.CUSTOM -> {
            Pair(
                customStart!!.atStartOfDay(),
                customEnd!!.atTime(23, 59, 59)
            )
        }
        else -> {
            val endDate = LocalDateTime.now()
            val startDate = endDate.minusMonths(period.months.toLong())
            Pair(startDate, endDate)
        }
    }
}