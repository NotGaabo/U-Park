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
import com.kotlin.u_park.domain.model.SalidaResponse
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

    fun actualizarVehiculosDentro() {
        viewModelScope.launch {
            val dentro = repository.getVehiculosDentro()
            _vehiculosDentro.value = dentro.mapNotNull { it.vehicles?.plate }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun crearReserva(
        garageId: String,
        vehicleId: String,   // este SÍ es UUID
        fecha: String,
        userId: String
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                val reserva = Reserva(
                    garage_id = garageId,
                    vehicle_id = vehicleId,   // UUID
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
    @RequiresApi(Build.VERSION_CODES.O)
    fun registrarEntrada(
        garageId: String,
        vehiclePlate: String,
        empleadoId: String,
        fotosBytes: List<ByteArray>
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                println("🚗 registrando entrada, placa=$vehiclePlate")

                // 1️⃣ Buscar UUID por placa
                val vehicleUuid = repository.getVehicleIdByPlate(vehiclePlate)
                if (vehicleUuid == null) {
                    _message.value = "No existe un vehículo con esa placa"
                    return@launch
                }

                // 2️⃣ Validar si ya está dentro
                if (repository.estaVehiculoDentro(vehicleUuid)) {
                    _message.value = "El vehículo ya está dentro"
                    return@launch
                }

                // 3️⃣ Construir Parking
                val hora = OffsetDateTime.now().toString()
                val parking = Parking(
                    id = null,
                    garage_id = garageId,
                    vehicle_id = vehicleUuid,  // ← UUID correcto
                    created_by_user_id = empleadoId,
                    hora_entrada = hora,
                    tipo = "entrada",
                    estado = "activa",
                    fotos = emptyList()
                )

                // 4️⃣ Registrar en Supabase
                val created = repository.registrarEntrada(parking, fotosBytes)

                // 5️⃣ Ticket
                _ticket.value = ParkingTicket(
                    plate = vehiclePlate,
                    horaEntrada = created.hora_entrada,
                    fotos = created.fotos,
                    garage = created.garage_id ?: "",
                    parkingId = created.id ?: ""
                )

                _message.value = "Entrada registrada correctamente"

            } catch (e: Exception) {
                _message.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetTicket() {
        _ticket.value = null
    }


    // ---------------------------------------------------------
    fun loadReservasConUsuario(garageId: String) {
        viewModelScope.launch {
            try {
                actualizarVehiculosDentro()
                val lista = repository.getReservasConUsuario(garageId)

                _reservasConUsuario.value = lista.filter { r ->
                    r.estado == "pendiente"     // SOLO reservas pendientes
                }

            } catch (e: Exception) {
                _message.value = e.message
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun registrarEntradaDesdeReserva(
        reserva: ReservaConUsuario,
        fotosBytes: List<ByteArray>,
        empleadoId: String
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                // 🔥 UUID REAL DEL VEHÍCULO (NO LA PLACA)
                val vehicleUuid = reserva.vehicle_id ?: run {
                    _message.value = "La reserva no tiene un vehículo válido"
                    return@launch
                }

                // ✔ Verificar duplicados con UUID
                if (repository.estaVehiculoDentro(vehicleUuid)) {
                    _message.value = "Este vehículo ya está dentro."
                    return@launch
                }

                // Registrar entrada
                val created = repository.registrarEntradaDesdeReserva(
                    reserva,
                    fotosBytes,
                    empleadoId
                )

                // Actualizar empleado
                reservasRepository.actualizarEmpleadoReserva(reserva.id!!, empleadoId)

                // Consumir la reserva
                reservasRepository.cancelarReserva(reserva.id!!)

                // Ticket final
                _ticket.value = ParkingTicket(
                    plate = reserva.vehicles?.plate ?: "",
                    horaEntrada = created.hora_entrada,
                    fotos = created.fotos,
                    garage = created.garage_id ?: "",
                    parkingId = created.id ?: ""
                )

                _message.value = "Entrada registrada desde reserva"

                // Recargar lista
                loadReservasConUsuario(created.garage_id!!)

            } catch (e: Exception) {
                _message.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    // -------------------
    @RequiresApi(Build.VERSION_CODES.O)
    fun registrarSalida(parkingId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                println("🚪 registrarSalida -> $parkingId")

                val hora = OffsetDateTime.now().toString()
                repository.registrarSalida(parkingId, hora)

                actualizarVehiculosDentro()
                _message.value = "Salida registrada correctamente"

            } catch (e: Exception) {
                println("🔥 Error salida: ${e.message}")
                _message.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

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
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun generarPdf(ctx: Context, salida: SalidaResponse, vehiculoNombre: String, garageNombre: String): File {
        return PdfGenerator.generateFacturaSalida(
            context = ctx,
            ticket = salida,
            vehiculoNombre = vehiculoNombre,
            garageNombre = garageNombre
        )
    }
}
