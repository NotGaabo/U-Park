package com.kotlin.u_park.presentation.screens.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.kotlin.u_park.R
import com.kotlin.u_park.domain.model.User
import com.kotlin.u_park.data.remote.SessionManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreenDueno(
    navController: NavController,
    currentUser: User?,
    sessionManager: SessionManager,
    onSignOut: () -> Unit
) {
    val background = Color(0xFFF9F9F9)
    val textGray = Color(0xFF6B6B6B)
    val dividerGray = Color(0xFFEAEAEA)
    val redSoft = Color(0xFFE60023)
    val redLight = Color(0xFFFFE5E5)

    var showRoleSheet by remember { mutableStateOf(false) }
    var activeRole by remember { mutableStateOf<String?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    // ✅ Cargar rol activo guardado o del usuario
    LaunchedEffect(currentUser) {
        val savedRole = sessionManager.getActiveRole()
        activeRole = savedRole ?: currentUser?.roles?.firstOrNull()
    }

    Scaffold(
        containerColor = background,
        topBar = {
            TopAppBar(
                title = { Text("Profile", fontWeight = FontWeight.Bold, color = Color.Black) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = background)
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("empleados") },
                    icon = { Icon(Icons.Default.Group, contentDescription = "Empleados") },
                    label = { Text("Empleados") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("tarifas") },
                    icon = { Icon(Icons.Default.AttachMoney, contentDescription = "Tarifas") },
                    label = { Text("Tarifas") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("duenogarage") },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard", tint = redSoft) },
                    label = { Text("Dashboard", color = redSoft) }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("reservas") },
                    icon = { Icon(Icons.Default.Book, contentDescription = "Reservas") },
                    label = { Text("Reservas") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("settings") },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                    label = { Text("Perfil") }
                )
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

            // ------------------------
            // USER HEADER
            // ------------------------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { /* Ir al perfil detallado si se desea */ }
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
                        text = "Show profile",
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

            // ------------------------
            // PROMOTIONAL CARD
            // ------------------------
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
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

            Spacer(modifier = Modifier.height(40.dp))

            // ------------------------
            // ACCOUNT DETAILS
            // ------------------------
            Text(
                text = "Account details",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White)
            ) {
                UserInfoLine("Correo electrónico", currentUser?.correo ?: "No disponible")
                Divider(color = dividerGray, thickness = 1.dp)
                UserInfoLine("Teléfono", currentUser?.telefono ?: "No disponible")
                Divider(color = dividerGray, thickness = 1.dp)
                UserInfoLine("Usuario", currentUser?.usuario ?: "No disponible")
            }

            Spacer(modifier = Modifier.height(40.dp))

            // ------------------------
            // SETTINGS
            // ------------------------
            Text(
                text = "Settings",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White)
            ) {
                SettingsItemLine(Icons.Filled.Person, "Información personal", textGray) { }
                Divider(color = dividerGray, thickness = 1.dp)
                SettingsItemLine(Icons.Filled.Security, "Privacidad y seguridad", textGray) { }
                Divider(color = dividerGray, thickness = 1.dp)
                SettingsItemLine(Icons.Filled.Notifications, "Notificaciones", textGray) { }
                Divider(color = dividerGray, thickness = 1.dp)
                SettingsItemLine(Icons.Filled.Info, "Acerca de U-Park", textGray) { }

                if ((currentUser?.roles?.size ?: 0) > 1) {
                    Divider(color = dividerGray, thickness = 1.dp)
                    SettingsItemLine(Icons.Filled.SwapHoriz, "Cambiar de rol", textGray) {
                        showRoleSheet = true
                    }
                }

                Divider(color = dividerGray, thickness = 1.dp)
                SettingsItemLine(Icons.Filled.ExitToApp, "Cerrar sesión", redSoft, onClick = onSignOut)
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // ------------------------
    // ROLE SELECTION SHEET
    // ------------------------
    if (showRoleSheet) {
        ModalBottomSheet(
            onDismissRequest = { showRoleSheet = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Selecciona un rol",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(16.dp))

                currentUser?.roles?.forEach { rol ->
                    val (label, icon) = when (rol.lowercase()) {
                        "dueno-garage" -> "Dueño de garaje" to Icons.Default.Business
                        "employee" -> "Empleado" to Icons.Default.Badge
                        "user" -> "Usuario" to Icons.Default.Person
                        else -> rol to Icons.Default.Help
                    }

                    val isActive = rol == activeRole

                    OutlinedButton(
                        onClick = {
                            activeRole = rol
                            showRoleSheet = false
                            scope.launch { sessionManager.saveActiveRole(rol) }

                            when (rol.lowercase()) {
                                "dueno-garage" -> navController.navigate("duenogarage")
                                "employee" -> navController.navigate("employeeHome")
                                "user" -> navController.navigate("home")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isActive) redLight else Color.White
                        )
                    ) {
                        Icon(icon, contentDescription = null, tint = if (isActive) redSoft else Color.Black)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(label, color = if (isActive) redSoft else Color.Black)
                    }
                }
            }
        }
    }
}
