package com.kotlin.u_park.presentation.screens.garage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kotlin.u_park.domain.model.Garage
import com.kotlin.u_park.domain.repository.GarageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

class GarageViewModel(
    private val repository: GarageRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isSuccess = MutableStateFlow<Boolean?>(null)
    val isSuccess: StateFlow<Boolean?> = _isSuccess

    fun addGarage(garage: Garage, imageFile: File?) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repository.newGarage(garage, imageFile)
                _isSuccess.value = result
            } catch (e: Exception) {
                e.printStackTrace()
                _isSuccess.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetStatus() {
        _isSuccess.value = null
    }
}
