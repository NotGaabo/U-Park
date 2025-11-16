package com.kotlin.u_park.presentation.screens.employee

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kotlin.u_park.domain.model.EmpleadoGarage
import com.kotlin.u_park.domain.repository.EmpleadoGarageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class Stats(
    val autosActivos: Int = 0,
    val espaciosLibres: Int = 0,
    val entradasHoy: Int = 0,
    val salidasHoy: Int = 0
)

class EmpleadosViewModel(
    private val repository: EmpleadoGarageRepository
) : ViewModel() {

    private val _stats = MutableStateFlow<Stats?>(null)
    val stats: StateFlow<Stats?> = _stats

    private val _empleados = MutableStateFlow<List<EmpleadoGarage>>(emptyList())
    val empleados: StateFlow<List<EmpleadoGarage>> = _empleados

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isSuccess = MutableStateFlow<Boolean?>(null)
    val isSuccess: StateFlow<Boolean?> = _isSuccess


    fun loadStats(garageId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val statsResult = repository.getStats(garageId)
                _stats.value = statsResult
            } catch (e: Exception) {
                _stats.value = Stats()
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 🔹 Cargar empleados
    fun loadEmpleados(garageId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val list = repository.getEmpleadosByGarage(garageId)
                _empleados.value = list
            } catch (e: Exception) {
                _empleados.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 🔹 Agregar empleado
    fun addEmpleado(garage_id: String, empleadoId: String) {
        viewModelScope.launch {
            android.util.Log.d("VM_ADD", "Agregando empleadoId=$empleadoId a garageId=$garage_id")

            _isLoading.value = true
            try {
                val ok = repository.addEmpleadoToGarage(garage_id, empleadoId)
                android.util.Log.d("VM_ADD", "Resultado repo=$ok")

                _isSuccess.value = ok

                if (ok) {
                    loadEmpleados(garage_id)
                }

            } catch (e: Exception) {
                android.util.Log.e("VM_ADD", "ERROR → ${e.message}")
                _isSuccess.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 🔹 Eliminar empleado
    fun removeEmpleado(garage_id: String, empleadoId: String) {
        viewModelScope.launch {
            android.util.Log.d("VM_DELETE", "Eliminar empleadoId=$empleadoId de garageId=$garage_id")

            _isLoading.value = true
            try {
                val ok = repository.removeEmpleadoFromGarage(garage_id, empleadoId)
                android.util.Log.d("VM_DELETE", "Resultado repo=$ok")

                if (ok) {
                    loadEmpleados(garage_id)
                }

            } catch (e: Exception) {
                android.util.Log.e("VM_DELETE", "ERROR → ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
}
