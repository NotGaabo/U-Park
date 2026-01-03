package com.kotlin.u_park.presentation.screens.profile

import android.app.Application
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.kotlin.u_park.data.repository.SubscriptionRepository
import com.kotlin.u_park.domain.model.Subscription
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.kotlin.u_park.R
import com.kotlin.u_park.presentation.components.SettingsItemLine
import com.kotlin.u_park.presentation.components.UserInfoLine
import com.kotlin.u_park.presentation.navigation.Routes
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

    // Reutilizar la misma lógica del SettingsScreenDueno
    SettingsScreenContent(
        navController = navController,
        viewModel = viewModel,
        onSignOut = onSignOut,
        isDuenoView = false
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsScreenContent(
    navController: NavController,
    viewModel: SettingsViewModel,
    onSignOut: () -> Unit,
    isDuenoView: Boolean = false
) {

    val background = Color(0xFFF9F9F9)
    val textGray = Color(0xFF6B6B6B)
    val dividerGray = Color(0xFFEAEAEA)
    val redSoft = Color(0xFFE60023)
    val redLight = Color(0xFFFFE5E5)

    val subscriptionRepo = remember { SubscriptionRepository() }
    var activeSubscription by remember { mutableStateOf<Subscription?>(null) }

    val currentUser by viewModel.currentUser.collectAsState()
    val activeRole by viewModel.activeRole.collectAsState()
    var showRoleSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    LaunchedEffect(currentUser) {
        currentUser?.id?.let { userId ->
            activeSubscription =
                subscriptionRepo.getActiveSubscriptionByUser(userId)
        }
    }

    // ✅ Verificar si el usuario NO tiene el rol "dueno_garage"
    val hasDuenoGarageRole = currentUser?.roles?.any { rol ->
        rol.lowercase().replace("-", "").replace("_", "") == "duenogarage"
    } ?: false

    Scaffold(
        containerColor = background,
        topBar = {
            TopAppBar(
                title = { Text("Perfil", fontWeight = FontWeight.Bold, color = Color.Black) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = background)
            )
        },
        bottomBar = {
            if (isDuenoView) {
                NavigationBar(containerColor = Color.White) {
                    NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(Routes.Rates.route) },
                    icon = { Icon(Icons.Default.AttachMoney, contentDescription = "Tarifas") },
                    label = { Text("Tarifas") }
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = { navController.navigate(Routes.DuenoGarage.route) },
                        icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                        label = { Text("Dashboard") }
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = { navController.navigate(Routes.ListaReservas.route) },
                        icon = { Icon(Icons.Default.Book, contentDescription = "Reservas") },
                        label = { Text("Reservas") }
                    )
                    NavigationBarItem(
                        selected = true,
                        onClick = {},
                        icon = { Icon(Icons.Default.Person, tint = redSoft, contentDescription = "Perfil") },
                        label = { Text("Perfil", color = redSoft) }
                    )
                }
            } else {
                NavigationBar(containerColor = Color.White) {
                    NavigationBarItem(
                        selected = false,
                        onClick = { navController.navigate(Routes.Home.route) },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Inicio") }
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = { navController.navigate("vehicles") },
                        icon = { Icon(Icons.Default.CarCrash, null) },
                        label = { Text("Vehiculos") }
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = { },
                        icon = { Icon(Icons.Default.History, contentDescription = "Historial") },
                        label = { Text("Historial") }
                    )
                    NavigationBarItem(
                        selected = true,
                        onClick = {},
                        icon = { Icon(Icons.Default.Person, tint = redSoft, contentDescription = "Perfil") },
                        label = { Text("Perfil", color = redSoft) }
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
                .background(background)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

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
                            color = Color.Black
                        )
                    )
                    Text(
                        text = currentUser?.usuario ?: "Ver perfil",
                        style = MaterialTheme.typography.bodyMedium.copy(color = textGray)
                    )
                }
                Spacer(Modifier.weight(1f))
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = textGray
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            // ✅ CARD PARA CREAR GARAJE (solo si NO tiene el rol y NO está en vista dueño)
            if (!hasDuenoGarageRole && !isDuenoView) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
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
                                    color = Color.Black
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Optimiza tus espacios y gana dinero fácilmente.",
                                style = MaterialTheme.typography.bodyMedium.copy(color = textGray)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            Text("Detalles de cuenta", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White)
            ) {
                UserInfoLine("Correo electrónico", currentUser?.correo ?: "No disponible")
                Divider(color = dividerGray)
                UserInfoLine("Teléfono", currentUser?.telefono ?: "No disponible")
                Divider(color = dividerGray)
                UserInfoLine("Usuario", currentUser?.usuario ?: "No disponible")
            }

            Spacer(modifier = Modifier.height(40.dp))

            Text("Configuración", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White)
            ) {
                SettingsItemLine(Icons.Filled.Person, "Información personal", textGray) { }
                Divider(color = dividerGray)
                SettingsItemLine(Icons.Filled.Security, "Privacidad y seguridad", textGray) { }
                Divider(color = dividerGray)
                SettingsItemLine(Icons.Filled.Notifications, "Notificaciones", textGray) { }
                Divider(color = dividerGray)
                SettingsItemLine(Icons.Filled.Info, "Acerca de U-Park", textGray) { }

                if ((currentUser?.roles?.size ?: 0) > 1) {
                    Divider(color = dividerGray)
                    SettingsItemLine(Icons.Filled.SwapHoriz, "Cambiar de rol", textGray) {
                        showRoleSheet = true
                    }
                }
                if (activeSubscription != null) {
                    Divider(color = dividerGray)

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

                Divider(color = dividerGray)
                SettingsItemLine(Icons.Filled.ExitToApp, "Cerrar sesión", redSoft) {
                    viewModel.signOut(onSignOut)
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // ✅ Selector de rol
    if (showRoleSheet) {
        ModalBottomSheet(
            onDismissRequest = { showRoleSheet = false },
            sheetState = sheetState,
            containerColor = Color.White,
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
                    fontSize = MaterialTheme.typography.titleLarge.fontSize,
                    color = Color(0xFF2D3436)
                )
                Text(
                    "Selecciona cómo quieres usar U-Park",
                    fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                    color = Color.Gray
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
                            containerColor = if (isActive) redLight else Color(0xFFF5F7FA)
                        ),
                        border = if (isActive) BorderStroke(2.dp, redSoft) else null
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
                                        if (isActive) redSoft.copy(alpha = 0.1f)
                                        else Color.White,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    icon,
                                    contentDescription = null,
                                    tint = if (isActive) redSoft else Color.Gray,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = rol,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                                    color = if (isActive) redSoft else Color(0xFF2D3436)
                                )
                                Text(
                                    text = description,
                                    fontSize = MaterialTheme.typography.bodySmall.fontSize,
                                    color = Color.Gray
                                )
                            }

                            if (isActive) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = "Rol activo",
                                    tint = redSoft,
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