package com.kotlin.u_park.ui.screens.register

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.kotlin.u_park.domain.model.User
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    navController: NavController,
    supabase: SupabaseClient? = null
) {
    val context = LocalContext.current
    val sessionManager = remember(supabase) {
        supabase?.let { SessionManager.getInstance(context, it) }
    }
    val authRepository = remember(supabase) {
        supabase?.let { AuthRepository(it) }
    }
    val authViewModel: AuthViewModel? = if (supabase != null && sessionManager != null && authRepository != null) {
        viewModel(factory = AuthViewModelFactory(authRepository, sessionManager))
    } else null

    val authState by authViewModel?.authState?.collectAsState() ?: remember { mutableStateOf(AuthState.Idle) }

    var nombre by remember { mutableStateOf("") }
    var usuario by remember { mutableStateOf("") }
    var cedula by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // 🎨 Paleta suave y minimalista
    val redPrimary = Color(0xFFE60023)
    val redLight = Color(0xFFFFF5F6)
    val white = Color.White

    val canNavigateBack = remember { navController.previousBackStackEntry != null }

    Scaffold(
    ) { padding ->

        // 🌸 Fondo blanco con toque rosado sutil
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            redLight.copy(alpha = 0.4f),
                            white
                        )
                    )
                )
                .padding(padding)
                .padding(horizontal = 28.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.up),
                    contentDescription = "App logo",
                    modifier = Modifier
                        .size(90.dp)
                        .padding(bottom = 10.dp)
                )

                Text(
                    "Crea tu cuenta para continuar",
                    style = MaterialTheme.typography.titleMedium,
                    color = redPrimary
                )

                // Campos
                RoundedInputField(value = nombre, onChange = { nombre = it }, label = "Nombre completo")
                RoundedInputField(value = usuario, onChange = { usuario = it }, label = "Nombre de usuario")
                RoundedInputField(value = cedula, onChange = { cedula = it }, label = "Cédula", keyboardType = KeyboardType.Number)
                RoundedInputField(
                    value = telefono,
                    onChange = {
                        telefono = it
                            .replace(Regex("[^\\d+]"), "") // solo permite dígitos y el "+"
                    },
                    label = "Teléfono",
                    keyboardType = KeyboardType.Phone
                )

                RoundedInputField(value = correo, onChange = { correo = it }, label = "Correo electrónico", keyboardType = KeyboardType.Email)

                RoundedInputField(
                    value = contrasena,
                    onChange = { contrasena = it },
                    label = "Contraseña",
                    isPassword = true,
                    passwordVisible = passwordVisible,
                    onPasswordToggle = { passwordVisible = !passwordVisible }
                )

                Button(
                    onClick = {
                        if (nombre.isBlank() || usuario.isBlank() || correo.isBlank() || contrasena.isBlank()) {
                            mensaje = "Por favor completa todos los campos"
                            return@Button
                        }
                        val user = User(
                            nombre = nombre,
                            usuario = usuario,
                            cedula = cedula,
                            telefono = telefono,
                            correo = correo,
                            contrasena = contrasena
                        )
                        scope.launch { authViewModel?.signUp(user) }
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
                        Text("Crear cuenta", fontSize = 16.sp)
                    }
                }

                TextButton(onClick = { navController.navigate("login") }) {
                    Text("¿Ya tienes cuenta? Inicia sesión", color = redPrimary)
                }

                if (mensaje.isNotEmpty()) {
                    Text(
                        text = mensaje,
                        color = if (mensaje.contains("exitoso")) redPrimary else Color.Red
                    )
                }
            }
        }
    }
}

@Composable
fun RoundedInputField(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onPasswordToggle: (() -> Unit)? = null
) {
    val redPrimary = Color(0xFFEF5350)

    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        trailingIcon = if (isPassword) {
            {
                IconButton(onClick = { onPasswordToggle?.invoke() }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null,
                        tint = redPrimary
                    )
                }
            }
        } else null,
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = redPrimary.copy(alpha = 0.6f),
            unfocusedBorderColor = Color.LightGray,
            cursorColor = redPrimary,
            focusedLabelColor = redPrimary
        )
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RegisterScreenPreview() {
    val navController = rememberNavController()
    RegisterScreen(navController = navController)
}
