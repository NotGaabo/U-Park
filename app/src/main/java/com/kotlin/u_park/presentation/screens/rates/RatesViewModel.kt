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
    fun loadRates(garageId: String) {
        viewModelScope.launch {
            try {
                loading.value = true
                rates.value = repo.getRatesByGarage(garageId)
            } catch (e: Exception) {
                errorMessage.value = e.message
            } finally {
                loading.value = false
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
    fun loadGarages() {
        viewModelScope.launch {
            try {
                garages.value = repo.getGarages()
            } catch(e: Exception) {
                errorMessage.value = e.message
            }
        }
    }

    // ----------------------------------------------------------
    fun setEditing(rate: Rate?) {
        editingRate.value = rate
    }

    // ----------------------------------------------------------
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

                val result = if (editing == null) {
                    repo.createRate(rate)
                } else {
                    repo.updateRate(editing.id!!, rate)
                }

                // actualizar lista local
                val list = rates.value.toMutableList()
                val idx = list.indexOfFirst { it.id == result.id }

                if (idx >= 0) list[idx] = result
                else list.add(result)

                rates.value = list
                editingRate.value = null

            } catch (e: Exception) {
                errorMessage.value = e.message
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

                // Calcular salida mediante RPC
                val salida = repo.calcularSalida(parkingId)
                salidaState.value = salida

                // Cargar nombres
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
                rates.value = rates.value.filterNot { it.id == id }
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
                rates.value = rates.value.map {
                    if (it.id == updated.id) updated else it
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