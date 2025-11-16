package com.kotlin.u_park.data.repository

import android.util.Log
import com.kotlin.u_park.domain.model.User
import com.kotlin.u_park.data.remote.SessionManager
import com.kotlin.u_park.domain.repository.AuthRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.Serializable

class AuthRepositoryImpl(private val supabase: SupabaseClient) : AuthRepository {

    override suspend fun restoreCurrentUser(sessionManager: SessionManager): User? {
        return try {
            val restored = sessionManager.refreshSessionFromDataStore()
            if (!restored) return null

            val userAuth = supabase.auth.currentUserOrNull()
            if (userAuth != null) {
                val usersList: List<UserDto> = supabase.from("users").select().decodeList<UserDto>()
                val dto = usersList.firstOrNull { it.id == userAuth.id }
                dto?.let {
                    val roles = getUserRoles(it.id)
                    val user = User(
                        id = it.id,
                        nombre = it.nombre,
                        usuario = it.usuario,
                        cedula = it.cedula,
                        telefono = it.telefono,
                        correo = it.correo,
                        contrasena = "",
                        roles = roles
                    )
                    sessionManager.saveUser(user)
                    user
                }
            } else null
        } catch (e: Exception) {
            Log.e("AuthRepositoryImpl", "Error restaurando usuario: ${e.message}")
            null
        }
    }

    override suspend fun signUp(user: User, sessionManager: SessionManager): Result<User> {
        return try {
            supabase.auth.signUpWith(Email) {
                email = user.correo
                password = user.contrasena
            }

            val userAuth = supabase.auth.currentUserOrNull()
            val userId = userAuth?.id ?: return Result.failure(Exception("No se pudo crear el usuario"))

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

            roleUser?.let {
                val userRoleInsert = UserRoleInsertDTO(userId, it.id)
                supabase.from("user_roles").insert(userRoleInsert)
            }

            sessionManager.saveSession()
            val newUser = user.copy(id = userId, roles = listOf("user"))
            sessionManager.saveUser(newUser)
            Result.success(newUser)
        } catch (e: Exception) {
            Log.e("AuthRepositoryImpl", "Error en signUp: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun signIn(correo: String, contrasena: String, sessionManager: SessionManager): Result<User> {
        return try {
            supabase.auth.signInWith(Email) {
                email = correo
                password = contrasena
            }

            val userAuth = supabase.auth.currentUserOrNull()
                ?: return Result.failure(Exception("Credenciales incorrectas"))

            sessionManager.saveSession()

            val usersList: List<UserDto> = supabase.from("users").select().decodeList<UserDto>()
            val dto = usersList.firstOrNull { it.id == userAuth.id }
                ?: return Result.failure(Exception("Usuario no encontrado"))

            val roles = getUserRoles(dto.id)
            val user = User(
                id = dto.id,
                nombre = dto.nombre,
                usuario = dto.usuario,
                cedula = dto.cedula,
                telefono = dto.telefono,
                correo = dto.correo,
                contrasena = "",
                roles = roles
            )

            sessionManager.saveUser(user)
            Result.success(user)
        } catch (e: Exception) {
            Log.e("AuthRepositoryImpl", "Error en signIn: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun signOut(sessionManager: SessionManager) {
        try {
            supabase.auth.signOut()
            sessionManager.clearSession()
        } catch (e: Exception) {
            Log.e("AuthRepositoryImpl", "Error en logout: ${e.message}")
        }
    }

    override suspend fun getUserRoles(userId: String): List<String> {
        return try {
            val result = supabase.from("user_roles").select(
                Columns.raw(
                    """
                        role_id,
                        roles!fk_user_roles_role(id,nombre)
                    """.trimIndent()
                )
            ) {
                filter { eq("user_id", userId) }
            }.decodeList<UserRoleWithName>()

            result.map { it.roles.nombre }
        } catch (e: Exception) {
            Log.e("AuthRepositoryImpl", "Error obteniendo roles: ${e.message}")
            emptyList()
        }
    }
}

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

@Serializable
data class UserDto(
    val id: String,
    val nombre: String,
    val usuario: String,
    val cedula: String,
    val telefono: String,
    val correo: String
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
