package com.kotlin.u_park.presentation.screens.suscriptions

import com.kotlin.u_park.domain.model.SubscriptionPlan
import com.kotlin.u_park.domain.model.User  // ✅ Importar User
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionScreen(
    userId: String,
    garageId: String,
    user: User?,  // ✅ RECIBIR USUARIO COMO PARÁMETRO
    viewModel: SubscriptionViewModel,
    onSuccess: () -> Unit
) {

    LaunchedEffect(userId, garageId) {
        viewModel.loadGarageData(userId, garageId)
    }

    // ✅ USAR EL USUARIO RECIBIDO COMO PARÁMETRO
    val plans = viewModel.plans
    val loading = viewModel.loading
    val spaces = viewModel.availableSpaces
    val error = viewModel.error

    val cedula = user?.cedula?.toString() ?: ""
    val nombre = user?.nombre ?: ""
    val telefono = user?.telefono ?: ""

    var expanded by remember { mutableStateOf(false) }
    var selectedPlan by remember { mutableStateOf<SubscriptionPlan?>(null) }

    val filteredPlans = plans.filter { it.max_vehicles <= spaces }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text("Suscribirse al garage", fontSize = 22.sp, fontWeight = FontWeight.Bold)

        // ✅ MOSTRAR ERROR SI EXISTE
        error?.let {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        Text(
            "Espacios disponibles: $spaces",
            color = if (spaces > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        )

        OutlinedTextField(
            value = cedula,
            onValueChange = {},
            label = { Text("Cédula") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true
        )

        OutlinedTextField(
            value = nombre,
            onValueChange = {},
            label = { Text("Nombre completo") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true
        )

        OutlinedTextField(
            value = telefono,
            onValueChange = {},
            label = { Text("Teléfono") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                readOnly = true,
                value = selectedPlan?.name ?: "",
                onValueChange = {},
                label = { Text("Seleccionar plan") },
                trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) }
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                filteredPlans.forEach { plan ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(plan.name, fontWeight = FontWeight.Medium)
                                Text(
                                    "Vehículos: ${plan.max_vehicles} · RD$ ${plan.price}",
                                    fontSize = 12.sp
                                )
                            }
                        },
                        onClick = {
                            selectedPlan = plan
                            expanded = false
                        }
                    )
                }

                if (filteredPlans.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("No hay planes disponibles", color = MaterialTheme.colorScheme.error) },
                        onClick = {}
                    )
                }
            }
        }

        Button(
            onClick = {
                selectedPlan?.let {
                    viewModel.requestSubscription(
                        userId = userId,
                        garageId = garageId,
                        planId = it.id,
                        onSuccess = onSuccess
                    )
                }
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            enabled = selectedPlan != null && !loading && spaces > 0
        ) {
            if (loading) {
                CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
            } else {
                Text("Solicitar suscripción")
            }
        }
    }
}