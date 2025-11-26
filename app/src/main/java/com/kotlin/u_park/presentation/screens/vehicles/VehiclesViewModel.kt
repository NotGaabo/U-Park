package com.kotlin.u_park.presentation.screens.vehicles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kotlin.u_park.data.repository.VehiclesRepositoryImpl
import com.kotlin.u_park.domain.model.Vehicle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class VehiclesViewModel(
    private val repo: VehiclesRepositoryImpl
) : ViewModel() {

    private val _vehicles = MutableStateFlow<List<Vehicle>>(emptyList())
    val vehicles: StateFlow<List<Vehicle>> = _vehicles

    fun loadVehicles(userId: String) {
        viewModelScope.launch {
            val result = repo.getVehiclesByUser(userId)
            _vehicles.value = result
        }
    }
}

