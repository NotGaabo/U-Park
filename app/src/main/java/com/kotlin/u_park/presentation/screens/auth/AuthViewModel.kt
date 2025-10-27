package com.kotlin.u_park.presentation.screens.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kotlin.u_park.data.remote.SessionManager
import com.kotlin.u_park.domain.repository.AuthRepository
import com.kotlin.u_park.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    // Exponemos ubicación como StateFlow
    private val _userLocation = MutableStateFlow<Pair<Double, Double>?>(null)
    val userLocation: StateFlow<Pair<Double, Double>?> = _userLocation

    fun updateLocation(lat: Double, lng: Double) {
        _userLocation.value = Pair(lat, lng)
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            val result = authRepository.signIn(email, password, sessionManager)
            result.onSuccess { _currentUser.value = it }
        }
    }

    fun signUp(user: User) {
        viewModelScope.launch {
            val result = authRepository.signUp(user, sessionManager)
            result.onSuccess { _currentUser.value = it }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut(sessionManager)
            _currentUser.value = null
        }
    }

    fun restoreCurrentUser() {
        viewModelScope.launch {
            _currentUser.value = authRepository.restoreCurrentUser(sessionManager)
        }
    }
}
