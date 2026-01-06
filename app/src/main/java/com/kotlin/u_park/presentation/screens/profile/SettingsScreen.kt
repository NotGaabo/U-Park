package com.kotlin.u_park.presentation.screens.profile

import android.app.Application
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.kotlin.u_park.R
import com.kotlin.u_park.data.repository.SubscriptionRepository
import com.kotlin.u_park.domain.model.Subscription
import com.kotlin.u_park.presentation.components.SettingsItemLine
import com.kotlin.u_park.presentation.components.UserInfoLine
import com.kotlin.u_park.presentation.navigation.Routes
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// 🎨 Modern Color System
private val PrimaryRed = Color(0xFFE60023)
private val DarkRed = Color(0xFFB8001C)
private val LightRed = Color(0xFFFFE5E9)
private val BackgroundColor = Color(0xFFFAFAFA)
private val SurfaceColor = Color(0xFFFFFFFF)
private val TextPrimary = Color(0xFF0D0D0D)
private val TextSecondary = Color(0xFF6E6E73)
private val BorderColor = Color(0xFFE5E5EA)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    supabase: SupabaseClient,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current.applicationContext as Application
    val viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(context, supabase)
    )

    SettingsScreenContent(
        navController = navController,
        viewModel = viewModel,
        onSignOut = onSignOut,
        isDuenoView = false,
        isEmployeeView = false
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsScreenContent(
    navController: NavController,
    viewModel: SettingsViewModel,
    onSignOut: () -> Unit,
    isDuenoView: Boolean = false,
    isEmployeeView: Boolean = false,
    garageId: String? = null
) {
    val subscriptionRepo = remember { SubscriptionRepository() }
    var activeSubscription by remember { mutableStateOf<Subscription?>(null) }

    val currentUser by viewModel.currentUser.collectAsState()
    val activeRole by viewModel.activeRole.collectAsState()
    var showRoleSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    LaunchedEffect(currentUser) {
        currentUser?.id?.let { userId ->
            activeSubscription = subscriptionRepo.getActiveSubscriptionByUser(userId)
        }
    }

    val hasDuenoGarageRole = currentUser?.roles?.any { rol ->
        rol.lowercase().replace("-", "").replace("_", "") == "duenogarage"
    } ?: false

    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Perfil",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = TextPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceColor
                )
            )
        },
        bottomBar = {
            when {
                isEmployeeView -> {
                    ModernBottomBarEmployee(
                        selectedIndex = 2,
                        onItemSelected = { index ->
                            when (index) {
                                0 -> navController.navigate(Routes.EmployeeHome.route)
                                1 -> garageId?.let {
                                    navController.navigate(Routes.VehiculosDentro.createRoute(it))
                                }
                            }
                        }
                    )
                }
                isDuenoView -> {
                    ModernBottomBarAdmin(
                        selectedIndex = 3,
                        onItemSelected = { index ->
                            when (index) {
                                0 -> navController.navigate(Routes.Rates.route)
                                1 -> navController.navigate(Routes.DuenoGarage.route)
                                2 -> navController.navigate(Routes.ListaReservas.route)
                            }
                        }
                    )
                }
                else -> {
                    ModernBottomBarUser(
                        selectedIndex = 3,
                        onItemSelected = { index ->
                            when (index) {
                                0 -> navController.navigate(Routes.Home.route)
                                1 -> navController.navigate("vehicles")
                                2 -> { /* Historial */ }
                            }
                        }
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
                .background(BackgroundColor)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Profile Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_user_placeholder),
                    contentDescription = "Foto de perfil",
                    modifier = Modifier
                        .size(70.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(20.dp))
                Column {
                    Text(
                        text = currentUser?.nombre ?: "Usuario",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Text(
                        text = currentUser?.usuario ?: "Ver perfil",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                    )
                }
                Spacer(Modifier.weight(1f))
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Garage Card (only for users without dueno role and not in employee view)
            if (!hasDuenoGarageRole && !isDuenoView && !isEmployeeView) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            navController.navigate(Routes.GarageAdd.route)
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_parking_placeholder),
                            contentDescription = "Ilustración parqueo",
                            modifier = Modifier.size(90.dp)
                        )
                        Spacer(modifier = Modifier.width(20.dp))
                        Column {
                            Text(
                                text = "Administra tu parqueo",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Optimiza tus espacios y gana dinero fácilmente.",
                                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Account Details
            Text(
                "Detalles de cuenta",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(SurfaceColor)
            ) {
                UserInfoLine("Correo electrónico", currentUser?.correo ?: "No disponible")
                Divider(color = BorderColor)
                UserInfoLine("Teléfono", currentUser?.telefono ?: "No disponible")
                Divider(color = BorderColor)
                UserInfoLine("Usuario", currentUser?.usuario ?: "No disponible")
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Settings
            Text(
                "Configuración",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(SurfaceColor)
            ) {
                SettingsItemLine(Icons.Filled.Person, "Información personal", TextSecondary) { }
                Divider(color = BorderColor)
                SettingsItemLine(Icons.Filled.Security, "Privacidad y seguridad", TextSecondary) { }
                Divider(color = BorderColor)
                SettingsItemLine(Icons.Filled.Notifications, "Notificaciones", TextSecondary) { }
                Divider(color = BorderColor)
                SettingsItemLine(Icons.Filled.Info, "Acerca de U-Park", TextSecondary) { }

                if ((currentUser?.roles?.size ?: 0) > 1) {
                    Divider(color = BorderColor)
                    SettingsItemLine(Icons.Filled.SwapHoriz, "Cambiar de rol", TextSecondary) {
                        showRoleSheet = true
                    }
                }

                if (activeSubscription != null) {
                    Divider(color = BorderColor)
                    SettingsItemLine(
                        icon = Icons.Default.Subscriptions,
                        title = "Gestionar suscripción",
                        iconColor = Color(0xFF0D47A1)
                    ) {
                        navController.navigate(
                            Routes.ManageSubscription.createRoute(
                                activeSubscription!!.garage_id
                            )
                        )
                    }
                }

                Divider(color = BorderColor)
                SettingsItemLine(Icons.Filled.ExitToApp, "Cerrar sesión", PrimaryRed) {
                    viewModel.signOut(onSignOut)
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // Role Selector Bottom Sheet
    if (showRoleSheet) {
        ModalBottomSheet(
            onDismissRequest = { showRoleSheet = false },
            sheetState = sheetState,
            containerColor = SurfaceColor,
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
                    .padding(24.dp)
            ) {
                Text(
                    "Cambiar de Rol",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = TextPrimary
                )
                Text(
                    "Selecciona cómo quieres usar U-Park",
                    fontSize = 14.sp,
                    color = TextSecondary
                )
                Spacer(Modifier.height(24.dp))

                currentUser?.roles?.forEach { rol ->
                    val isActive = rol.equals(activeRole, ignoreCase = true)
                    val normalizedRole = rol.lowercase().replace("-", "").replace("_", "")

                    val (icon, description) = when (normalizedRole) {
                        "duenogarage" -> Icons.Default.Business to "Administra tus garages"
                        "employee" -> Icons.Default.Work to "Gestiona reservas"
                        "user" -> Icons.Default.Person to "Busca estacionamientos"
                        else -> Icons.Default.Person to "Acceso estándar"
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clickable {
                                if (!isActive) {
                                    scope.launch {
                                        viewModel.getSessionManager().saveActiveRole(rol)
                                        delay(300)
                                        showRoleSheet = false

                                        val route = when (normalizedRole) {
                                            "duenogarage" -> Routes.DuenoGarage.route
                                            "employee" -> Routes.EmployeeHome.route
                                            "user" -> Routes.Home.route
                                            else -> Routes.Login.route
                                        }

                                        navController.navigate(route) {
                                            popUpTo(Routes.Settings.route) { inclusive = false }
                                            launchSingleTop = true
                                        }
                                    }
                                }
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isActive) LightRed else Color(0xFFF5F7FA)
                        ),
                        border = if (isActive) BorderStroke(2.dp, PrimaryRed) else null
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(
                                        if (isActive) PrimaryRed.copy(alpha = 0.1f)
                                        else SurfaceColor,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    icon,
                                    contentDescription = null,
                                    tint = if (isActive) PrimaryRed else Color.Gray,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = rol,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp,
                                    color = if (isActive) PrimaryRed else TextPrimary
                                )
                                Text(
                                    text = description,
                                    fontSize = 13.sp,
                                    color = TextSecondary
                                )
                            }

                            if (isActive) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = "Rol activo",
                                    tint = PrimaryRed,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

// Modern Bottom Bar for Employee
@Composable
fun ModernBottomBarEmployee(
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
            BottomBarItemModern(
                icon = Icons.Outlined.Home,
                selectedIcon = Icons.Default.Home,
                label = "Home",
                isSelected = selectedIndex == 0,
                onClick = { onItemSelected(0) }
            )
            BottomBarItemModern(
                icon = Icons.Outlined.DirectionsCar,
                selectedIcon = Icons.Default.DirectionsCar,
                label = "Vehículos",
                isSelected = selectedIndex == 1,
                onClick = { onItemSelected(1) }
            )
            BottomBarItemModern(
                icon = Icons.Outlined.Person,
                selectedIcon = Icons.Default.Person,
                label = "Perfil",
                isSelected = selectedIndex == 2,
                onClick = { onItemSelected(2) }
            )
        }
    }
}

// Modern Bottom Bar for Admin/Dueño
@Composable
fun ModernBottomBarAdmin(
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
            BottomBarItemModern(
                icon = Icons.Outlined.AttachMoney,
                selectedIcon = Icons.Default.AttachMoney,
                label = "Tarifas",
                isSelected = selectedIndex == 0,
                onClick = { onItemSelected(0) }
            )
            BottomBarItemModern(
                icon = Icons.Outlined.Dashboard,
                selectedIcon = Icons.Default.Dashboard,
                label = "Dashboard",
                isSelected = selectedIndex == 1,
                onClick = { onItemSelected(1) }
            )
            BottomBarItemModern(
                icon = Icons.Outlined.BookmarkBorder,
                selectedIcon = Icons.Default.Bookmark,
                label = "Reservas",
                isSelected = selectedIndex == 2,
                onClick = { onItemSelected(2) }
            )
            BottomBarItemModern(
                icon = Icons.Outlined.Person,
                selectedIcon = Icons.Default.Person,
                label = "Perfil",
                isSelected = selectedIndex == 3,
                onClick = { onItemSelected(3) }
            )
        }
    }
}

// Modern Bottom Bar for User
@Composable
fun ModernBottomBarUser(
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
            BottomBarItemModern(
                icon = Icons.Outlined.Home,
                selectedIcon = Icons.Default.Home,
                label = "Inicio",
                isSelected = selectedIndex == 0,
                onClick = { onItemSelected(0) }
            )
            BottomBarItemModern(
                icon = Icons.Outlined.DirectionsCar,
                selectedIcon = Icons.Default.DirectionsCar,
                label = "Vehículos",
                isSelected = selectedIndex == 1,
                onClick = { onItemSelected(1) }
            )
            BottomBarItemModern(
                icon = Icons.Outlined.History,
                selectedIcon = Icons.Default.History,
                label = "Historial",
                isSelected = selectedIndex == 2,
                onClick = { onItemSelected(2) }
            )
            BottomBarItemModern(
                icon = Icons.Outlined.Person,
                selectedIcon = Icons.Default.Person,
                label = "Perfil",
                isSelected = selectedIndex == 3,
                onClick = { onItemSelected(3) }
            )
        }
    }
}

@Composable
fun BottomBarItemModern(
    icon: ImageVector,
    selectedIcon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val animatedColor by animateColorAsState(
        targetValue = if (isSelected) PrimaryRed else TextSecondary,
        animationSpec = tween(300)
    )

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
                tint = animatedColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = animatedColor
            )
        }
    }
}