package com.kotlin.u_park.presentation.screens.parking

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kotlin.u_park.domain.model.HistorialParking
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
import com.kotlin.u_park.data.remote.SessionManager
import com.kotlin.u_park.data.remote.supabase
import com.kotlin.u_park.domain.model.Rate
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import java.io.File
import java.time.OffsetDateTime

class ParkingViewModel(
    private val repository: ParkingRepository,
    private val reservasRepository: ReservasRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _ticket = MutableStateFlow<ParkingTicket?>(null)
    val ticket = _ticket.asStateFlow()

    private val _rates = MutableStateFlow<List<Rate>>(emptyList())
    val rates = _rates.asStateFlow()

    private val _selectedRate = MutableStateFlow<Rate?>(null)
    val selectedRate = _selectedRate.asStateFlow()

    fun loadRatesByGarage(garageId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                val result = supabase.postgrest
                    .rpc(
                        "get_active_rates_by_garage",
                        mapOf("p_garage_id" to garageId)
                    )
                    .decodeList<Rate>()

                _rates.value = result

            } catch (e: Exception) {
                _message.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }



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

    private val _historial = MutableStateFlow<List<HistorialParking>>(emptyList())
    val historial = _historial.asStateFlow()

    private val _parkingActivo = MutableStateFlow<HistorialParking?>(null)
    val parkingActivo = _parkingActivo.asStateFlow()

    fun cargarHistorial(userId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                val lista = repository.getHistorialByUser(userId)

                _historial.value = lista

                _parkingActivo.value = lista.firstOrNull { it.estado == "activa" }

            } catch (e: Exception) {
                _message.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }


    fun actualizarVehiculosDentro() {
        viewModelScope.launch {
            try {
                val dentro = repository.getVehiculosDentro()
                _vehiculosDentro.value = dentro.mapNotNull { it.vehicles?.plate }
                println("🔄 Vehículos dentro actualizados: ${_vehiculosDentro.value.size}")
            } catch (e: Exception) {
                println("🔥 Error actualizando vehículos: ${e.message}")
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun registrarSalida(parkingId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                val hora = OffsetDateTime.now().toString()
                val empleadoId = sessionManager.getUserId()!!

                println("📤 Registrando salida → parkingId=$parkingId, empleadoId=$empleadoId")

                repository.registrarSalida(
                    parkingId = parkingId,
                    horaSalida = hora,
                    empleadoId = empleadoId
                )

                actualizarVehiculosDentro()

                _message.value = "Salida registrada correctamente"

            } catch (e: Exception) {
                _message.value = e.message
            }
             finally {
                _isLoading.value = false
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


    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun generarPdf(ctx: Context, salida: SalidaResponse, vehiculoNombre: String, garageNombre: String): File {
        return PdfGenerator.generateFacturaSalida(
            context = ctx,
            ticket = salida,
            vehiculoNombre = vehiculoNombre,
            garageNombre = garageNombre
        )
    }

    fun selectRate(rate: Rate) {
        _selectedRate.value = rate
    }

    // ------------------------------------------------------------
    // 🔥 REGISTRAR SALIDA CON MÚLTIPLES FOTOS Y PAGO
    // ------------------------------------------------------------
    @RequiresApi(Build.VERSION_CODES.O)
    fun registrarSalidaConPago(
        parkingId: String,
        metodoPago: String,
        fotosSalidaBytes: List<ByteArray>,      // 🔥 Múltiples fotos del vehículo
        comprobanteBytes: ByteArray?            // 🔥 Una foto del comprobante
    ) {
        viewModelScope.launch {
            try {
                if (metodoPago == "TRANSFERENCIA" && comprobanteBytes == null) {
                    _message.value = "Debe adjuntar comprobante de transferencia"
                    return@launch
                }

                if (fotosSalidaBytes.isEmpty()) {
                    _message.value = "Debe tomar al menos una foto del vehículo"
                    return@launch
                }

                _isLoading.value = true

                val horaSalida = OffsetDateTime.now().toString()
                val empleadoId = sessionManager.getUserId()!!

                println("🔥 CONFIRMANDO SALIDA")
                println("parkingId=$parkingId | empleado=$empleadoId")
                println("Fotos salida: ${fotosSalidaBytes.size}")
                println("Comprobante: ${if (comprobanteBytes != null) "Sí" else "No"}")

                repository.registrarSalidaConPago(
                    parkingId = parkingId,
                    horaSalida = horaSalida,
                    empleadoId = empleadoId,
                    metodoPago = metodoPago,
                    fotosSalidaBytes = fotosSalidaBytes,
                    comprobanteBytes = comprobanteBytes
                )

                println("📡 Enviando a RPC registrar_salida_con_pago")
                println("parkingId=$parkingId")
                println("horaSalida=$horaSalida")
                println("empleado=$empleadoId")
                println("metodo=$metodoPago")
                println("comprobante adjunto = ${comprobanteBytes != null}")

                actualizarVehiculosDentro()
                _message.value = "Salida registrada y pagada correctamente ✅"

            } catch (e: Exception) {
                _message.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }



    // ------------------------------------------------------------
    // 🔥 REGISTRAR ENTRADA CON MÚLTIPLES FOTOS
    // ------------------------------------------------------------
    @RequiresApi(Build.VERSION_CODES.O)
    fun registrarEntrada(
        garageId: String,
        vehiclePlate: String,
        empleadoId: String,
        rateId: String,
        fotosBytes: List<ByteArray>  // 🔥 Lista de fotos
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                println("🚗 Registrando entrada, placa=$vehiclePlate")
                println("Fotos: ${fotosBytes.size}")

                val vehicleUuid = repository.getVehicleIdByPlate(vehiclePlate)
                if (vehicleUuid == null) {
                    _message.value = "No existe un vehículo con esa placa"
                    return@launch
                }

                if (repository.estaVehiculoDentro(vehicleUuid)) {
                    _message.value = "El vehículo ya está dentro"
                    return@launch
                }

                val hora = OffsetDateTime.now().toString()
                val parking = Parking(
                    id = null,
                    garage_id = garageId,
                    vehicle_id = vehicleUuid,
                    rate_id = rateId,
                    created_by_user_id = empleadoId,
                    hora_entrada = hora,
                    tipo = "entrada",
                    estado = "activa",
                    fotos = emptyList(),
                    fotos_entrada = emptyList()
                )

                val created = repository.registrarEntrada(parking, fotosBytes)

                _ticket.value = ParkingTicket(
                    plate = vehiclePlate,
                    horaEntrada = created.hora_entrada,
                    fotos = created.fotos_entrada,  // 🔥 Usar fotos_entrada
                    garage = created.garage_id ?: "",
                    parkingId = created.id ?: ""
                )

                _message.value = "Entrada registrada correctamente"

                actualizarVehiculosDentro()
                loadActividad(garageId)

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

    fun loadReservasConUsuario(garageId: String) {
        viewModelScope.launch {
            try {
                actualizarVehiculosDentro()
                val lista = repository.getReservasConUsuario(garageId)
                _reservasConUsuario.value = lista.filter { r ->
                    r.estado == "pendiente"
                }
            } catch (e: Exception) {
                _message.value = e.message
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun registrarEntradaDesdeReserva(
        reserva: ReservaConUsuario,
        fotosBytes: List<ByteArray>,  // 🔥 Múltiples fotos
        empleadoId: String
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                val vehicleUuid = reserva.vehicle_id ?: run {
                    _message.value = "La reserva no tiene un vehículo válido"
                    return@launch
                }

                if (repository.estaVehiculoDentro(vehicleUuid)) {
                    _message.value = "Este vehículo ya está dentro."
                    return@launch
                }

                val created = repository.registrarEntradaDesdeReserva(
                    reserva,
                    fotosBytes,
                    empleadoId
                )

                reservasRepository.actualizarEmpleadoReserva(reserva.id!!, empleadoId)
                reservasRepository.cancelarReserva(reserva.id!!)

                _ticket.value = ParkingTicket(
                    plate = reserva.vehicles?.plate ?: "",
                    horaEntrada = created.hora_entrada,
                    fotos = created.fotos_entrada,  // 🔥 Usar fotos_entrada
                    garage = created.garage_id ?: "",
                    parkingId = created.id ?: ""
                )

                _message.value = "Entrada registrada desde reserva"
                loadReservasConUsuario(created.garage_id!!)

            } catch (e: Exception) {
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

    fun loadActividad(garageId: String) {
        viewModelScope.launch {
            try {
                val actividadLista = repository.getActividadReciente(garageId)
                _actividad.value = actividadLista
                println("🔄 Actividad recargada: ${actividadLista.size} registros")
            } catch (e: Exception) {
                _message.value = e.message
                println("🔥 Error cargando actividad: ${e.message}")
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}