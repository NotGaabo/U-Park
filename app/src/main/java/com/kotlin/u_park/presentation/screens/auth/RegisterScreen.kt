package com.kotlin.u_park.presentation.screens.auth

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.kotlin.u_park.R
import com.kotlin.u_park.data.remote.SessionManager
import com.kotlin.u_park.data.repository.AuthRepositoryImpl
import com.kotlin.u_park.domain.model.User
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// 🔹 Estados de registro
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    navController: NavController,
    supabase: SupabaseClient
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager.getInstance(context, supabase) }
    val authRepository = remember { AuthRepositoryImpl(supabase) }

    val authViewModel: AuthViewModel =
        viewModel(factory = AuthViewModelFactory(authRepository, sessionManager, appContext = context.applicationContext))

    val authState by authViewModel.currentUser.collectAsState(initial = null)

    var currentStep by remember { mutableStateOf(1) }

    var nombre by remember { mutableStateOf("") }
    var usuario by remember { mutableStateOf("") }
    var cedula by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }

    var contrasena by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var mensaje by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    val redPrimary = Color(0xFFE60023)
    val redLight = Color(0xFFFFF5F6)

    // Reglas de contraseña
    val hasUppercase = contrasena.any { it.isUpperCase() }
    val hasNumber = contrasena.any { it.isDigit() }
    val hasSpecial = contrasena.any { !it.isLetterOrDigit() }
    val hasLength = contrasena.length >= 8
    val passwordValid = hasUppercase && hasNumber && hasSpecial && hasLength

    // Observando cambios en el estado de registro
    LaunchedEffect(authViewModel.currentUser) {
        authViewModel.currentUser.collect { user ->
            user?.let {
                mensaje = "Registro exitoso 🎉"
                delay(800)
                navController.navigate("login") {
                    popUpTo("register") { inclusive = true }
                }
            }
        }
    }

    Scaffold { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(listOf(redLight.copy(alpha = 0.5f), Color.White))
                )
                .padding(padding)
                .padding(horizontal = 28.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.up),
                    contentDescription = "App logo",
                    modifier = Modifier.size(90.dp)
                )

                StepIndicator(currentStep = currentStep, totalSteps = 2)

                Crossfade(targetState = currentStep) { step ->
                    when (step) {
                        1 -> StepOne(
                            nombre = nombre,
                            usuario = usuario,
                            cedula = cedula,
                            telefono = telefono,
                            correo = correo,
                            onNombreChange = { nombre = it },
                            onUsuarioChange = { usuario = it },
                            onCedulaChange = { cedula = it },
                            onTelefonoChange = { telefono = it },
                            onCorreoChange = { correo = it }
                        )
                        2 -> StepTwo(
                            contrasena = contrasena,
                            passwordVisible = passwordVisible,
                            onPasswordChange = { contrasena = it },
                            onToggleVisibility = { passwordVisible = !passwordVisible }
                        )
                    }
                }

                // Botón inferior
                Button(
                    onClick = {
                        if (currentStep == 1) {
                            if (nombre.isBlank() || usuario.isBlank() || cedula.isBlank() ||
                                telefono.isBlank() || correo.isBlank()
                            ) {
                                mensaje = "Por favor completa todos los campos del paso 1"
                                return@Button
                            }
                            currentStep = 2
                        } else {
                            if (!passwordValid) {
                                mensaje = "Tu contraseña no cumple con los requisitos"
                                return@Button
                            }

                            val user = User(
                                nombre = nombre,
                                usuario = usuario,
                                cedula = cedula.toLong(),
                                telefono = telefono,
                                correo = correo,
                                contrasena = contrasena
                            )

                            scope.launch {
                                authViewModel.signUp(user)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = redPrimary)
                ) {
                    Text(
                        text = if (currentStep == 1) stringResource(R.string.siguiente) else stringResource(
                            R.string.crear_cuenta
                        ),
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }

                // 🔹 Texto debajo del botón
                TextButton(onClick = { navController.navigate("login") }) {
                    Text(stringResource(R.string.ya_tienes_cuenta_inicia_sesi_n), color = redPrimary)
                }

                if (currentStep == 2) {
                    TextButton(onClick = { currentStep = 1 }) {
                        Text(stringResource(R.string.volver_al_paso_anterior)
                            , color = redPrimary)
                    }
                }

                if (mensaje.isNotEmpty()) {
                    Text(
                        text = mensaje,
                        color = if (mensaje.contains("exitoso")) Color(0xFF4CAF50) else Color.Red,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

// 🔹 Indicador tipo iOS
@Composable
fun StepIndicator(currentStep: Int, totalSteps: Int) {
    val activeColor = Color(0xFFE60023)
    val inactiveColor = Color.LightGray.copy(alpha = 0.6f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalSteps) { index ->
            val isActive = index + 1 == currentStep
            val barColor by animateColorAsState(
                targetValue = if (isActive) activeColor else inactiveColor,
                label = "stepColor"
            )

            Box(
                modifier = Modifier
                    .height(6.dp)
                    .width(32.dp)
                    .clip(RoundedCornerShape(50))
                    .background(barColor)
            )

            if (index != totalSteps - 1) Spacer(modifier = Modifier.width(8.dp))
        }
    }
}

// 🔹 Paso 1
@Composable
fun StepOne(
    nombre: String,
    usuario: String,
    cedula: String,
    telefono: String,
    correo: String,
    onNombreChange: (String) -> Unit,
    onUsuarioChange: (String) -> Unit,
    onCedulaChange: (String) -> Unit,
    onTelefonoChange: (String) -> Unit,
    onCorreoChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            stringResource(R.string.ingresa_tus_datos_personales),
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFFE60023),
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        RoundedInputField(nombre, onNombreChange, stringResource(R.string.nombre_completo))
        RoundedInputField(usuario, onUsuarioChange, stringResource(R.string.nombre_de_usuario))
        RoundedInputField(cedula, onCedulaChange,
            stringResource(R.string.c_dula), KeyboardType.Number)
        RoundedInputField(telefono, onTelefonoChange,
            stringResource(R.string.tel_fono), KeyboardType.Phone)
        RoundedInputField(correo, onCorreoChange,
            stringResource(R.string.correo_electr_nico2), KeyboardType.Email)
    }
}

// 🔹 Paso 2
@Composable
fun StepTwo(
    contrasena: String,
    passwordVisible: Boolean,
    onPasswordChange: (String) -> Unit,
    onToggleVisibility: () -> Unit
) {
    val redPrimary = Color(0xFFE60023)

    val hasUppercase = contrasena.any { it.isUpperCase() }
    val hasNumber = contrasena.any { it.isDigit() }
    val hasSpecial = contrasena.any { !it.isLetterOrDigit() }
    val hasLength = contrasena.length >= 8

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            stringResource(R.string.configura_tu_contrase_a),
            fontWeight = FontWeight.SemiBold,
            color = redPrimary,
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = contrasena,
            onValueChange = onPasswordChange,
            label = { Text(stringResource(R.string.contrase_a2)) },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = if (!passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            trailingIcon = {
                IconButton(onClick = { onToggleVisibility() }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null,
                        tint = redPrimary
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = redPrimary.copy(alpha = 0.7f),
                unfocusedBorderColor = Color.LightGray,
                cursorColor = redPrimary
            )
        )

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            PasswordRequirementRow(stringResource(R.string.m_nimo_8_caracteres), hasLength)
            PasswordRequirementRow(stringResource(R.string.al_menos_una_may_scula), hasUppercase)
            PasswordRequirementRow(stringResource(R.string.al_menos_un_n_mero), hasNumber)
            PasswordRequirementRow(stringResource(R.string.un_car_cter_especial), hasSpecial)
        }
    }
}

@Composable
fun PasswordRequirementRow(text: String, met: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(if (met) Color(0xFF4CAF50) else Color.LightGray)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, color = if (met) Color.Black else Color.Gray, fontSize = 13.sp)
    }
}

// 🔹 Input redondeado
@Composable
fun RoundedInputField(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    val redPrimary = Color(0xFFE60023)
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = redPrimary.copy(alpha = 0.7f),
            unfocusedBorderColor = Color.LightGray,
            cursorColor = redPrimary,
            focusedLabelColor = redPrimary
        )
    )
}
