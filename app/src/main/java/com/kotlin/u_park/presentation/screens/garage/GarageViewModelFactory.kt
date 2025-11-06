package com.kotlin.u_park.presentation.screens.garage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kotlin.u_park.data.repository.GarageRepositoryImpl

class GarageViewModelFactory(
    private val repository: GarageRepositoryImpl
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GarageViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GarageViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}