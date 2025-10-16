package com.kotlin.u_park.data.repository

import com.kotlin.u_park.domain.model.User
import com.kotlin.u_park.data.remote.supabase
import io.github.jan.supabase.postgrest.postgrest

class AuthRepository {

    private fun esContrasenaSegura(contrasena: String): Boolean {
        return contrasena.length >= 8 &&
                contrasena.any { it.isDigit() } &&
                contrasena.any { it.isUpperCase() } &&
                contrasena.any { !it.isLetterOrDigit() }
    }


    suspend fun registerUser(user: User): Boolean {
        return try {
            if (!esContrasenaSegura(user.contrasena)) {
                println("Contraseña no segura.")
                return false
            }

            // Inserción en Supabase (lanza excepción si falla)
            supabase.postgrest["users"].insert(user)

            println("Usuario registrado correctamente: $user")
            true
        } catch (e: Exception) {
            println("Error al registrar usuario: ${e.message}")
            false
        }
    }

}
