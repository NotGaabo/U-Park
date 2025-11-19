package com.kotlin.u_park.presentation.screens.parking

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kotlin.u_park.domain.model.Parking
import com.kotlin.u_park.domain.model.ParkingTicket
import com.kotlin.u_park.domain.repository.ParkingRepository
import com.kotlin.u_park.presentation.utils.PdfGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

class ParkingViewModel(
    private val repository: ParkingRepository
) : ViewModel() {

    private val _ticket = MutableStateFlow<ParkingTicket?>(null)
    val ticket: StateFlow<ParkingTicket?> = _ticket

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    @RequiresApi(Build.VERSION_CODES.O)
    fun registrarEntrada(
        garageId: String,
        vehicleId: String,
        empleadoId: String,
        fotosBytes: List<ByteArray>
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {

                val horaEntrada = java.time.OffsetDateTime.now().toString()

                val parking = Parking(
                    id = null,
                    garageId = garageId,
                    vehicleId = vehicleId,
                    empleadoId = empleadoId,
                    horaEntrada = horaEntrada
                )

                val created = repository.registrarEntrada(parking, fotosBytes)

                _ticket.value = ParkingTicket(
                    plate = created.vehicleId,
                    horaEntrada = created.horaEntrada,
                    fotos = created.fotos,
                    garage = created.garageId,
                    parkingId = created.id?.toString() ?: ""
                )

            } finally {
                _isLoading.value = false
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun registrarSalida(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.registrarSalida(id, java.time.OffsetDateTime.now().toString())
            } finally {
                _isLoading.value = false
            }
        }
    }

    suspend fun generarPdf(ctx: Context, ticket: ParkingTicket): File =
        PdfGenerator.generateFactura(ctx, ticket)
}
