package com.kotlin.u_park.presentation.screens.employee

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kotlin.u_park.R
import com.kotlin.u_park.domain.model.EmpleadoGarage

// 🎨 Color System
private val PrimaryRed = Color(0xFFE60023)
private val RedLight = Color(0xFFFF6B6B)
private val TextPrimary = Color(0xFF0F172A)
private val TextSecondary = Color(0xFF64748B)
private val BackgroundGray = Color(0xFFF5F7FA)
private val SurfaceColor = Color(0xFFFFFFFF)
private val SuccessGreen = Color(0xFF34C759)
private val InfoBlue = Color(0xFF007AFF)

@Composable
fun EmpleadosScreen(
    garageId: String,
    viewModel: EmpleadosViewModel,
    onAgregarEmpleado: () -> Unit
) {
    val empleados by viewModel.empleados.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGray)
    ) {
        when {
            isLoading -> LoadingState()
            empleados.isEmpty() -> EmptyState(onAgregarEmpleado)
            else -> EmpleadosList(
                empleados = empleados,
                onDelete = { cedula -> viewModel.removeEmpleado(garageId, cedula) }
            )
        }

        // FAB flotante
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            FloatingActionButton(
                onClick = onAgregarEmpleado,
                containerColor = PrimaryRed,
                contentColor = Color.White,
                shape = RoundedCornerShape(14.dp),
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 8.dp
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        }
    }
}

@Composable
private fun EmpleadosList(
    empleados: List<EmpleadoGarage>,
    onDelete: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Section
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    stringResource(R.string.empleados),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    "${empleados.size} ${if (empleados.size == 1) stringResource(R.string.empleado_registrado) else stringResource(
                        R.string.empleados_registrados
                    )}",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Stats Card
        item {
            StatsCard(empleados.size)
        }

        // Section Header
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Icon(
                    Icons.Outlined.Group,
                    contentDescription = null,
                    tint = TextPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    stringResource(R.string.tu_equipo),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        }

        // Employees List
        items(empleados, key = { it.empleado_id }) { empleado ->
            ModernEmpleadoCard(
                empleado = empleado,
                onDelete = { onDelete(empleado.empleado_id) }
            )
        }

        item { Spacer(Modifier.height(80.dp)) }
    }
}

@Composable
private fun StatsCard(total: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = SurfaceColor,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Outlined.Analytics,
                    contentDescription = null,
                    tint = PrimaryRed,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    stringResource(R.string.resumen_del_equipo),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    icon = Icons.Outlined.Group,
                    value = "$total",
                    label = stringResource(R.string.empleados2),
                    color = PrimaryRed
                )

                StatItem(
                    icon = Icons.Outlined.CheckCircle,
                    value = "$total",
                    label = stringResource(R.string.activos),
                    color = SuccessGreen
                )

                StatItem(
                    icon = Icons.Outlined.Badge,
                    value = "$total",
                    label = stringResource(R.string.registrados),
                    color = InfoBlue
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    icon: ImageVector,
    value: String,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(color.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }

        Text(
            value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Text(
            label,
            fontSize = 12.sp,
            color = TextSecondary,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ModernEmpleadoCard(
    empleado: EmpleadoGarage,
    onDelete: () -> Unit
) {
    val user = empleado.users
    var expanded by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = SurfaceColor,
        shadowElevation = 2.dp
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.linearGradient(
                                listOf(PrimaryRed, RedLight)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        (user?.nombre?.firstOrNull()?.uppercase() ?: "?"),
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.width(12.dp))

                // Employee Info
                Column(Modifier.weight(1f)) {
                    Text(
                        user?.nombre ?: stringResource(R.string.nombre_no_disponible),
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // ID Badge
                        Surface(
                            color = PrimaryRed.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                "ID: ${empleado.empleado_id}",
                                fontSize = 11.sp,
                                color = PrimaryRed,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        // Status Badge
                        Surface(
                            color = SuccessGreen.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(SuccessGreen, CircleShape)
                                )
                                Text(
                                    stringResource(R.string.activo),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SuccessGreen
                                )
                            }
                        }
                    }
                }

                IconButton(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = TextSecondary
                    )
                }
            }

            // Expanded Content
            AnimatedVisibility(expanded) {
                Column {
                    Spacer(Modifier.height(12.dp))
                    Divider(color = BackgroundGray)
                    Spacer(Modifier.height(12.dp))

                    InfoRow(Icons.Outlined.Email, "Correo", user?.correo)
                    InfoRow(Icons.Outlined.Phone, "Teléfono", user?.telefono)
                    InfoRow(Icons.Outlined.Badge, "Cédula", empleado.empleado_id.toString())
                    InfoRow(Icons.Outlined.CalendarToday, "Registrado", empleado.fecha_registro)

                    Spacer(Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { showDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.Red
                        )
                    ) {
                        Icon(
                            Icons.Outlined.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.eliminar_empleado), fontSize = 13.sp)
                    }
                }
            }
        }
    }

    if (showDialog) {
        DeleteDialog(
            name = user?.nombre,
            onDismiss = { showDialog = false },
            onConfirm = {
                onDelete()
                showDialog = false
            }
        )
    }
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String?) {
    if (value.isNullOrBlank()) return

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            null,
            tint = PrimaryRed.copy(alpha = 0.7f),
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(12.dp))

        Column {
            Text(
                label,
                fontSize = 11.sp,
                color = TextSecondary
            )
            Text(
                value,
                fontSize = 14.sp,
                color = TextPrimary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun DeleteDialog(
    name: String?,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = Color.Red,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(stringResource(R.string.confirmar_eliminaci_n))
        },
        text = {
            Text(
                stringResource(
                    R.string.est_s_seguro_de_que_deseas_eliminar_a_esta_acci_n_no_se_puede_deshacer,
                    name ?: stringResource(R.string.este_empleado)
                ))
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text(stringResource(R.string.eliminar))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancelar2))
            }
        }
    )
}

@Composable
private fun EmptyState(onAgregarEmpleado: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(PrimaryRed.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Outlined.PersonOff,
                contentDescription = null,
                tint = PrimaryRed,
                modifier = Modifier.size(50.dp)
            )
        }

        Spacer(Modifier.height(24.dp))

        Text(
            stringResource(R.string.no_hay_empleados_registrados),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(Modifier.height(8.dp))

        Text(
            stringResource(R.string.agrega_tu_primer_empleado_para_comenzar_a_gestionar_tu_equipo),
            color = TextSecondary,
            fontSize = 14.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            lineHeight = 20.sp
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = onAgregarEmpleado,
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.height(50.dp),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 4.dp,
                pressedElevation = 8.dp
            )
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.agregar_empleado3), fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun LoadingState() {
    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            color = PrimaryRed,
            strokeWidth = 3.dp,
            modifier = Modifier.size(48.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            stringResource(R.string.cargando_empleados),
            color = TextSecondary,
            fontSize = 14.sp
        )
    }
}