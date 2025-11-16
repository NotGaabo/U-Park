package com.kotlin.u_park.presentation.screens.garage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kotlin.u_park.domain.model.Garage
import com.kotlin.u_park.domain.repository.GarageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class GarageViewModel(
    private val repository: GarageRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess: StateFlow<Boolean> = _isSuccess

    // ✅ Lista de garajes del usuario
    private val _garages = MutableStateFlow<List<Garage>>(emptyList())
    val garages: StateFlow<List<Garage>> = _garages.asStateFlow()

    // ✅ Nuevo estado para controlar la carga inicial
    private val _hasLoadedInitially = MutableStateFlow(false)
    val hasLoadedInitially: StateFlow<Boolean> = _hasLoadedInitially.asStateFlow()

    // 🔹 Cargar garajes del usuario
    fun loadGaragesByUser(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repository.getGaragesByUserId(userId)
                _garages.value = result
                _hasLoadedInitially.value = true // ✅ Marcar como cargado
            } catch (e: Exception) {
                e.printStackTrace()
                _garages.value = emptyList()
                _hasLoadedInitially.value = true
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 🔹 Insertar nuevo garage (y recargar lista)
    fun addGarage(garage: Garage, imageFile: File?) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repository.newGarage(garage, imageFile)
                _isSuccess.value = result

                // 🔁 Si se insertó con éxito, recargar lista
                if (result && garage.userId != null) {
                    loadGaragesByUser(garage.userId)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                _isSuccess.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetStatus() {
        _isSuccess.value = false
    }
}