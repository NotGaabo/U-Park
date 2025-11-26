package com.kotlin.u_park.presentation.screens.parking

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kotlin.u_park.domain.model.Parking
import com.kotlin.u_park.domain.model.ParkingTicket
import com.kotlin.u_park.domain.model.Reserva
import com.kotlin.u_park.domain.repository.ParkingRepository
import com.kotlin.u_park.domain.repository.ReservasRepository
import com.kotlin.u_park.presentation.utils.PdfGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.time.Duration
import java.time.OffsetDateTime

class ParkingViewModel(
    private val repository: ParkingRepository,
    private val reservasRepository: ReservasRepository
) : ViewModel() {

    private val _ticket = MutableStateFlow<ParkingTicket?>(null)
    private val _reservas = MutableStateFlow<List<Reserva>>(emptyList())
    val reservas: StateFlow<List<Reserva>> = _reservas
    val ticket: StateFlow<ParkingTicket?> = _ticket

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message


    // ----------------------------------------------------
    // REGISTRAR ENTRADA
    // ----------------------------------------------------
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
                val horaEntrada = OffsetDateTime.now().toString()

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

            } catch (e: Exception) {
                _message.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }


    // ----------------------------------------------------
    // REGISTRAR SALIDA
    // ----------------------------------------------------
    @RequiresApi(Build.VERSION_CODES.O)
    fun registrarSalida(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.registrarSalida(id, OffsetDateTime.now().toString())
                _message.value = "Salida registrada correctamente"
            } catch (e: Exception) {
                _message.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }


    // ----------------------------------------------------
    // CREAR RESERVA (NUEVO SISTEMA)
    // ----------------------------------------------------
    @RequiresApi(Build.VERSION_CODES.O)
    fun crearReserva(garageId: String, vehicleId: String, fecha: String, userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val reserva = Parking(
                    id = null,
                    garageId = garageId,
                    vehicleId = vehicleId,
                    empleadoId = null,
                    horaEntrada = fecha,
                    tipo = "reserva",
                    estado = "pendiente"
                )

                reservasRepository.crearReserva(reserva)
                _message.value = "Reserva creada correctamente"

            } catch (e: Exception) {
                _message.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
    // ----------------------------------------------------
    // LISTAR RESERVAS DEL GARAGE
    // ----------------------------------------------------
    fun getReservasByGarage(garageId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val list = reservasRepository.listarReservasPorGarage(garageId)
                _reservas.value = list
            } catch (e: Exception) {
                _message.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }


    // ----------------------------------------------------
    // CANCELAR RESERVA
    // ----------------------------------------------------
    fun cancelarReserva(id: String) {
        viewModelScope.launch {
            reservasRepository.cancelarReserva(id)
            _message.value = "Reserva cancelada"
        }
    }


    // ----------------------------------------------------
    // ACTIVAR RESERVA
    // ----------------------------------------------------
    fun activarReserva(id: String) {
        viewModelScope.launch {
            reservasRepository.activarReserva(id)
            _message.value = "Reserva activada"
        }
    }


    // ----------------------------------------------------
    // Obtener Parking por ID
    // ----------------------------------------------------
    fun obtenerParking(id: Int, onResult: (Parking?) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                onResult(repository.getParkingById(id))
            } finally {
                _isLoading.value = false
            }
        }
    }


    // ----------------------------------------------------
    // Calcular precio
    // ----------------------------------------------------
    @RequiresApi(Build.VERSION_CODES.O)
    fun calcularPrecio(horaEntrada: String, horaSalida: String, tarifaHora: Double): Double {
        val inicio = OffsetDateTime.parse(horaEntrada)
        val fin = OffsetDateTime.parse(horaSalida)

        val minutos = Duration.between(inicio, fin).toMinutes().toDouble()
        val horas = minutos / 60.0

        return horas * tarifaHora
    }


    // ----------------------------------------------------
    // PDF
    // ----------------------------------------------------
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun generarPdf(ctx: Context, ticket: ParkingTicket): File =
        PdfGenerator.generateFactura(ctx, ticket)
}
