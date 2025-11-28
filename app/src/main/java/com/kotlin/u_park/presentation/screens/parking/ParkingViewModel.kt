package com.kotlin.u_park.presentation.screens.parking

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kotlin.u_park.domain.model.Parking
import com.kotlin.u_park.domain.model.ParkingActividad
import com.kotlin.u_park.domain.model.ParkingTicket
import com.kotlin.u_park.domain.model.Reserva
import com.kotlin.u_park.domain.model.ReservaConUsuario
import com.kotlin.u_park.domain.repository.ParkingRepository
import com.kotlin.u_park.domain.repository.ReservasRepository
import com.kotlin.u_park.presentation.utils.PdfGenerator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.time.OffsetDateTime

class ParkingViewModel(
    private val repository: ParkingRepository,
    private val reservasRepository: ReservasRepository
) : ViewModel() {

    private val _ticket = MutableStateFlow<ParkingTicket?>(null)
    val ticket = _ticket.asStateFlow()

    private val _reservasConUsuario = MutableStateFlow<List<ReservaConUsuario>>(emptyList())
    val reservasConUsuario = _reservasConUsuario.asStateFlow()

    private val _vehiculosDentro = MutableStateFlow<List<String>>(emptyList())
    val vehiculosDentro = _vehiculosDentro.asStateFlow()

    private val _actividad = MutableStateFlow<List<ParkingActividad>>(emptyList())
    val actividad = _actividad.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message = _message.asStateFlow()

    // ---------------------------------------------------------
    // VEHÍCULOS DENTRO (cache)
    // ---------------------------------------------------------
    fun actualizarVehiculosDentro() {
        viewModelScope.launch {
            val dentro = repository.getVehiculosDentro()
            _vehiculosDentro.value = dentro.mapNotNull { it.vehicle_id }
        }
    }

    private fun estaVehiculoDentroCache(vehicleId: String): Boolean =
        _vehiculosDentro.value.contains(vehicleId)

    // ---------------------------------------------------------
    // REGISTRAR ENTRADA NORMAL
    // ---------------------------------------------------------
    @RequiresApi(Build.VERSION_CODES.O)
    fun registrarEntrada(
        garageId: String,
        vehicleId: String,
        empleadoId: String,
        fotosBytes: List<ByteArray>
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                actualizarVehiculosDentro()

                val hora = OffsetDateTime.now().toString()

                val parking = Parking(
                    id = null,
                    garage_id = garageId,
                    vehicle_id = vehicleId,
                    created_by_user_id = empleadoId,
                    hora_entrada = hora,
                    tipo = "entrada",
                    estado = "activa",
                    fotos = emptyList()
                )

                val created = repository.registrarEntrada(parking, fotosBytes)

                _ticket.value = ParkingTicket(
                    plate = created.vehicle_id ?: "",
                    horaEntrada = created.hora_entrada,
                    fotos = created.fotos,
                    garage = created.garage_id ?: "",
                    parkingId = created.id ?: ""
                )
            } catch (e: Exception) {
                _message.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun crearReserva(
        garageId: String,
        vehicleId: String,
        fecha: String,
        userId: String
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                val reserva = Reserva(
                    garage_id = garageId,
                    vehicle_id = vehicleId,
                    hora_reserva = fecha,
                    estado = "pendiente",
                    empleado_id = userId
                )

                reservasRepository.crearReserva(reserva)
                _message.value = "Reserva creada correctamente"

            } catch (e: Exception) {
                _message.value = e.message ?: "Error creando reserva"
            } finally {
                _isLoading.value = false
            }
        }
    }


    // ---------------------------------------------------------
    // RESERVAS FILTRADAS
    // ---------------------------------------------------------
    fun loadReservasConUsuario(garageId: String) {
        viewModelScope.launch {
            try {
                actualizarVehiculosDentro()
                val lista = repository.getReservasConUsuario(garageId)

                _reservasConUsuario.value = lista.filter { r ->
                    r.vehicle_id != null && !estaVehiculoDentroCache(r.vehicle_id)
                }
            } catch (e: Exception) {
                _message.value = e.message
            }
        }
    }

    // ---------------------------------------------------------
    // ENTRADA DESDE RESERVA
    // ---------------------------------------------------------
    @RequiresApi(Build.VERSION_CODES.O)
    fun registrarEntradaDesdeReserva(
        reserva: ReservaConUsuario,
        fotosBytes: List<ByteArray>,
        empleadoId: String
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                if (estaVehiculoDentroCache(reserva.vehicle_id!!)) {
                    _message.value = "Este vehículo ya está dentro"
                    return@launch
                }

                val created = repository.registrarEntradaDesdeReserva(
                    reserva,
                    fotosBytes,
                    empleadoId
                )

                reservasRepository.actualizarEmpleadoReserva(reserva.id!!, empleadoId)

                _ticket.value = ParkingTicket(
                    plate = created.vehicle_id ?: "",
                    horaEntrada = created.hora_entrada,
                    fotos = created.fotos,
                    garage = created.garage_id ?: "",
                    parkingId = created.id ?: ""
                )

                _message.value = "Entrada registrada"

            } catch (e: Exception) {
                _message.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ---------------------------------------------------------
    // SALIDA
    // ---------------------------------------------------------
    @RequiresApi(Build.VERSION_CODES.O)
    fun registrarSalida(parkingId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.registrarSalida(parkingId, OffsetDateTime.now().toString())
                actualizarVehiculosDentro()
                _message.value = "Salida registrada"
            } catch (e: Exception) {
                _message.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ---------------------------------------------------------
    // RESERVAS: activar y cancelar
    // ---------------------------------------------------------
    fun cancelarReserva(id: String) {
        viewModelScope.launch {
            try {
                reservasRepository.cancelarReserva(id)
                _reservasConUsuario.value = _reservasConUsuario.value.filter { it.id != id }
                _message.value = "Reserva cancelada"
            } catch (e: Exception) {
                _message.value = e.message
            }
        }
    }

    fun activarReserva(id: String) {
        viewModelScope.launch {
            try {
                reservasRepository.activarReserva(id)
                _reservasConUsuario.value = _reservasConUsuario.value.filter { it.id != id }
                _message.value = "Reserva activada"
            } catch (e: Exception) {
                _message.value = e.message
            }
        }
    }

    // ---------------------------------------------------------
    // ACTIVIDAD RECIENTE
    // ---------------------------------------------------------
    fun loadActividad(garageId: String) {
        viewModelScope.launch {
            try {
                _actividad.value = repository.getActividadReciente(garageId)
            } catch (e: Exception) {
                _message.value = e.message
            }
        }
    }

    // ---------------------------------------------------------
    // PDF
    // ---------------------------------------------------------
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun generarPdf(ctx: Context, ticket: ParkingTicket): File =
        PdfGenerator.generateFactura(ctx, ticket)
}
