package com.kotlin.u_park.presentation.screens.rates

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.kotlin.u_park.domain.model.Rate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RatesScreen(
    navController: NavController,
    viewModel: RatesViewModel,
    garageId: String,
    onCreateRate: () -> Unit,
    onEditRate: (String) -> Unit
) {

    val rates by viewModel.rates

    LaunchedEffect(garageId) {
        viewModel.loadRates(garageId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tarifas del Garage") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateRate) {
                Icon(Icons.Default.Add, null)
            }
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {

            if (rates.isEmpty()) {
                Text(
                    "No hay tarifas registradas",
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(rates) { rate ->
                        RateItemCard(
                            rate = rate,
                            onEdit = { onEditRate(rate.id!!) },
                            onDelete = { viewModel.deleteRate(rate.id!!) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RateItemCard(
    rate: Rate,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text("${rate.baseRate} / ${rate.timeUnit}", style = MaterialTheme.typography.titleMedium)

            Spacer(Modifier.height(4.dp))

            Text("Tipo vehículo: ${rate.vehicleTypeId ?: "Cualquiera"}")
            Text("Días aplicables: ${rate.diasAplicables.joinToString()}")

            rate.specialRate?.let {
                Text("Especial: $it")
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                }
            }
        }
    }
}
