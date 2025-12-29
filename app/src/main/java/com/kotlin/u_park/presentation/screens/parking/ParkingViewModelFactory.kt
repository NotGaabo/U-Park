package com.kotlin.u_park.presentation.screens.parking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kotlin.u_park.data.remote.SessionManager
import com.kotlin.u_park.domain.repository.ParkingRepository
import com.kotlin.u_park.domain.repository.ReservasRepository

class ParkingViewModelFactory(
    private val parkingRepository: ParkingRepository,
    private val reservasRepository: ReservasRepository,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ParkingViewModel::class.java)) {
            return ParkingViewModel(parkingRepository, reservasRepository, sessionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
