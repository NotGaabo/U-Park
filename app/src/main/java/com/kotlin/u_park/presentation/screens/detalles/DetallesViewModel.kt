package com.kotlin.u_park.presentation.screens.detalles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kotlin.u_park.domain.model.Garage
import com.kotlin.u_park.domain.repository.GarageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DetallesViewModel(
    private val repo: GarageRepository
) : ViewModel() {

    private val _garage = MutableStateFlow<Garage?>(null)
    val garage: StateFlow<Garage?> = _garage

    fun loadGarage(garageId: String) {
        viewModelScope.launch {
            _garage.value = repo.getGarageById(garageId)
        }
    }
}
