package com.kotlin.u_park.data.remote

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

class SessionManager private constructor(
    context: Context,
    private val supabase: SupabaseClient
) {

    // Configuración de DataStore
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth")
    private val dataStore = context.dataStore

    private val TOKEN_KEY = stringPreferencesKey("accessToken")
    private val REFRESH_KEY = stringPreferencesKey("refreshToken")

    // ✅ Guarda la sesión actual (sin usar tipo Session)
    suspend fun saveSession() {
        val s = supabase.auth.currentSessionOrNull() ?: return
        dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = s.accessToken
            prefs[REFRESH_KEY] = s.refreshToken
        }
        Log.d("SessionManager", "✅ Sesión guardada en DataStore.")
    }

    // ✅ Flujo reactivo con tokens
    val sessionFlow: Flow<Pair<String, String>?> = dataStore.data.map { prefs ->
        val access = prefs[TOKEN_KEY]
        val refresh = prefs[REFRESH_KEY]
        if (access != null && refresh != null) access to refresh else null
    }

    // ✅ Restaurar sesión desde DataStore
    suspend fun restoreSession() {
        val session = sessionFlow.firstOrNull()
        session?.let { (_, refresh) ->
            try {
                val newSession = supabase.auth.refreshSession(refresh)
                // guarda nuevos tokens
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

    // ✅ Refrescar sesión de forma segura
    suspend fun refreshSessionFromDataStore(): Boolean {
        // No refrescar si ya hay sesión activa
        supabase.auth.currentSessionOrNull()?.let {
            Log.d("SessionManager", "✅ Ya hay sesión activa, no se refresca.")
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
                Log.w("SessionManager", "⚠️ Refresh token ya usado, ignorando sin limpiar sesión.")
                return true
            }
            Log.e("SessionManager", "❌ Error al refrescar sesión: ${e.message}")
            clearSession()
            false
        }
    }

    // ✅ Limpieza completa
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
                INSTANCE ?: SessionManager(context.applicationContext, supabase).also { INSTANCE = it }
            }
        }
    }
}
