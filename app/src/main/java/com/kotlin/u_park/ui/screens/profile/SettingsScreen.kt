package com.kotlin.u_park.ui.screens.profile

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.kotlin.u_park.domain.model.User

private val RedSoft = Color(0xFFFF4D4D)
private val BackgroundColor = Color.White

@SuppressLint("MutableCollectionMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    currentUser: User? = null, // nullable
    userRoles: List<String> = emptyList(),
    allRoles: List<String> = emptyList(),
    onSaveRoles: (selectedRoles: List<String>) -> Unit = {}
) {
    var selectedRoles by remember { mutableStateOf(userRoles.toMutableSet()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, color = RedSoft) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundColor,
                    titleContentColor = RedSoft
                )
            )
        },
        bottomBar = {
            NavigationBar(containerColor = BackgroundColor) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.DirectionsCar, contentDescription = "Home", tint = RedSoft) },
                    label = { Text("Home", color = RedSoft) },
                    selected = false,
                    onClick = { navController.navigate("home") }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.AddCircle, contentDescription = "Agregar", tint = RedSoft) },
                    label = { Text("Agregar", color = RedSoft) },
                    selected = false,
                    onClick = { }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.History, contentDescription = "Historial", tint = RedSoft) },
                    label = { Text("Historial", color = RedSoft) },
                    selected = false,
                    onClick = { }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Perfil", tint = RedSoft) },
                    label = { Text("Perfil", color = RedSoft) },
                    selected = true,
                    onClick = { }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(BackgroundColor)
                .padding(16.dp)
        ) {
            // Mostrar nombre de usuario si existe
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(RedSoft.copy(alpha = 0.1f))
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.Person, contentDescription = "Usuario", tint = RedSoft, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(currentUser?.nombre ?: "Usuario", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, fontSize = 18.sp)
                    Text("Mostrar perfil", color = Color.DarkGray, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Mostrar roles solo si existen
            if (userRoles.isNotEmpty()) {
                Text("Tus roles:", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))

                allRoles.forEach { role ->
                    val canChangeRole = userRoles.size > 1
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(RedSoft.copy(alpha = 0.05f))
                            .padding(vertical = 8.dp, horizontal = 12.dp)
                            .clickable(enabled = canChangeRole) {
                                if (selectedRoles.contains(role)) selectedRoles.remove(role)
                                else selectedRoles.add(role)
                            }
                    ) {
                        Checkbox(
                            checked = selectedRoles.contains(role),
                            onCheckedChange = { isChecked ->
                                if (canChangeRole) {
                                    if (isChecked) selectedRoles.add(role) else selectedRoles.remove(role)
                                }
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = RedSoft,
                                uncheckedColor = Color.Gray
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(role)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { onSaveRoles(selectedRoles.toList()) },
                    colors = ButtonDefaults.buttonColors(containerColor = RedSoft),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Guardar Roles", color = Color.White)
                }
            }
        }
    }
}
