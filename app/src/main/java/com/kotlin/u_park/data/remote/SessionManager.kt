package com.kotlin.u_park.data.remote

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.kotlin.u_park.domain.model.User
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class SessionManager private constructor(
    context: Context,
    private val supabase: SupabaseClient
) {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth")
    private val dataStore = context.dataStore

    private val TOKEN_KEY = stringPreferencesKey("accessToken")
    private val REFRESH_KEY = stringPreferencesKey("refreshToken")
    private val USER_KEY = stringPreferencesKey("userData")
    private val ACTIVE_ROLE_KEY = stringPreferencesKey("activeRole")

    suspend fun saveSession() {
        val s = supabase.auth.currentSessionOrNull() ?: return
        dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = s.accessToken
            prefs[REFRESH_KEY] = s.refreshToken
        }
        Log.d("SessionManager", "✅ Sesión guardada en DataStore.")
    }

    suspend fun saveUser(user: User) {
        val json = Json.encodeToString(user)
        dataStore.edit { prefs -> prefs[USER_KEY] = json }
        Log.d("SessionManager", "✅ Usuario guardado: ${user.nombre}")
    }

    suspend fun getUser(): User? {
        return dataStore.data.map { prefs ->
            prefs[USER_KEY]?.let { Json.decodeFromString<User>(it) }
        }.firstOrNull()
    }

    // 👉 Nuevo: flujo observable del usuario
    fun getUserFlow(): Flow<User?> = dataStore.data.map { prefs ->
        prefs[USER_KEY]?.let { Json.decodeFromString<User>(it) }
    }

    val sessionFlow: Flow<Pair<String, String>?> = dataStore.data.map { prefs ->
        val access = prefs[TOKEN_KEY]
        val refresh = prefs[REFRESH_KEY]
        if (access != null && refresh != null) access to refresh else null
    }

    suspend fun restoreSession() {
        val session = sessionFlow.firstOrNull()
        session?.let { (_, refresh) ->
            try {
                val newSession = supabase.auth.refreshSession(refresh)
                dataStore.edit { prefs ->
                    prefs[TOKEN_KEY] = newSession.accessToken
                    prefs[REFRESH_KEY] = newSession.refreshToken
                }
                Log.d("SessionManager", "✅ Sesión restaurada correctamente.")
            } catch (e: Exception) {
                if (e.message?.contains("already_used") == true) {
                    Log.w("SessionManager", "⚠️ Refresh token ya usado, sesión aún válida.")
                    return
                }
                Log.e("SessionManager", "❌ Error al restaurar sesión: ${e.message}")
                clearSession()
            }
        } ?: Log.d("SessionManager", "ℹ️ No había sesión almacenada.")
    }

    suspend fun refreshSessionFromDataStore(): Boolean {
        supabase.auth.currentSessionOrNull()?.let {
            Log.d("SessionManager", "✅ Ya hay sesión activa.")
            return true
        }

        val session = sessionFlow.firstOrNull() ?: return false
        return try {
            val (_, refresh) = session
            val newSession = supabase.auth.refreshSession(refresh)
            dataStore.edit { prefs ->
                prefs[TOKEN_KEY] = newSession.accessToken
                prefs[REFRESH_KEY] = newSession.refreshToken
            }
            Log.d("SessionManager", "✅ Sesión refrescada correctamente.")
            true
        } catch (e: Exception) {
            if (e.message?.contains("already_used") == true) {
                Log.w("SessionManager", "⚠️ Refresh token ya usado, ignorando.")
                return true
            }
            Log.e("SessionManager", "❌ Error al refrescar sesión: ${e.message}")
            clearSession()
            false
        }
    }

    suspend fun saveActiveRole(role: String) {
        dataStore.edit { prefs -> prefs[ACTIVE_ROLE_KEY] = role }
        Log.d("SessionManager", "🎯 Rol activo guardado: $role")
    }

    suspend fun getActiveRole(): String? {
        return dataStore.data.map { prefs -> prefs[ACTIVE_ROLE_KEY] }.firstOrNull()
    }

    suspend fun clearSession() {
        dataStore.edit { it.clear() }
        try {
            supabase.auth.signOut()
            Log.d("SessionManager", "🧹 Sesión limpiada correctamente.")
        } catch (_: Exception) {}
    }

    companion object {
        @Volatile
        private var INSTANCE: SessionManager? = null

        fun getInstance(context: Context, supabase: SupabaseClient): SessionManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SessionManager(context.applicationContext, supabase).also {
                    INSTANCE = it
                }
            }
        }
    }
}
