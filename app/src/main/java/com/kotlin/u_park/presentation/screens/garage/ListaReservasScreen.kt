package com.kotlin.u_park.presentation.screens.garage

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.kotlin.u_park.presentation.screens.parking.ParkingViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ListaReservasScreen(
    viewModel: ParkingViewModel,
    garageId: String
) {
    val reservas by viewModel.reservas.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.getReservasByGarage(garageId)
    }

    Column(Modifier.fillMaxSize().padding(20.dp)) {

        Text("Reservas Pendientes", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))

        reservas.forEach { r ->

            Card(
                Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                Column(Modifier.padding(16.dp)) {

                    Text("Vehículo: ${r.vehicle_id}")
                    Text("Estado: ${r.estado}")
                    Text("Fecha: ${r.hora_reserva}")

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {

                        Button(onClick = { viewModel.activarReserva(r.id!!) }) {
                            Text("Activar")
                        }

                        Button(onClick = { viewModel.cancelarReserva(r.id!!) }) {
                            Text("Cancelar")
                        }
                    }
                }
            }
        }
    }
}
