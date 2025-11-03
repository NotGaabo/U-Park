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

    val background = Color(0xFFF9F9F9)
    val textGray = Color(0xFF6B6B6B)
    val dividerGray = Color(0xFFEAEAEA)
    val redSoft = Color(0xFFE60023)
    val redLight = Color(0xFFFFE5E5)

    val currentUser by viewModel.currentUser.collectAsState()
    val activeRole by viewModel.activeRole.collectAsState()
    var showRoleSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = background,
        topBar = {
            TopAppBar(
                title = { Text("Perfil", fontWeight = FontWeight.Bold, color = Color.Black) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = background)
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(Routes.Home.route) },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Inicio") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(Routes.GarageAdd.route) },
                    icon = { Icon(Icons.Default.AddCircle, contentDescription = "Agregar") },
                    label = { Text("Agregar") }
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

                Divider(color = dividerGray)
                SettingsItemLine(Icons.Filled.ExitToApp, "Cerrar sesión", redSoft) {
                    viewModel.signOut(onSignOut)
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // ✅ Selector de rol corregido
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
                Text("Selecciona un rol", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))

                currentUser?.roles?.forEach { rol ->
                    val isActive = rol.equals(activeRole, ignoreCase = true)

                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                viewModel.getSessionManager().saveActiveRole(rol)
                                delay(200)
                                showRoleSheet = false

                                val normalizedRole = rol.lowercase().replace("-", "").replace("_", "")
                                navController.navigate(
                                    when (normalizedRole) {
                                        "duenogarage" -> Routes.DuenoGarage.route
                                        "employee" -> "employeeHome"
                                        "user" -> Routes.Home.route
                                        else -> Routes.Login.route
                                    }
                                ) {
                                    popUpTo(0)
                                    launchSingleTop = true
                                }
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
                        Icon(Icons.Default.Person, contentDescription = null, tint = if (isActive) redSoft else Color.Black)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = rol, color = if (isActive) redSoft else Color.Black)
                    }
                }
            }
        }
    }
}
