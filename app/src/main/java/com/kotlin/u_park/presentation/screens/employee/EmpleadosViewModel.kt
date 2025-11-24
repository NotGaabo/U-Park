package com.kotlin.u_park.presentation.screens.employee

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kotlin.u_park.domain.model.EmpleadoGarage
import com.kotlin.u_park.domain.model.Stats
import com.kotlin.u_park.domain.repository.EmpleadoGarageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

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



    // 🔹 Cargar estadísticas
    fun loadStats(garageId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                _stats.value = repository.getStats(garageId)
            } catch (e: Exception) {
                _stats.value = Stats()
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 🔹 Cargar empleados de un garage
    fun loadEmpleados(garageId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                _empleados.value = repository.getEmpleadosByGarage(garageId)
            } catch (e: Exception) {
                _empleados.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }


    // 🔹 Agregar empleado por CÉDULA (Long)
    fun addEmpleado(garageId: String, cedula: Long) {
        viewModelScope.launch {
            android.util.Log.d("VM_ADD", "Agregando empleadoCedula=$cedula a garageId=$garageId")

            _isLoading.value = true
            try {
                val ok = repository.addEmpleadoToGarage(garageId, cedula)

                android.util.Log.d("VM_ADD", "Resultado repo=$ok")
                _isSuccess.value = ok

                if (ok) loadEmpleados(garageId)

            } catch (e: Exception) {
                android.util.Log.e("VM_ADD", "ERROR → ${e.message}")
                _isSuccess.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }


    // 🔹 Eliminar empleado por CÉDULA (Long)
    fun removeEmpleado(garageId: String, cedula: Long) {
        viewModelScope.launch {
            android.util.Log.d("VM_DELETE", "Eliminar empleadoCedula=$cedula de garageId=$garageId")

            _isLoading.value = true
            try {
                val ok = repository.removeEmpleadoFromGarage(garageId, cedula)

                android.util.Log.d("VM_DELETE", "Resultado repo=$ok")
                if (ok) loadEmpleados(garageId)

            } catch (e: Exception) {
                android.util.Log.e("VM_DELETE", "ERROR → ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
}
