package com.kotlin.u_park.presentation.screens.employee

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kotlin.u_park.domain.model.EmpleadoGarage
import com.kotlin.u_park.domain.model.ParkingActividad
import com.kotlin.u_park.domain.model.Stats
import com.kotlin.u_park.domain.repository.EmpleadoGarageRepository
import com.kotlin.u_park.domain.repository.ParkingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EmpleadosViewModel(
    private val empleadoRepo: EmpleadoGarageRepository,
    private val parkingRepo: ParkingRepository     // 🔥 SE AGREGA EL REPO DE PARKING
) : ViewModel() {

    private val _stats = MutableStateFlow<Stats?>(null)
    val stats: StateFlow<Stats?> = _stats

    private val _empleados = MutableStateFlow<List<EmpleadoGarage>>(emptyList())
    val empleados: StateFlow<List<EmpleadoGarage>> = _empleados

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isSuccess = MutableStateFlow<Boolean?>(null)
    val isSuccess: StateFlow<Boolean?> = _isSuccess


    /* ---------------------------------------------------------
       🔥 BLOQUE AÑADIDO: ACTIVIDAD RECIENTE DEL PARKING
    --------------------------------------------------------- */

    private val _actividad = MutableStateFlow<List<ParkingActividad>>(emptyList())
    val actividad = _actividad.asStateFlow()

    fun loadActividad(garageId: String) {
        viewModelScope.launch {
            try {
                _actividad.value = parkingRepo.getActividadReciente(garageId)
            } catch (e: Exception) {
                _actividad.value = emptyList()
            }
        }
    }

    /* ---------------------------------------------------------
       ESTADÍSTICAS
    --------------------------------------------------------- */

    fun loadStats(garageId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                _stats.value = empleadoRepo.getStats(garageId)
            } catch (e: Exception) {
                _stats.value = Stats()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /* ---------------------------------------------------------
       EMPLEADOS
    --------------------------------------------------------- */

    fun loadEmpleados(garageId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                _empleados.value = empleadoRepo.getEmpleadosByGarage(garageId)
            } catch (e: Exception) {
                _empleados.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addEmpleado(garageId: String, cedula: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val ok = empleadoRepo.addEmpleadoToGarage(garageId, cedula)

                _isSuccess.value = ok
                if (ok) loadEmpleados(garageId)

            } catch (e: Exception) {
                _isSuccess.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun removeEmpleado(garageId: String, cedula: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val ok = empleadoRepo.removeEmpleadoFromGarage(garageId, cedula)
                if (ok) loadEmpleados(garageId)

            } catch (e: Exception) {
            } finally {
                _isLoading.value = false
            }
        }
    }
}
