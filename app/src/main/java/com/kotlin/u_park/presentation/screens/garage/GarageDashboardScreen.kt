package com.kotlin.u_park.presentation.screens.garage

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kotlin.u_park.data.remote.supabase
import com.kotlin.u_park.data.repository.GarageReportRepositoryImpl
import com.kotlin.u_park.domain.model.*
import java.time.LocalDateTime

// 🎨 Color System
private val PrimaryRed = Color(0xFFE60023)
private val BackgroundColor = Color(0xFFFAFAFA)
private val SurfaceColor = Color(0xFFFFFFFF)
private val TextPrimary = Color(0xFF0D0D0D)
private val TextSecondary = Color(0xFF6E6E73)
private val SuccessGreen = Color(0xFF34C759)
private val InfoBlue = Color(0xFF007AFF)

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GarageDashboardScreen(
    garageId: String,
    garageName: String,
    onNavigateBack: () -> Unit,
    onNavigateToEmployees: () -> Unit,
    onNavigateToSubscriptions: () -> Unit
) {
    val context = LocalContext.current
    val viewModel = remember {
        GarageDashboardViewModel(GarageReportRepositoryImpl(supabase))
    }

    val reportState by viewModel.reportState.collectAsState()
    val occupancyReport by viewModel.occupancyReport.collectAsState()
    val incomeReport by viewModel.incomeReport.collectAsState()

    var showBottomSheet by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(reportState) {
        when (val state = reportState) {
            is ReportState.Success -> {
                snackbarHostState.showSnackbar("✅ PDF guardado en: ${state.filePath}")
                viewModel.resetReportState()
            }
            is ReportState.Error -> {
                snackbarHostState.showSnackbar("❌ ${state.message}")
                viewModel.resetReportState()
            }
            else -> {}
        }
    }

    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            garageName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = TextPrimary
                        )
                        Text(
                            "Dashboard",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceColor
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            ModernBottomBarDashboard(
                selectedIndex = 0,
                onItemSelected = { index ->
                    when (index) {
                        1 -> onNavigateToEmployees()
                        2 -> onNavigateToSubscriptions()
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("Generar reporte", fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Assessment, null) },
                onClick = { showBottomSheet = true },
                containerColor = PrimaryRed,
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 8.dp
                )
            )
        }
    ) { padding ->
        Box(
            Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Section
                Text(
                    "Resumen de Actividad",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    letterSpacing = (-0.5).sp
                )

                // Statistics Cards
                StatisticsSection(occupancyReport, incomeReport)

                // Quick Actions
                QuickActionsCard(
                    onEmployees = onNavigateToEmployees,
                    onSubscriptions = onNavigateToSubscriptions,
                    onReports = { showBottomSheet = true }
                )

                // Occupancy Report
                occupancyReport?.let {
                    ModernOccupancyCard(it)
                }

                // Income Report
                incomeReport?.let {
                    ModernIncomeCard(it)
                }

                Spacer(Modifier.height(80.dp))
            }

            if (reportState is ReportState.Loading) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryRed)
                }
            }
        }
    }

    if (showBottomSheet) {
        ReportSelectorBottomSheet(
            onDismiss = { showBottomSheet = false },
            onGenerateReport = { type, start, end ->
                when (type) {
                    ReportType.OCCUPANCY ->
                        viewModel.generateOccupancyReport(context, garageId, start, end)
                    ReportType.INCOME ->
                        viewModel.generateIncomeReport(context, garageId, start, end)
                }
                showBottomSheet = false
            }
        )
    }
}

@Composable
private fun StatisticsSection(
    occupancyReport: OccupancyReport?,
    incomeReport: IncomeReport?
) {
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
                    "Estadísticas",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                DashboardStatItem(
                    icon = Icons.Outlined.DirectionsCar,
                    value = occupancyReport?.totalVehicles?.toString() ?: "—",
                    label = "Vehículos",
                    color = PrimaryRed
                )

                DashboardStatItem(
                    icon = Icons.Outlined.AttachMoney,
                    value = incomeReport?.let { "$${"%.0f".format(it.totalIncome)}" } ?: "—",
                    label = "Ingresos",
                    color = SuccessGreen
                )

                DashboardStatItem(
                    icon = Icons.Outlined.TrendingUp,
                    value = "—",
                    label = "Promedio",
                    color = InfoBlue
                )
            }
        }
    }
}

@Composable
private fun DashboardStatItem(
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
private fun QuickActionsCard(
    onEmployees: () -> Unit,
    onSubscriptions: () -> Unit,
    onReports: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = SurfaceColor,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Acciones Rápidas",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionButton(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.Group,
                    label = "Empleados",
                    color = PrimaryRed,
                    onClick = onEmployees
                )
                QuickActionButton(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.CardMembership,
                    label = "Suscripciones",
                    color = InfoBlue,
                    onClick = onSubscriptions
                )
            }
        }
    }
}

@Composable
private fun QuickActionButton(
    modifier: Modifier,
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(28.dp)
            )
            Text(
                label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun ModernOccupancyCard(report: OccupancyReport) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = SurfaceColor,
        shadowElevation = 2.dp
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Outlined.DirectionsCar,
                    contentDescription = null,
                    tint = PrimaryRed,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    "Reporte de Ocupación",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Total Vehículos", fontSize = 12.sp, color = TextSecondary)
                    Text(
                        "${report.totalVehicles}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryRed
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Período", fontSize = 12.sp, color = TextSecondary)
                    Text(
                        "${report.startDate.toLocalDate()}",
                        fontSize = 13.sp,
                        color = TextPrimary
                    )
                    Text(
                        "→ ${report.endDate.toLocalDate()}",
                        fontSize = 13.sp,
                        color = TextPrimary
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun ModernIncomeCard(report: IncomeReport) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = SurfaceColor,
        shadowElevation = 2.dp
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Outlined.AttachMoney,
                    contentDescription = null,
                    tint = SuccessGreen,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    "Reporte de Ingresos",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Total Ingresos", fontSize = 12.sp, color = TextSecondary)
                    Text(
                        "$${"%.2f".format(report.totalIncome)}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = SuccessGreen
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Período", fontSize = 12.sp, color = TextSecondary)
                    Text(
                        "${report.startDate.toLocalDate()}",
                        fontSize = 13.sp,
                        color = TextPrimary
                    )
                    Text(
                        "→ ${report.endDate.toLocalDate()}",
                        fontSize = 13.sp,
                        color = TextPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun ModernBottomBarDashboard(
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
            BottomBarItemDashboard(
                icon = Icons.Outlined.Dashboard,
                selectedIcon = Icons.Default.Dashboard,
                label = "Dashboard",
                isSelected = selectedIndex == 0,
                onClick = { onItemSelected(0) }
            )
            BottomBarItemDashboard(
                icon = Icons.Outlined.Group,
                selectedIcon = Icons.Default.Group,
                label = "Empleados",
                isSelected = selectedIndex == 1,
                onClick = { onItemSelected(1) }
            )
            BottomBarItemDashboard(
                icon = Icons.Outlined.CardMembership,
                selectedIcon = Icons.Default.CardMembership,
                label = "Suscripciones",
                isSelected = selectedIndex == 2,
                onClick = { onItemSelected(2) }
            )
        }
    }
}

@Composable
private fun BottomBarItemDashboard(
    icon: ImageVector,
    selectedIcon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        modifier = Modifier.size(64.dp, 56.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                if (isSelected) selectedIcon else icon,
                contentDescription = label,
                tint = if (isSelected) PrimaryRed else TextSecondary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) PrimaryRed else TextSecondary
            )
        }
    }
}