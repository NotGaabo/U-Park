package com.kotlin.u_park.presentation.screens.rates

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kotlin.u_park.domain.model.Rate
import com.kotlin.u_park.domain.model.SalidaResponse
import com.kotlin.u_park.domain.repository.RatesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class RatesViewModel(
    private val repo: RatesRepository
) : ViewModel() {

    val rates = mutableStateOf<List<Rate>>(emptyList())
    val loading = mutableStateOf(false)
    val errorMessage = mutableStateOf<String?>(null)

    val editingRate = mutableStateOf<Rate?>(null)
    val saving = mutableStateOf(false)

    // resultado del ticket de salida
    val salidaState = mutableStateOf<SalidaResponse?>(null)

    // tipos de vehículos
    val vehicleTypes = mutableStateOf<List<Pair<Int, String>>>(emptyList())

    // garages disponibles
    val garages = mutableStateOf<List<Pair<String, String>>>(emptyList())

    // ----------------------------------------------------------
    val groupedRates = MutableStateFlow<Map<String, List<Rate>>>(emptyMap())

    // 🔥 NUEVO: guardar el userId actual (público para navegación)
    var currentUserId: String? = null
        private set

    fun loadAllRates(userId: String) {
        currentUserId = userId
        viewModelScope.launch {

            println("🚀 [VM] loadAllRates(userId=$userId)")

            try {
                loading.value = true

                groupedRates.value = repo.getAllRatesForOwner(userId)

                println("📊 [VM] groupedRates cargado con ${groupedRates.value.size} garages")

                loadGarages(userId)

            } catch (e: Exception) {
                errorMessage.value = e.message
                println("❌ [VM] Error cargando tarifas: ${e.message}")
            } finally {
                loading.value = false
            }
        }
    }

    fun loadGarages(userId: String?) {
        println("🚀 [VM] loadGarages($userId)")
        viewModelScope.launch {
            try {
                garages.value = repo.getGarages(userId)
                println("📦 [VM] garages encontrados: ${garages.value.size}")
            } catch (e: Exception) {
                errorMessage.value = e.message
                println("❌ [VM] Error cargando garages: ${e.message}")
            }
        }
    }



    // ----------------------------------------------------------
    fun loadVehicleTypes() {
        viewModelScope.launch {
            try {
                vehicleTypes.value = repo.getVehicleTypes()
            } catch(_: Exception) {}
        }
    }
    // ----------------------------------------------------------
    fun setEditing(rate: Rate?) {
        editingRate.value = rate
    }

    // ----------------------------------------------------------
    // 🔥 NUEVO: Estado para saber cuando terminó de guardar
    val saveSuccess = mutableStateOf(false)

    fun saveRate(
        garageId: String,
        baseRate: Double,
        timeUnit: String,
        vehicleTypeId: Int?,
        diasAplicables: List<String>,
        specialRate: Double?,
    ) {
        viewModelScope.launch {
            try {
                saving.value = true
                saveSuccess.value = false

                val editing = editingRate.value

                val rate = Rate(
                    id = editing?.id,
                    garageId = garageId,
                    vehicleTypeId = vehicleTypeId,
                    baseRate = baseRate,
                    timeUnit = timeUnit,
                    diasAplicables = diasAplicables,
                    specialRate = specialRate,
                    active = editing?.active ?: true
                )

                println("🔥 Guardando tarifa: $rate") // DEBUG

                val result = if (editing == null) {
                    repo.createRate(rate)
                } else {
                    repo.updateRate(editing.id!!, rate)
                }

                println("✅ Tarifa guardada: $result") // DEBUG

                // 🔥 NUEVO: Recargar groupedRates después de guardar
                currentUserId?.let { userId ->
                    groupedRates.value = repo.getAllRatesForOwner(userId)
                    println("✅ Tarifas recargadas: ${groupedRates.value}") // DEBUG
                }

                editingRate.value = null
                saveSuccess.value = true // 🔥 Marcar como exitoso

            } catch (e: Exception) {
                println("❌ Error guardando: ${e.message}") // DEBUG
                e.printStackTrace()
                errorMessage.value = e.message
                saveSuccess.value = false
            } finally {
                saving.value = false
            }
        }
    }

    // ----------------------------------------------------------
    val vehiculoNombre = MutableStateFlow<String?>(null)
    val garageNombre = MutableStateFlow<String?>(null)

    fun cargarDatosTicket(parkingId: String) {
        viewModelScope.launch {
            try {
                loading.value = true

                // 🔥 1) Real RPC calcular_salida
                val salida = repo.calcularSalida(parkingId)
                salidaState.value = salida

                // 🔥 2) Actualizar nombres
                vehiculoNombre.value = repo.getVehicleNameById(salida.vehiculo_id)
                garageNombre.value = repo.getGarageNameById(salida.garage_id)

            } catch (e: Exception) {
                errorMessage.value = e.message
            } finally {
                loading.value = false
            }
        }
    }

    // ----------------------------------------------------------
    fun deleteRate(id: String) {
        viewModelScope.launch {
            try {
                repo.deleteRate(id)

                // 🔥 NUEVO: Recargar después de eliminar
                currentUserId?.let { userId ->
                    groupedRates.value = repo.getAllRatesForOwner(userId)
                }

            } catch (e: Exception) {
                errorMessage.value = e.message
            }
        }
    }

    // ----------------------------------------------------------
    fun toggleActive(rate: Rate) {
        viewModelScope.launch {
            try {
                val updated = repo.toggleActive(rate.id!!, !rate.active)

                // 🔥 NUEVO: Recargar después de toggle
                currentUserId?.let { userId ->
                    groupedRates.value = repo.getAllRatesForOwner(userId)
                }

            } catch (e: Exception) {
                errorMessage.value = e.message
            }
        }
    }

    // ----------------------------------------------------------
    fun calcularSalida(parkingId: String) {
        viewModelScope.launch {
            try {
                loading.value = true
                salidaState.value = repo.calcularSalida(parkingId)
            } catch (e: Exception) {
                errorMessage.value = e.message
            } finally {
                loading.value = false
            }
        }
    }
}