package com.kotlin.u_park.presentation.screens.employee

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kotlin.u_park.domain.repository.EmpleadoGarageRepository

class EmpleadosViewModelFactory(
    private val repository: EmpleadoGarageRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EmpleadosViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EmpleadosViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}
