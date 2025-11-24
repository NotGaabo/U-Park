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
//     PANTALLA PRINCIPAL
// ——————————————————————————————
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmpleadosScreen(
    garageId: String,
    viewModel: EmpleadosViewModel,
    onAgregarEmpleado: () -> Unit
) {
    val empleados by viewModel.empleados.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(garageId) { viewModel.loadEmpleados(garageId) }

    Scaffold(
        topBar = { CompactTopBar() },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAgregarEmpleado,
                containerColor = RedPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(14.dp),
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        },
        containerColor = BackgroundGray,
        contentWindowInsets = WindowInsets.safeDrawing   // 👈 FIX PRINCIPAL
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                isLoading -> LoadingState()
                empleados.isEmpty() -> EmptyState(onAgregarEmpleado)
                else -> EmpleadosList(
                    empleados = empleados,
                    onDelete = { cedula -> viewModel.removeEmpleado(garageId, cedula) }
                )

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
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { HeaderCard(empleados.size) }

        items(empleados, key = { it.empleado_id }) { empleado ->
            CompactEmpleadoCard(
                empleado = empleado,
                onDelete = { onDelete(empleado.empleado_id) }
            )
        }

        item { Spacer(Modifier.height(60.dp)) }
    }
}



// ——————————————————————————————
//     TOP BAR  (ARREGLADA 🚀)
// ——————————————————————————————
@Composable
fun CompactTopBar() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars),   // 👈 FIX PARA QUE NO SE META BAJO LA HORA
        shadowElevation = 1.dp,
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            brush = Brush.linearGradient(listOf(RedPrimary, RedLight))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Group,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(Modifier.width(8.dp))

                Column {
                    Text("Empleados", fontSize = 15.sp, color = TextPrimary)
                    Text("Gestiona tu equipo", fontSize = 10.sp, color = TextSecondary)
                }
            }

            IconButton(onClick = {}) {
                Icon(Icons.Outlined.Search, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
            }
        }
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
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(RedPrimary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.People, contentDescription = null, tint = RedPrimary, modifier = Modifier.size(18.dp))
            }

            Spacer(Modifier.width(10.dp))

            Column {
                Text("Total de Empleados", fontSize = 11.sp, color = TextSecondary)
                Text(
                    "$total activos",
                    fontSize = 16.sp,
                    color = RedPrimary
                )
            }
        }
    }
}



// ——————————————————————————————
//     CARD EMPLEADO
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
        colors = CardDefaults.cardColors(Color.White)
    ) {
        Column(Modifier.padding(12.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Avatar(user?.nombre)
                Spacer(Modifier.width(10.dp))

                Column(Modifier.weight(1f)) {
                    Text(user?.nombre ?: "Nombre no disponible", color = TextPrimary, fontSize = 15.sp)
                    Text("ID: ${empleado.empleado_id}", fontSize = 10.sp, color = RedPrimary)
                }

                IconButton(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            AnimatedVisibility(expanded) {
                Column {
                    Spacer(Modifier.height(8.dp))
                    Divider(color = BackgroundGray)
                    Spacer(Modifier.height(8.dp))

                    InfoRow(Icons.Outlined.Email, "Correo", user?.correo)
                    InfoRow(Icons.Outlined.Phone, "Teléfono", user?.telefono)
                    InfoRow(Icons.Outlined.CalendarToday, "Registrado", empleado.fecha_registro)

                    Spacer(Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { dialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Icon(Icons.Outlined.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Eliminar", fontSize = 13.sp)
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
            .size(36.dp)
            .clip(CircleShape)
            .background(RedPrimary.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            (name?.firstOrNull()?.uppercase() ?: "?"),
            color = RedPrimary,
            fontSize = 14.sp
        )
    }
}

@Composable
fun InfoRow(icon: ImageVector, label: String, value: String?) {
    if (value.isNullOrBlank()) return

    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = TextSecondary, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))

        Column {
            Text(label, fontSize = 10.sp, color = TextSecondary)
            Text(value, fontSize = 13.sp, color = TextPrimary)
        }
    }

    Spacer(Modifier.height(6.dp))
}



// ——————————————————————————————
//     DIALOG
// ——————————————————————————————
@Composable
fun DeleteDialog(name: String?, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = onConfirm) { Text("Eliminar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
        title = { Text("Confirmar Eliminación") },
        text = { Text("¿Eliminar a ${name ?: "este empleado"}?") }
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
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Outlined.PersonOff,
            contentDescription = null,
            tint = RedPrimary,
            modifier = Modifier.size(70.dp)
        )
        Spacer(Modifier.height(14.dp))
        Text("No hay empleados", fontSize = 18.sp)
        Text("Agrega tu primer empleado", color = TextSecondary, fontSize = 13.sp)

        Spacer(Modifier.height(16.dp))

        Button(onClick = onAgregarEmpleado) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(4.dp))
            Text("Agregar", fontSize = 14.sp)
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
        Spacer(Modifier.height(8.dp))
        Text("Cargando…", color = TextSecondary, fontSize = 13.sp)
    }
}
