package com.kotlin.u_park.presentation.screens.rates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kotlin.u_park.domain.repository.RatesRepository

class RatesViewModelFactory(
    private val repo: RatesRepository
) : ViewModelProvider.Factory {
    override fun <T: ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RatesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RatesViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
