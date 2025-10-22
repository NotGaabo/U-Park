package com.kotlin.u_park.data.repository

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kotlin.u_park.domain.model.User
import com.kotlin.u_park.data.remote.SessionManager
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    val authState: StateFlow<AuthState> = authRepository.authState
    val currentUser: StateFlow<User?> = authRepository.currentUser

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            authRepository.signIn(email, password, sessionManager)
        }
    }

    fun signUp(user: User, sessionManager: SessionManager) {
        viewModelScope.launch {
            authRepository.signUp(user, sessionManager)
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut(sessionManager)
        }
    }
}
