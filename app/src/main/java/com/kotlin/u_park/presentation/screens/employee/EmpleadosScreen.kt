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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kotlin.u_park.domain.model.EmpleadoGarage


// ——————————————————————————————
//     COLORES
// ——————————————————————————————
private val RedPrimary = Color(0xFFE60023)
private val RedLight = Color(0xFFFF6B6B)
private val TextPrimary = Color(0xFF0F172A)
private val TextSecondary = Color(0xFF64748B)
private val BackgroundGray = Color(0xFFF5F7FA)


// ——————————————————————————————
//     PANTALLA PRINCIPAL (SIN SCAFFOLD)
// ——————————————————————————————
@Composable
fun EmpleadosScreen(
    garageId: String,
    viewModel: EmpleadosViewModel,
    onAgregarEmpleado: () -> Unit
) {
    val empleados by viewModel.empleados.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(garageId) { viewModel.loadEmpleados(garageId) }

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
                containerColor = RedPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(14.dp),
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        }
    }
}


// ——————————————————————————————
//     LISTA
// ——————————————————————————————
@Composable
private fun EmpleadosList(
    empleados: List<EmpleadoGarage>,
    onDelete: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header con título
        item {
            Column {
                Text(
                    "Empleados",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    "Gestiona tu equipo de trabajo",
                    fontSize = 14.sp,
                    color = TextSecondary
                )
                Spacer(Modifier.height(16.dp))
            }
        }

        item { HeaderCard(empleados.size) }

        items(empleados, key = { it.empleado_id }) { empleado ->
            CompactEmpleadoCard(
                empleado = empleado,
                onDelete = { onDelete(empleado.empleado_id) }
            )
        }

        item { Spacer(Modifier.height(80.dp)) }
    }
}


// ——————————————————————————————
//     CARD CONTADOR
// ——————————————————————————————
@Composable
fun HeaderCard(total: Int) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = RedPrimary.copy(alpha = 0.08f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(RedPrimary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.People,
                    contentDescription = null,
                    tint = RedPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column {
                Text(
                    "Total de Empleados",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                Text(
                    "$total ${if (total == 1) "empleado" else "empleados"}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = RedPrimary
                )
            }
        }
    }
}


// ——————————————————————————————
//     CARD EMPLEADO (CORREGIDA)
// ——————————————————————————————
@Composable
fun CompactEmpleadoCard(
    empleado: EmpleadoGarage,
    onDelete: () -> Unit
) {
    val user = empleado.users
    var expanded by remember { mutableStateOf(false) }
    var dialog by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Avatar(user?.nombre)
                Spacer(Modifier.width(12.dp))

                Column(Modifier.weight(1f)) {
                    Text(
                        user?.nombre ?: "Nombre no disponible",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = RedPrimary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                "ID: ${empleado.empleado_id}",
                                fontSize = 11.sp,
                                color = RedPrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
//
//                        // Badge de rol
//                        Surface(
//                            color = Color(0xFF00B894).copy(alpha = 0.1f),
//                            shape = RoundedCornerShape(4.dp)
//                        ) {
//                            Text(
//                                empleado. ?: "Empleado",
//                                fontSize = 11.sp,
//                                color = Color(0xFF00B894),
//                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
//                            )
//                        }
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
                        onClick = { dialog = true },
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
                        Text("Eliminar empleado", fontSize = 13.sp)
                    }
                }
            }
        }
    }

    if (dialog) DeleteDialog(
        name = user?.nombre,
        onDismiss = { dialog = false },
        onConfirm = {
            onDelete()
            dialog = false
        }
    )
}


// ——————————————————————————————
//     SUBCOMPONENTES
// ——————————————————————————————
@Composable
fun Avatar(name: String?) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(
                brush = Brush.linearGradient(
                    listOf(RedPrimary, RedLight)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            (name?.firstOrNull()?.uppercase() ?: "?"),
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun InfoRow(icon: ImageVector, label: String, value: String?) {
    if (value.isNullOrBlank()) return

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            null,
            tint = RedPrimary.copy(alpha = 0.7f),
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


// ——————————————————————————————
//     DIALOG
// ——————————————————————————————
@Composable
fun DeleteDialog(name: String?, onDismiss: () -> Unit, onConfirm: () -> Unit) {
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
            Text("Confirmar Eliminación")
        },
        text = {
            Text("¿Estás seguro de que deseas eliminar a ${name ?: "este empleado"}? Esta acción no se puede deshacer.")
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("Eliminar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}


// ——————————————————————————————
//     EMPTY & LOADING
// ——————————————————————————————
@Composable
fun EmptyState(onAgregarEmpleado: () -> Unit) {
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
                .background(RedPrimary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Outlined.PersonOff,
                contentDescription = null,
                tint = RedPrimary,
                modifier = Modifier.size(50.dp)
            )
        }

        Spacer(Modifier.height(24.dp))

        Text(
            "No hay empleados registrados",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(Modifier.height(8.dp))

        Text(
            "Agrega tu primer empleado para comenzar",
            color = TextSecondary,
            fontSize = 14.sp
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = onAgregarEmpleado,
            colors = ButtonDefaults.buttonColors(containerColor = RedPrimary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.height(50.dp)
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text("Agregar Empleado", fontSize = 15.sp)
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
        CircularProgressIndicator(color = RedPrimary)
        Spacer(Modifier.height(16.dp))
        Text("Cargando empleados...", color = TextSecondary, fontSize = 14.sp)
    }
}