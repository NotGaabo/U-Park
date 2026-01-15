package com.kotlin.u_park.presentation.screens.employee

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.kotlin.u_park.R
import com.kotlin.u_park.presentation.navigation.Routes
import com.kotlin.u_park.presentation.screens.parking.ParkingViewModel
import com.kotlin.u_park.ui.theme.*
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.Duration

// Data class corregida según la tabla de Supabase
data class ParkingRecord(
    val id: String,
    val vehicleId: String?,
    val garageId: String?,
    val rateId: String?,
    val horaEntrada: String, // timestamp
    val horaSalida: String?, // timestamp nullable
    val total: Double?,
    val pagado: Boolean?,
    val createdAt: String?,
    val tipo: String?, // entrada o reserva
    val estado: String?, // pendiente, activa, completada, expirada
    val createdByUserId: String?,
    val fotosEntrada: List<String>, // array de URLs
    val fotosSalida: List<String>, // array de URLs
    // Datos relacionados de otras tablas (joins)
    val vehiclePlate: String?,
    val employeeName: String?,
    val esIncidencia: Boolean? = false
) {
    // Helpers para la UI
    val isActive: Boolean
        get() = estado == "activa"

    val isCompleted: Boolean
        get() = estado == "completada"

    val firstEntryPhoto: String?
        get() = fotosEntrada.firstOrNull()

    val firstExitPhoto: String?
        get() = fotosSalida.firstOrNull()

    @RequiresApi(Build.VERSION_CODES.O)
    fun getFormattedEntryTime(): String {
        return try {
            val dt = OffsetDateTime.parse(horaEntrada)
            dt.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"))
        } catch (e: Exception) {
            horaEntrada
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getFormattedExitTime(): String {
        return try {
            horaSalida?.let {
                val dt = OffsetDateTime.parse(it)
                dt.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"))
            } ?: "--"
        } catch (e: Exception) {
            horaSalida ?: "--"
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getFormattedDate(): String {
        return try {
            val dt = OffsetDateTime.parse(horaEntrada)
            dt.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
        } catch (e: Exception) {
            horaEntrada
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getDuration(): String {
        return try {
            if (horaSalida != null) {
                val entrada = OffsetDateTime.parse(horaEntrada)
                val salida = OffsetDateTime.parse(horaSalida)
                val duration = Duration.between(entrada, salida)

                val hours = duration.toHours()
                val minutes = duration.toMinutes() % 60

                when {
                    hours > 0 -> "${hours}h ${minutes}m"
                    else -> "${minutes}m"
                }
            } else {
                "En curso"
            }
        } catch (e: Exception) {
            "En curso"
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParkingRecordsScreen(
    navController: NavController,
    garageId: String,
    viewModel: ParkingViewModel
) {
    val records by viewModel.records.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    var selectedRecord by remember { mutableStateOf<ParkingRecord?>(null) }
    var selectedFilter by remember { mutableStateOf("all") }

    LaunchedEffect(garageId) {
        viewModel.loadRecords(garageId)
    }

    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Historial de Registros",
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = TextPrimary
                        )
                        Text(
                            "Todos los movimientos del parqueo",
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceColor
                )
            )
        },
        bottomBar = {
            ModernBottomBarEmployee(
                selectedIndex = 3,
                onItemSelected = { index ->
                    when (index) {
                        1 -> navController.navigate(Routes.VehiculosDentro.createRoute(garageId))
                        2 -> navController.navigate(Routes.EmployeeSettings.route)
                        3 -> navController.navigate(Routes.ParkingRecords.createRoute(garageId))
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundColor)
                .padding(padding)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                FilterChips(
                    selectedFilter = selectedFilter,
                    onFilterSelected = { selectedFilter = it }
                )

                when {
                    isLoading -> LoadingState()
                    error != null -> ErrorState(error = error!!, onRetry = { viewModel.loadRecords(garageId) })
                    else -> {
                        val filteredRecords = when (selectedFilter) {
                            "active" -> records.filter { it.isActive }
                            "completed" -> records.filter { it.isCompleted }
                            else -> records
                        }

                        if (filteredRecords.isEmpty()) {
                            EmptyRecordsState()
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(filteredRecords) { record ->
                                    ParkingRecordCard(
                                        record = record,
                                        onClick = { selectedRecord = record }
                                    )
                                }
                                item { Spacer(Modifier.height(20.dp)) }
                            }
                        }
                    }
                }
            }
        }

        // Modal de detalles con función de incidencia
        selectedRecord?.let { record ->
            RecordDetailDialog(
                record = record,
                onDismiss = { selectedRecord = null },
                onMarcarIncidencia = { parkingId, esIncidencia ->
                    viewModel.marcarComoIncidencia(parkingId, esIncidencia)
                }
            )
        }
    }
}

@Composable
fun FilterChips(
    selectedFilter: String,
    onFilterSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        FilterChip(
            label = "Todos",
            isSelected = selectedFilter == "all",
            onClick = { onFilterSelected("all") },
            icon = Icons.Default.ViewList
        )
        FilterChip(
            label = "Activos",
            isSelected = selectedFilter == "active",
            onClick = { onFilterSelected("active") },
            icon = Icons.Default.DirectionsCar
        )
        FilterChip(
            label = "Completados",
            isSelected = selectedFilter == "completed",
            onClick = { onFilterSelected("completed") },
            icon = Icons.Default.CheckCircle
        )
    }
}

@Composable
fun FilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) PrimaryRed else SurfaceColor,
        animationSpec = tween(300)
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else TextPrimary,
        animationSpec = tween(300)
    )

    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 1.dp
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(18.dp)
            )
            Text(
                label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = contentColor
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ParkingRecordCard(
    record: ParkingRecord,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (record.esIncidencia == true) {
                Color(0xFFFEE2E2)  // 🔥 Rojo claro si es incidencia
            } else {
                SurfaceColor
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        border = if (record.esIncidencia == true) {
            androidx.compose.foundation.BorderStroke(2.dp, PrimaryRed.copy(alpha = 0.3f))
        } else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 🔥 Indicador de incidencia en la parte superior
            if (record.esIncidencia == true) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = PrimaryRed,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        "INCIDENCIA REPORTADA",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryRed,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // Header con placa y status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(
                                if (record.esIncidencia == true) {
                                    PrimaryRed.copy(alpha = 0.15f)
                                } else if (record.isActive) {
                                    SuccessGreen.copy(alpha = 0.15f)
                                } else {
                                    Color(0xFFE5E7EB)
                                },
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (record.esIncidencia == true) Icons.Default.Warning
                            else Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = if (record.esIncidencia == true) PrimaryRed
                            else if (record.isActive) SuccessGreen
                            else Color(0xFF6B7280),
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Column {
                        Text(
                            record.vehiclePlate ?: "Desconocido",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            record.getFormattedDate(),
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }
                }

                StatusBadge(estado = record.estado ?: "pendiente")
            }

            Spacer(Modifier.height(16.dp))

            // Info Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InfoItem(
                    icon = Icons.Default.Login,
                    label = "Entrada",
                    value = record.getFormattedEntryTime(),
                    color = SuccessGreen,
                    modifier = Modifier.weight(1f)
                )

                InfoItem(
                    icon = Icons.Default.Logout,
                    label = "Salida",
                    value = record.getFormattedExitTime(),
                    color = if (record.horaSalida != null) PrimaryRed else TextSecondary,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(12.dp))

            // Registrado por y fotos
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        record.employeeName ?: "Sistema",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (record.fotosEntrada.isNotEmpty()) {
                        PhotoIndicator(hasPhoto = true, count = record.fotosEntrada.size)
                    }
                    if (record.fotosSalida.isNotEmpty()) {
                        PhotoIndicator(hasPhoto = true, count = record.fotosSalida.size)
                    }
                    if (record.fotosEntrada.isEmpty() && record.fotosSalida.isEmpty()) {
                        Icon(
                            Icons.Default.PhotoCamera,
                            contentDescription = null,
                            tint = Color(0xFFD1D5DB),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun StatusBadge(estado: String) {
    val (backgroundColor, textColor, text) = when (estado) {
        "activa" -> Triple(SuccessGreen.copy(alpha = 0.15f), SuccessGreen, "Activo")
        "completada" -> Triple(Color(0xFFE5E7EB), Color(0xFF6B7280), "Completado")
        "pendiente" -> Triple(WarningOrange.copy(alpha = 0.15f), WarningOrange, "Pendiente")
        "expirada" -> Triple(PrimaryRed.copy(alpha = 0.15f), PrimaryRed, "Expirado")
        else -> Triple(Color(0xFFE5E7EB), Color(0xFF6B7280), estado)
    }

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}

@Composable
fun InfoItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = BackgroundColor
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Column {
                Text(
                    label,
                    fontSize = 11.sp,
                    color = TextSecondary
                )
                Text(
                    value,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
        }
    }
}

@Composable
fun PhotoIndicator(hasPhoto: Boolean, count: Int = 1) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .background(
                if (hasPhoto) InfoBlue.copy(alpha = 0.15f) else Color(0xFFE5E7EB),
                CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        if (count > 1) {
            Text(
                count.toString(),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (hasPhoto) InfoBlue else Color(0xFFD1D5DB)
            )
        } else {
            Icon(
                Icons.Default.PhotoCamera,
                contentDescription = null,
                tint = if (hasPhoto) InfoBlue else Color(0xFFD1D5DB),
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RecordDetailDialog(
    record: ParkingRecord,
    onDismiss: () -> Unit,
    onMarcarIncidencia: (String, Boolean) -> Unit  // 🔥 NUEVO parámetro
) {
    var selectedPhoto by remember { mutableStateOf<String?>(null) }
    var showIncidenciaConfirm by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f),
            colors = CardDefaults.cardColors(
                containerColor = SurfaceColor
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = if (record.esIncidencia == true) {
                                    listOf(Color(0xFFFEE2E2), SurfaceColor)
                                } else {
                                    listOf(LightRed, SurfaceColor)
                                }
                            )
                        )
                        .padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    "Detalles del Registro",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                if (record.esIncidencia == true) {
                                    Icon(
                                        Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = PrimaryRed,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Text(
                                record.vehiclePlate ?: "Desconocido",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (record.esIncidencia == true) PrimaryRed else PrimaryRed
                            )
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color.White.copy(alpha = 0.3f), CircleShape)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = TextPrimary
                            )
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // 🔥 Botón de incidencia
                    item {
                        Button(
                            onClick = { showIncidenciaConfirm = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (record.esIncidencia == true) {
                                    SuccessGreen
                                } else {
                                    PrimaryRed
                                }
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                if (record.esIncidencia == true) Icons.Default.CheckCircle
                                else Icons.Default.Warning,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                if (record.esIncidencia == true) {
                                    "Remover Incidencia"
                                } else {
                                    "Marcar como Incidencia"
                                },
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Info Cards
                    item {
                        DetailInfoCard(
                            icon = Icons.Default.AccessTime,
                            title = "Información de Tiempo",
                            items = listOf(
                                "Entrada" to record.getFormattedEntryTime(),
                                "Salida" to record.getFormattedExitTime(),
                                "Duración" to record.getDuration(),
                                "Fecha" to record.getFormattedDate()
                            )
                        )
                    }

                    item {
                        DetailInfoCard(
                            icon = Icons.Default.Person,
                            title = "Información del Registro",
                            items = buildList {
                                add("Registrado por" to (record.employeeName ?: "Sistema"))
                                add("Estado" to (record.estado ?: "Desconocido"))
                                add("Tipo" to (record.tipo ?: "Desconocido"))
                                if (record.esIncidencia == true) {
                                    add("⚠️ Incidencia" to "SÍ")
                                }
                                record.total?.let { add("Total" to "RD$ ${String.format("%.2f", it)}") }
                                record.pagado?.let { add("Pagado" to if (it) "Sí" else "No") }
                            }
                        )
                    }

                    // Fotos de Entrada
                    if (record.fotosEntrada.isNotEmpty()) {
                        item {
                            Text(
                                "Fotos de Entrada (${record.fotosEntrada.size})",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        item {
                            PhotoGrid(
                                photos = record.fotosEntrada,
                                label = "Entrada",
                                color = SuccessGreen,
                                onPhotoClick = { selectedPhoto = it }
                            )
                        }
                    }

                    // Fotos de Salida
                    if (record.fotosSalida.isNotEmpty()) {
                        item {
                            Text(
                                "Fotos de Salida (${record.fotosSalida.size})",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        item {
                            PhotoGrid(
                                photos = record.fotosSalida,
                                label = "Salida",
                                color = PrimaryRed,
                                onPhotoClick = { selectedPhoto = it }
                            )
                        }
                    }

                    // Si no hay fotos
                    if (record.fotosEntrada.isEmpty() && record.fotosSalida.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = BackgroundColor
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(40.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        Icons.Default.PhotoCamera,
                                        contentDescription = null,
                                        tint = Color(0xFFD1D5DB),
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Text(
                                        "Sin evidencia fotográfica",
                                        fontSize = 14.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }

                    item { Spacer(Modifier.height(20.dp)) }
                }
            }
        }
    }

    // Visor de foto en pantalla completa
    selectedPhoto?.let { photoUrl ->
        PhotoViewerDialog(
            photoUrl = photoUrl,
            onDismiss = { selectedPhoto = null }
        )
    }

    // 🔥 Diálogo de confirmación
    if (showIncidenciaConfirm) {
        AlertDialog(
            onDismissRequest = { showIncidenciaConfirm = false },
            icon = {
                Icon(
                    if (record.esIncidencia == true) Icons.Default.CheckCircle
                    else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (record.esIncidencia == true) SuccessGreen else PrimaryRed,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    if (record.esIncidencia == true) {
                        "¿Remover Incidencia?"
                    } else {
                        "¿Marcar como Incidencia?"
                    },
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    if (record.esIncidencia == true) {
                        "Este registro dejará de estar marcado como incidencia."
                    } else {
                        "Este registro quedará marcado como incidencia y se destacará en la lista."
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onMarcarIncidencia(record.id, !(record.esIncidencia ?: false))
                        showIncidenciaConfirm = false
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (record.esIncidencia == true) SuccessGreen else PrimaryRed
                    )
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showIncidenciaConfirm = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun PhotoGrid(
    photos: List<String>,
    label: String,
    color: Color,
    onPhotoClick: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        photos.chunked(2).forEach { rowPhotos ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowPhotos.forEach { photoUrl ->
                    PhotoCard(
                        photoUrl = photoUrl,
                        label = label,
                        color = color,
                        onClick = { onPhotoClick(photoUrl) },
                        modifier = Modifier.weight(1f)
                    )
                }
                // Si solo hay una foto en la fila, añadir spacer
                if (rowPhotos.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun DetailInfoCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    items: List<Pair<String, String>>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = BackgroundColor
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(PrimaryRed.copy(alpha = 0.15f), CircleShape),
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
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            items.forEach { (label, value) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        label,
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                    Text(
                        value,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }
                if (label != items.last().first) {
                    HorizontalDivider(color = BorderColor)
                }
            }
        }
    }
}

@Composable
fun PhotoCard(
    photoUrl: String,
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(160.dp),
        colors = CardDefaults.cardColors(
            containerColor = BackgroundColor
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            AsyncImage(
                model = photoUrl,
                contentDescription = "Foto de $label",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            // Overlay gradient
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.5f)
                            )
                        )
                    )
            )

            // Zoom icon
            Icon(
                Icons.Default.ZoomIn,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(20.dp)
            )
        }
    }
}

@Composable
fun PhotoViewerDialog(
    photoUrl: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .clickable(onClick = onDismiss)
        ) {
            AsyncImage(
                model = photoUrl,
                contentDescription = "Foto ampliada",
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center),
                contentScale = ContentScale.Fit
            )

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(48.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Cerrar",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun EmptyRecordsState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(Color(0xFFF3F4F6), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Description,
                    contentDescription = null,
                    tint = Color(0xFF9CA3AF),
                    modifier = Modifier.size(60.dp)
                )
            }
            Text(
                "Sin registros",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                "No hay registros de parking para mostrar",
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}


@Composable
fun ErrorState(error: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Default.Error,
                contentDescription = null,
                tint = PrimaryRed,
                modifier = Modifier.size(64.dp)
            )
            Text(
                "Error al cargar",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                error,
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryRed
                )
            ) {
                Text("Reintentar")
            }
        }
    }
}
