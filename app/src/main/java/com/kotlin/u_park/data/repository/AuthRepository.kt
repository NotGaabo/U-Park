package com.kotlin.u_park.data.repository

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kotlin.u_park.domain.model.User
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import android.util.Log

class AuthRepository(private val supabase: SupabaseClient) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    fun signUp(user: User) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading

                val result = supabase.auth.signUpWith(Email) {
                    email = user.correo
                    password = user.contrasena
                    data = buildJsonObject {
                        put("usuario", user.usuario)
                    }
                }

                val authUser = supabase.auth.currentUserOrNull()
                if (authUser == null) {
                    _authState.value = AuthState.Error("Fallo creando usuario en auth")
                    return@launch
                }

                supabase.from("users").insert(
                    mapOf(
                        "id" to authUser.id,
                        "nombre" to user.nombre,
                        "usuario" to user.usuario,
                        "cedula" to user.cedula,
                        "telefono" to user.telefono,
                        "correo" to user.correo,
                        "rol" to user.rol,
                        "direccion" to user.direccion
                    )
                )

                _currentUser.value = user
                _authState.value = AuthState.Success

            } catch (e: Exception) {
                Log.e("AuthRepository", "Error en signUp: ${e.message}")
                _authState.value = AuthState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading

                supabase.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }

                val authUser = supabase.auth.currentUserOrNull()
                if (authUser == null) {
                    _authState.value = AuthState.Error("Usuario no encontrado")
                    return@launch
                }

                val user = supabase.from("users")
                    .select(columns = Columns.list("*")) {
                        filter { eq("id", authUser.id) }
                    }
                    .decodeSingle<User>()

                _currentUser.value = user
                _authState.value = AuthState.Success

            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Error iniciando sesión")
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                supabase.auth.signOut()
                _currentUser.value = null
                _authState.value = AuthState.Idle
            } catch (e: Exception) {
                Log.e("AuthRepository", "Error en signOut: ${e.message}")
            }
        }
    }
}

// -------------------------
// AUTH STATE
// -------------------------
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}
