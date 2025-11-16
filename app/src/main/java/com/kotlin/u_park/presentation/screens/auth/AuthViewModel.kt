package com.kotlin.u_park.presentation.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kotlin.u_park.data.remote.SessionManager
import com.kotlin.u_park.domain.model.User
import com.kotlin.u_park.domain.repository.AuthRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _userLocation = MutableStateFlow<Pair<Double, Double>?>(null)
    val userLocation: StateFlow<Pair<Double, Double>?> = _userLocation.asStateFlow()

    init {
        // 🔄 Escucha DataStore en tiempo real
        viewModelScope.launch {
            sessionManager.getUserFlow().collect {
                _currentUser.value = it
            }
        }
    }

    fun updateLocation(lat: Double, lng: Double) {
        _userLocation.value = lat to lng
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
}
