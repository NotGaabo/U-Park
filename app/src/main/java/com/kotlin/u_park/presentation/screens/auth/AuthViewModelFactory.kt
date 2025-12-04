package com.kotlin.u_park.presentation.screens.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kotlin.u_park.data.remote.SessionManager
import com.kotlin.u_park.data.repository.AuthRepositoryImpl
import com.kotlin.u_park.domain.repository.AuthRepository

class AuthViewModelFactory(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager,
    private val appContext: Context
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(
                authRepository = authRepository as AuthRepositoryImpl,
                sessionManager = sessionManager,
                appContext = appContext
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
