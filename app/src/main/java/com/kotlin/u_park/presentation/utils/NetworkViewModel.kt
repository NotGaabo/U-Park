package com.kotlin.u_park.presentation.utils


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData

class NetworkViewModel(application: Application) : AndroidViewModel(application) {
    private val networkMonitor = NetworkMonitor(application)
    val isConnected: LiveData<Boolean> = networkMonitor.isConnected
}
