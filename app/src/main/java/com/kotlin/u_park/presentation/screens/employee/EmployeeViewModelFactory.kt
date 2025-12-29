package com.kotlin.u_park.presentation.screens.employee

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kotlin.u_park.domain.repository.EmpleadoGarageRepository
import com.kotlin.u_park.domain.repository.ParkingRepository

class EmpleadosViewModelFactory(
    private val empleadoRepo: EmpleadoGarageRepository,
    private val parkingRepo: ParkingRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EmpleadosViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EmpleadosViewModel(
                empleadoRepo = empleadoRepo,
                parkingRepo = parkingRepo
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
