package com.kotlin.u_park.ui.screens.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.kotlin.u_park.R
import com.kotlin.u_park.data.remote.SessionManager
import com.kotlin.u_park.data.repository.*
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    supabase: SupabaseClient,
    authViewModel: AuthViewModel
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager.getInstance(context, supabase) }
    val authRepository = remember { AuthRepository(supabase) }
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(authRepository, sessionManager)
    )

    val authState by authViewModel.authState.collectAsState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Loading -> mensaje = "Iniciando sesión..."
            is AuthState.Success -> {
                mensaje = "Inicio de sesión exitoso"
                navController.navigate("home") {
                    popUpTo("login") { inclusive = true }
                }
            }
            is AuthState.Error -> mensaje = (authState as AuthState.Error).message
            else -> {}
        }
    }

    // 🎨 Colores suaves
    val redPrimary = Color(0xFFE60023)
    val redLight = Color(0xFFFFF5F6)
    val white = Color.White
    val iconGray = Color(0xFF444444)
    val textGray = Color(0xFF333333)

    Scaffold(containerColor = white) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(redLight.copy(alpha = 0.3f), white)
                    )
                )
                .padding(padding)
                .padding(horizontal = 28.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 🖼️ Logo
                Image(
                    painter = painterResource(id = R.drawable.up),
                    contentDescription = "App logo",
                    modifier = Modifier
                        .size(90.dp)
                        .padding(bottom = 10.dp),
                    contentScale = ContentScale.Fit
                )

                Text(
                    text = "Bienvenido de nuevo",
                    style = MaterialTheme.typography.titleMedium,
                    color = redPrimary
                )

                // ✉️ Email
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Correo electrónico") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Email,
                            contentDescription = null,
                            tint = iconGray
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = redPrimary.copy(alpha = 0.6f),
                        unfocusedBorderColor = Color.LightGray,
                        cursorColor = redPrimary,
                        focusedLabelColor = redPrimary,
                        focusedTextColor = textGray,
                        unfocusedTextColor = textGray
                    )
                )

                // 🔒 Contraseña
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            tint = iconGray
                        )
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible)
                                    Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null,
                                tint = iconGray
                            )
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = redPrimary.copy(alpha = 0.6f),
                        unfocusedBorderColor = Color.LightGray,
                        cursorColor = redPrimary,
                        focusedLabelColor = redPrimary,
                        focusedTextColor = textGray,
                        unfocusedTextColor = textGray
                    )
                )

                // 🔘 Botón de iniciar sesión
                Button(
                    onClick = {
                        if (email.isBlank() || password.isBlank()) {
                            mensaje = "Por favor completa todos los campos"
                            return@Button
                        }
                        scope.launch { authViewModel.signIn(email, password) }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = redPrimary,
                        contentColor = white
                    )
                ) {
                    if (authState is AuthState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = white,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Iniciar sesión", fontSize = 16.sp)
                    }
                }

                // 🔁 Olvidar contraseña
                TextButton(onClick = { /* TODO */ }) {
                    Text("¿Olvidaste tu contraseña?", color = redPrimary, fontSize = 14.sp)
                }

                // ➕ Crear cuenta
                OutlinedButton(
                    onClick = { navController.navigate("register") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = redPrimary),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        width = 1.dp,
                        brush = Brush.horizontalGradient(listOf(redPrimary, Color.LightGray))
                    )
                ) {
                    Text("Crear nueva cuenta", fontSize = 15.sp)
                }

                // 📣 Mensaje de feedback
                if (mensaje.isNotEmpty()) {
                    Text(
                        text = mensaje,
                        color = if (mensaje.contains("exitoso")) redPrimary else Color.Red,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
