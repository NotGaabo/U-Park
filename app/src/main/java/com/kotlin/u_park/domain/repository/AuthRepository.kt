package com.kotlin.u_park.domain.repository

import com.kotlin.u_park.domain.model.User
import com.kotlin.u_park.data.remote.SessionManager

interface AuthRepository {
    suspend fun restoreCurrentUser(sessionManager: SessionManager): User?
    suspend fun signUp(user: User, sessionManager: SessionManager): Result<User>
    suspend fun signIn(correo: String, contrasena: String, sessionManager: SessionManager): Result<User>
    suspend fun signOut(sessionManager: SessionManager)
    suspend fun getUserRoles(userId: String): List<String>
}
