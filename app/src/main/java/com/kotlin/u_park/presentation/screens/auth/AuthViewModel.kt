package com.kotlin.u_park.presentation.screens.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kotlin.u_park.data.remote.SessionManager
import com.kotlin.u_park.data.repository.AuthRepositoryImpl
import com.kotlin.u_park.domain.model.User
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepositoryImpl,
    private val sessionManager: SessionManager,
    private val appContext: Context
) : ViewModel() {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()


    init {
        viewModelScope.launch {
            sessionManager.getUserFlow().collect {
                _currentUser.value = it
            }
        }
    }


    // 🔐 LOGIN
    fun signIn(email: String, password: String) {
        _loading.value = true
        viewModelScope.launch {
            val result = authRepository.signIn(email, password, sessionManager)

            result.onSuccess { user ->
                _currentUser.value = user
            }

            result.onFailure {
                _error.value = it.message
            }

            _loading.value = false
        }
    }


    // 📝 SIGN UP
    fun signUp(user: User) {
        _loading.value = true
        viewModelScope.launch {
            val result = authRepository.signUp(user, sessionManager)

            result.onSuccess { newUser ->
                _currentUser.value = newUser
            }

            result.onFailure {
                _error.value = it.message
            }

            _loading.value = false
        }
    }
    private val _userLocation = MutableStateFlow<Pair<Double, Double>?>(null)
    val userLocation: StateFlow<Pair<Double, Double>?> = _userLocation.asStateFlow()

    fun updateLocation(lat: Double, lng: Double) {
        _userLocation.value = lat to lng
    }


    // 🚪 LOGOUT
    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut(sessionManager)
            _currentUser.value = null
        }
    }
}
