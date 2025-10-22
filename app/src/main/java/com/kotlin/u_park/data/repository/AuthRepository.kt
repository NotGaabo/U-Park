package com.kotlin.u_park.data.repository

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kotlin.u_park.domain.model.User
import com.kotlin.u_park.data.remote.SessionManager
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

class AuthRepository(private val supabase: SupabaseClient) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    suspend fun signUp(user: User, sessionManager: SessionManager) {
        _authState.value = AuthState.Loading
        try {
            supabase.auth.signUpWith(Email) {
                email = user.correo
                password = user.contrasena
            }

            val userId = supabase.auth.currentUserOrNull()?.id
            if (userId.isNullOrBlank()) {
                _authState.value = AuthState.Error("No se pudo crear el usuario")
                return
            }

            val userInsert = UserInsert(
                id = userId,
                nombre = user.nombre,
                usuario = user.usuario,
                cedula = user.cedula,
                telefono = user.telefono,
                correo = user.correo,
                roles = listOf("user")
            )

            supabase.from("users").insert(userInsert)
            sessionManager.saveSession()

            _currentUser.value = user.copy(id = userId, roles = listOf("user"))
            _authState.value = AuthState.Success
            Log.d("AuthRepository", "=== Usuario registrado correctamente ===")

        } catch (e: Exception) {
            _authState.value = AuthState.Error(e.message ?: "Error desconocido")
            Log.e("AuthRepository", "Error en signUp", e)
        }
    }

    suspend fun updateUserRoles(userId: String, newRoles: List<String>) {
        try {
            // Traemos todos los usuarios
            val usersList: List<UserDto> = supabase.from("users").select().decodeList<UserDto>()

            // Buscamos el usuario que queremos actualizar
            val userToUpdate = usersList.firstOrNull { it.id == userId } ?: return

            // Creamos un nuevo objeto con los roles actualizados
            val updatedUser = userToUpdate.copy(roles = newRoles)

            // Hacemos la actualización enviando directamente el objeto
            supabase.from("users").update(updatedUser)

            // Actualizamos el flujo interno si el usuario actual es el mismo
            if (_currentUser.value?.id == userId) {
                _currentUser.value = _currentUser.value?.copy(roles = newRoles)
            }

            Log.d("AuthRepository", "=== Roles actualizados correctamente ===")
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error al actualizar roles", e)
        }
    }

    fun signIn(correo: String, contrasena: String, sessionManager: SessionManager) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                supabase.auth.signInWith(Email) {
                    email = correo
                    password = contrasena
                }

                val userAuth = supabase.auth.currentUserOrNull()
                if (userAuth == null) {
                    _authState.value = AuthState.Error("Credenciales incorrectas")
                    return@launch
                }

                sessionManager.saveSession()

                val usersList: List<UserDto> = supabase.from("users").select().decodeList<UserDto>()
                val userData = usersList.firstOrNull { it.id == userAuth.id }?.let { dto ->
                    User(
                        id = dto.id,
                        nombre = dto.nombre,
                        usuario = dto.usuario,
                        cedula = dto.cedula,
                        telefono = dto.telefono,
                        correo = dto.correo,
                        contrasena = "",
                        roles = dto.roles
                    )
                }

                if (userData != null) {
                    _currentUser.value = userData
                    _authState.value = AuthState.Success
                    Log.d("AuthRepository", "=== Inicio de sesión exitoso ===")
                } else {
                    _authState.value = AuthState.Error("Usuario no encontrado")
                }

            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Error desconocido")
                Log.e("AuthRepository", "Error en signIn", e)
            }
        }
    }

    fun signOut(sessionManager: SessionManager? = null) {
        viewModelScope.launch {
            try {
                supabase.auth.signOut()
                sessionManager?.clearSession()
                _currentUser.value = null
                _authState.value = AuthState.Idle
            } catch (e: Exception) {
                Log.e("AuthRepository", "Error en logout: ${e.message}")
            }
        }
    }
}

@Serializable
data class UserInsert(
    val id: String,
    val nombre: String,
    val usuario: String,
    val cedula: String,
    val telefono: String,
    val correo: String,
    val roles: List<String> = listOf("user")
)

@Serializable
data class UserDto(
    val id: String,
    val nombre: String,
    val usuario: String,
    val cedula: String,
    val telefono: String,
    val correo: String,
    val roles: List<String> = listOf("user")
)

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}
