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
import io.github.jan.supabase.postgrest.query.Columns
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

    // Restaurar usuario con roles reales desde la BD
    suspend fun restoreCurrentUser(sessionManager: SessionManager) {
        try {
            val restored = sessionManager.refreshSessionFromDataStore()
            if (!restored) {
                _currentUser.value = null
                return
            }

            val userAuth = supabase.auth.currentUserOrNull()
            if (userAuth != null) {
                val usersList: List<UserDto> = supabase.from("users").select().decodeList<UserDto>()
                val userData = usersList.firstOrNull { it.id == userAuth.id }?.let { dto ->
                    val roles = getUserRoles(dto.id)
                    User(
                        id = dto.id,
                        nombre = dto.nombre,
                        usuario = dto.usuario,
                        cedula = dto.cedula,
                        telefono = dto.telefono,
                        correo = dto.correo,
                        contrasena = "",
                        roles = roles
                    )
                }
                _currentUser.value = userData
            } else {
                _currentUser.value = null
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "No se pudo restaurar usuario: ${e.message}")
            _currentUser.value = null
        }
    }

    // Registro con asignación automática de rol "user"
    suspend fun signUp(user: User, sessionManager: SessionManager) {
        _authState.value = AuthState.Loading
        try {
            supabase.auth.signUpWith(Email) {
                email = user.correo
                password = user.contrasena
            }

            val userAuth = supabase.auth.currentUserOrNull()
            val userId = userAuth?.id
            if (userId.isNullOrBlank()) {
                _authState.value = AuthState.Error("No se pudo crear el usuario")
                return
            }

            val userInsert = UserInsertDTO(
                id = userId,
                nombre = user.nombre,
                usuario = user.usuario,
                cedula = user.cedula,
                telefono = user.telefono,
                correo = user.correo
            )
            supabase.from("users").insert(userInsert)

            val rolesList = supabase.from("roles")
                .select(columns = Columns.list("id", "nombre"))
                .decodeList<RoleRow>()

            val roleUser = rolesList.firstOrNull { it.nombre == "user" }

            if (roleUser != null) {
                val userRoleInsert = UserRoleInsertDTO(
                    user_id = userId,
                    role_id = roleUser.id
                )
                supabase.from("user_roles").insert(userRoleInsert)
                Log.d("AuthRepository", "✅ Rol 'user' asignado correctamente a $userId")
            } else {
                Log.e("AuthRepository", "⚠️ No se encontró el rol 'user'")
            }

            sessionManager.saveSession()
            _currentUser.value = user.copy(id = userId, roles = listOf("user"))
            _authState.value = AuthState.Success

        } catch (e: Exception) {
            Log.e("AuthRepository", "Error en signUp: ${e.message}", e)
            _authState.value = AuthState.Error(e.message ?: "Error desconocido")
        }
    }

    // Inicio de sesión con carga de roles reales
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
                    val roles = getUserRoles(dto.id)
                    User(
                        id = dto.id,
                        nombre = dto.nombre,
                        usuario = dto.usuario,
                        cedula = dto.cedula,
                        telefono = dto.telefono,
                        correo = dto.correo,
                        contrasena = "",
                        roles = roles
                    )
                }

                if (userData != null) {
                    _currentUser.value = userData
                    _authState.value = AuthState.Success
                    Log.d("AuthRepository", "=== Inicio de sesión exitoso === Roles: ${userData.roles}")
                } else {
                    _authState.value = AuthState.Error("Usuario no encontrado")
                }

            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Error desconocido")
                Log.e("AuthRepository", "Error en signIn", e)
            }
        }
    }

    // Cerrar sesión
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

    // Obtener roles reales del usuario desde la BD
    suspend fun getUserRoles(userId: String): List<String> {
        return try {
            val result = supabase.from("user_roles")
                .select(
                    Columns.raw("""
                        role_id,
                        roles!fk_user_roles_role(id,nombre)
                    """.trimIndent())
                ) {
                    filter { eq("user_id", userId) }
                }
                .decodeList<UserRoleWithName>()

            val roles = result.map { it.roles.nombre }
            Log.d("AuthRepository", "✅ Roles obtenidos para $userId: $roles")
            roles
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error al obtener roles del usuario: ${e.message}")
            emptyList()
        }
    }
}

// DTOs para inserts
@Serializable
data class UserInsertDTO(
    val id: String,
    val nombre: String,
    val usuario: String,
    val cedula: String,
    val telefono: String,
    val correo: String
)

@Serializable
data class UserRoleInsertDTO(
    val user_id: String,
    val role_id: Int
)

// DTOs para lectura
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

@Serializable
data class RoleRow(
    val id: Int,
    val nombre: String
)

@Serializable
data class UserRoleWithName(
    val role_id: Int,
    val roles: RoleRow
)

// Estados del Auth
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}
