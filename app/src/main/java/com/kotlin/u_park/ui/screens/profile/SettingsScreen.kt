package com.kotlin.u_park.ui.screens.profile

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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.kotlin.u_park.R
import com.kotlin.u_park.domain.model.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    currentUser: User?,
    onSignOut: () -> Unit
) {
    val background = Color(0xFFF9F9F9)
    val textGray = Color(0xFF6B6B6B)
    val dividerGray = Color(0xFFEAEAEA)
    val redSoft = Color(0xFFE60023)

    Scaffold(
        containerColor = background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Profile",
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = background,
                    titleContentColor = Color.Black
                )
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("home") },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("add") },
                    icon = { Icon(Icons.Default.AddCircle, contentDescription = "Agregar") },
                    label = { Text("Agregar") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("history") },
                    icon = { Icon(Icons.Default.History, contentDescription = "Historial") },
                    label = { Text("Historial") }
                )
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Perfil", tint = redSoft) },
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

            // ------------------------
            // USER HEADER
            // ------------------------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { /* Navegar al perfil detallado si se desea */ }
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
            // USER INFO SUMMARY
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
            // SETTINGS SECTION
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

                // ✅ Mostrar solo si tiene más de un rol
                if ((currentUser?.roles?.size ?: 0) > 1) {
                    SettingsItemLine(
                        icon = Icons.Filled.SwapHoriz,
                        title = "Cambiar de rol",
                        iconColor = textGray
                    ) {
                        navController.navigate("admin")
                    }
                }

                Divider(color = dividerGray, thickness = 1.dp)
                SettingsItemLine(Icons.Filled.ExitToApp, "Cerrar sesión", redSoft, onClick = onSignOut)
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

// -------------------------
// COMPONENTES REUTILIZABLES
// -------------------------
@Composable
fun SettingsItemLine(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    iconColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = iconColor)
        Spacer(modifier = Modifier.width(18.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge.copy(color = Color.Black)
        )
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Color.Gray
        )
    }
}

@Composable
fun UserInfoLine(label: String, value: String) {
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.copy(
                color = Color.Black,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSettingsScreen() {
    val dummyUser = User(
        id = "1",
        nombre = "Judy",
        usuario = "judy123",
        cedula = "123456789",
        telefono = "+1 (809) 555-5555",
        correo = "judy@example.com",
        contrasena = "password123",
        roles = listOf("user", "admin")
    )

    SettingsScreen(
        navController = rememberNavController(),
        currentUser = dummyUser,
        onSignOut = {}
    )
}
