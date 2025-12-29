package com.kotlin.u_park.data.repository

import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.kotlin.u_park.data.remote.SessionManager
import com.kotlin.u_park.domain.model.*
import com.kotlin.u_park.domain.repository.AuthRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.tasks.await

class AuthRepositoryImpl(private val supabase: SupabaseClient) : AuthRepository {

    // ---------------------------
    // 🔥 TOKEN FCM — UPSERT
    // ---------------------------
    private suspend fun saveTokenFCM(userId: String) {
        try {
            val token = FirebaseMessaging.getInstance().token.await()

            supabase.from("device_tokens").upsert(
                mapOf(
                    "user_id" to userId,
                    "token" to token
                )
            )

            Log.d("AuthRepository", "🔥 Token FCM guardado/actualizado: $token")

        } catch (e: Exception) {
            Log.e("AuthRepository", "❌ Error guardando token FCM: ${e.message}")
        }
    }


    // ---------------------------
    // 🔄 RESTAURAR SESIÓN
    // ---------------------------
    override suspend fun restoreCurrentUser(sessionManager: SessionManager): User? {
        return try {
            val restored = sessionManager.refreshSessionFromDataStore()
            if (!restored) return null

            val authUser = supabase.auth.currentUserOrNull() ?: return null

            val dto = supabase.from("users").select().decodeList<UserDto>()
                .firstOrNull { it.id == authUser.id } ?: return null

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
            user

        } catch (e: Exception) {
            Log.e("AuthRepository", "❌ Error restaurando usuario: ${e.message}")
            null
        }
    }


    // ---------------------------
    // 📝 REGISTRO
    // ---------------------------
    override suspend fun signUp(user: User, sessionManager: SessionManager): Result<User> {
        return try {

            supabase.auth.signUpWith(Email) {
                email = user.correo
                password = user.contrasena
            }

            val authUser = supabase.auth.currentUserOrNull()
                ?: return Result.failure(Exception("No se pudo crear el usuario"))

            val userId = authUser.id

            val insertDTO = UserInsertDTO(
                id = userId,
                nombre = user.nombre,
                usuario = user.usuario,
                cedula = user.cedula,
                telefono = user.telefono,
                correo = user.correo
            )

            supabase.from("users").insert(insertDTO)

            // asignar rol por defecto
            val role = supabase.from("roles")
                .select(columns = Columns.list("id", "nombre"))
                .decodeList<RoleRow>()
                .firstOrNull { it.nombre == "user" }

            role?.let {
                supabase.from("user_roles")
                    .insert(UserRoleInsertDTO(userId, it.id))
            }

            sessionManager.saveSession()

            val newUser = user.copy(id = userId, roles = listOf("user"))
            sessionManager.saveUser(newUser)

            // 🔥 token FCM
            saveTokenFCM(userId)

            Result.success(newUser)

        } catch (e: Exception) {
            Log.e("AuthRepository", "❌ Error en signUp: ${e.message}")
            Result.failure(e)
        }
    }


    // ---------------------------
    // 🔐 LOGIN
    // ---------------------------
    override suspend fun signIn(email: String, password: String, sessionManager: SessionManager): Result<User> {
        return try {

            supabase.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }

            val authUser = supabase.auth.currentUserOrNull()
                ?: return Result.failure(Exception("Credenciales inválidas"))

            sessionManager.saveSession()

            val dto = supabase.from("users").select().decodeList<UserDto>()
                .firstOrNull { it.id == authUser.id }
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

            // 🔥 token FCM// LOGIN
            saveTokenFCM(user.id!!)

            Result.success(user)

        } catch (e: Exception) {
            Log.e("AuthRepository", "❌ Error en signIn: ${e.message}")
            Result.failure(e)
        }
    }


    // ---------------------------
    // 🚪 LOGOUT
    // ---------------------------
    override suspend fun signOut(sessionManager: SessionManager) {
        try {
            val user = sessionManager.getUser()

            user?.id?.let { uid ->
                try {
                    supabase.from("device_tokens").delete {
                        filter { eq("user_id", uid) }
                    }
                    Log.d("AuthRepository", "🧹 Token FCM eliminado correctamente.")
                } catch (e: Exception) {
                    Log.e("AuthRepository", "❌ Error eliminando token: ${e.message}")
                }
            }

            supabase.auth.signOut()
            sessionManager.clearSession()

        } catch (e: Exception) {
            Log.e("AuthRepository", "❌ Error en signOut: ${e.message}")
        }
    }


    // ---------------------------
    // 🎭 ROLES
    // ---------------------------
    override suspend fun getUserRoles(userId: String): List<String> {
        return try {
            supabase.from("user_roles").select(
                Columns.raw("""
                    role_id,
                    roles!fk_user_roles_role(id,nombre)
                """.trimIndent())
            ) {
                filter { eq("user_id", userId) }
            }.decodeList<UserRoleWithName>()
                .map { it.roles.nombre }

        } catch (e: Exception) {
            Log.e("AuthRepository", "❌ Error obteniendo roles: ${e.message}")
            emptyList()
        }
    }
}
