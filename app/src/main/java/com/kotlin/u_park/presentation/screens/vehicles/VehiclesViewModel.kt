package com.kotlin.u_park.presentation.screens.vehicles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kotlin.u_park.data.repository.VehiclesRepositoryImpl
import com.kotlin.u_park.domain.model.Vehicle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import com.kotlin.u_park.domain.model.VehicleTypeSimple


class VehiclesViewModel(
    private val repo: VehiclesRepositoryImpl
) : ViewModel() {

    private val _vehicles = MutableStateFlow<List<Vehicle>>(emptyList())
    val vehicles: StateFlow<List<Vehicle>> = _vehicles

    private val _vehicleTypes = MutableStateFlow<List<VehicleTypeSimple>>(emptyList())
    val vehicleTypes: StateFlow<List<VehicleTypeSimple>> = _vehicleTypes


    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess: StateFlow<Boolean> = _isSuccess

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun loadVehicleTypes() {
        viewModelScope.launch {
            try {
                val result = repo.getVehicleTypes()
                _vehicleTypes.value = result
            } catch (e: Exception) {
                _vehicleTypes.value = emptyList()
                _errorMessage.value = "Error al cargar tipos de vehículo"
            }
        }
    }


    fun loadVehicles(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repo.getVehiclesByUser(userId)
                _vehicles.value = result
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar vehículos: ${e.message}"
                _vehicles.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addVehicle(vehicle: Vehicle, imageFile: File? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _isSuccess.value = false
            try {
                val success = repo.addVehicle(vehicle)
                if (success) {
                    _isSuccess.value = true
                    _errorMessage.value = null
                    // Recargar la lista de vehículos
                    vehicle.user_id?.let { loadVehicles(it) }
                } else {
                    _errorMessage.value = "Error al agregar el vehículo"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteVehicle(vehicleId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = repo.deleteVehicle(vehicleId)
                if (success) {
                    // Remover el vehículo de la lista local
                    _vehicles.value = _vehicles.value.filter { it.id != vehicleId }
                    _errorMessage.value = null
                } else {
                    _errorMessage.value = "Error al eliminar el vehículo"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetStatus() {
        _isSuccess.value = false
        _errorMessage.value = null
    }
}