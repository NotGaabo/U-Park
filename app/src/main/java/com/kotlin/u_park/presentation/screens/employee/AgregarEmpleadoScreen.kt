package com.kotlin.u_park.presentation.screens.employee

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kotlin.u_park.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgregarEmpleadoScreen(
    garageId: String,
    viewModel: EmpleadosViewModel,
    onClose: () -> Unit
) {
    var cedulaText by remember { mutableStateOf("") }
    var cedulaError by remember { mutableStateOf<String?>(null) }

    val isLoading by viewModel.isLoading.collectAsState()
    val isSuccess by viewModel.isSuccess.collectAsState()

    LaunchedEffect(isSuccess) {
        if (isSuccess == true) onClose()
    }

    // Validación en tiempo real
    LaunchedEffect(cedulaText) {
        cedulaError = when {
            cedulaText.isBlank() -> "La cédula no puede estar vacía"
            !cedulaText.all { it.isDigit() } -> "Solo se permiten números"
            cedulaText.length !in 8..11 -> "Cédula inválida"
            else -> null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.agregar_empleado),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFE60023),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F7FA))
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(Modifier.height(16.dp))

                // Ícono principal
                Surface(
                    modifier = Modifier.size(100.dp),
                    shape = RoundedCornerShape(50.dp),
                    color = Color(0xFFE60023).copy(alpha = 0.15f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.PersonAdd,
                            contentDescription = null,
                            tint = Color(0xFFE60023),
                            modifier = Modifier.size(50.dp)
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Título
                Text(
                    stringResource(R.string.nuevo_empleado),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2D3436)
                )

                Spacer(Modifier.height(8.dp))

                // Subtítulo
                Text(
                    stringResource(R.string.ingresa_la_c_dula_del_empleado_para_agregarlo_al_garage),
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                Spacer(Modifier.height(32.dp))

                // Card principal
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(Modifier.padding(24.dp)) {

                        Text(
                            "Cédula del empleado",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF2D3436)
                        )

                        Spacer(Modifier.height(12.dp))

                        OutlinedTextField(
                            value = cedulaText,
                            onValueChange = { cedulaText = it },
                            label = { Text(stringResource(R.string.c_dula_del_empleado)) },
                            placeholder = { Text(stringResource(R.string.ej_40212345678)) },
                            isError = cedulaError != null,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFE60023),
                                focusedLabelColor = Color(0xFFE60023),
                                unfocusedBorderColor = Color(0xFFDFE6E9)
                            )
                        )

                        if (cedulaError != null) {
                            Spacer(Modifier.height(8.dp))
                            Surface(
                                color = Color.Red.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = cedulaError!!,
                                    color = Color.Red,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(28.dp))

                        // Botón principal
                        Button(
                            onClick = {
                                val cedulaLong = cedulaText.toLong()
                                android.util.Log.d("UI_ADD", "CEDULA=$cedulaLong")
                                viewModel.addEmpleado(garageId, cedulaLong)
                            },
                            enabled = cedulaError == null && !isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF00B894),
                                disabledContainerColor = Color(0xFF00B894).copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.5.dp,
                                    color = Color.White
                                )
                            } else {
                                Icon(
                                    Icons.Default.PersonAdd,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    stringResource(R.string.agregar_empleado2),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // Botón secundario
                        OutlinedButton(
                            onClick = onClose,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.Gray
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                stringResource(R.string.cancelar),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Texto informativo
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(6.dp),
                        shape = RoundedCornerShape(3.dp),
                        color = Color(0xFFE60023)
                    ) {}
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "El empleado recibirá una notificación al ser agregado",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}