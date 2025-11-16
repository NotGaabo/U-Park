package com.kotlin.u_park.presentation.screens.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kotlin.u_park.data.remote.SessionManager
import com.kotlin.u_park.domain.model.User
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    application: Application,
    private val supabase: SupabaseClient
) : AndroidViewModel(application) {

    private val sessionManager = SessionManager.getInstance(application, supabase)

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    private val _activeRole = MutableStateFlow<String?>(null)
    val activeRole: StateFlow<String?> = _activeRole

    init {
        loadUserData()
        loadActiveRole()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            _currentUser.value = sessionManager.getUser()
        }
    }

    private fun loadActiveRole() {
        viewModelScope.launch {
            _activeRole.value = sessionManager.getActiveRole()
        }
    }

    fun signOut(onDone: () -> Unit) {
        viewModelScope.launch {
            sessionManager.clearSession()
            onDone()
        }
    }

    fun getSessionManager(): SessionManager = sessionManager
}
