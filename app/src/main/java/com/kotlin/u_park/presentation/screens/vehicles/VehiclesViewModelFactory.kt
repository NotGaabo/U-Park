package com.kotlin.u_park.presentation.screens.vehicles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kotlin.u_park.data.repository.VehiclesRepositoryImpl

class VehiclesViewModelFactory(
    private val repo: VehiclesRepositoryImpl
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return VehiclesViewModel(repo) as T
    }
}
