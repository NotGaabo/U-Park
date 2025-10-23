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

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth")
    private val dataStore = context.dataStore

    private val TOKEN_KEY = stringPreferencesKey("accessToken")
    private val REFRESH_KEY = stringPreferencesKey("refreshToken")

    // Guarda la sesión actual
    suspend fun saveSession() {
        val session = supabase.auth.currentSessionOrNull()
        session?.let {
            dataStore.edit { prefs ->
                prefs[TOKEN_KEY] = it.accessToken
                prefs[REFRESH_KEY] = it.refreshToken
            }
        }
    }

    // Flujo de sesión
    val sessionFlow: Flow<Pair<String, String>?> = dataStore.data.map { prefs ->
        val access = prefs[TOKEN_KEY]
        val refresh = prefs[REFRESH_KEY]
        if (access != null && refresh != null) access to refresh else null
    }

    // Restaurar sesión
    suspend fun restoreSession() {
        val session = sessionFlow.firstOrNull()
        session?.let { (_, refresh) ->
            try {
                val newSession = supabase.auth.refreshSession(refresh)
                Log.d("SessionManager", "AccessToken renovado: ${newSession.accessToken}")
                dataStore.edit { prefs ->
                    prefs[TOKEN_KEY] = newSession.accessToken
                    prefs[REFRESH_KEY] = newSession.refreshToken
                }
            } catch (e: Exception) {
                Log.e("SessionManager", "No se pudo restaurar sesión: ${e.message}")
                clearSession()
            }
        }
    }

    // Refrescar sesión directamente desde DataStore
    suspend fun refreshSessionFromDataStore(): Boolean {
        val session = sessionFlow.firstOrNull() ?: return false
        return try {
            val (_, refresh) = session
            val newSession = supabase.auth.refreshSession(refresh)
            saveSession() // guarda los nuevos tokens
            true
        } catch (e: Exception) {
            Log.e("SessionManager", "No se pudo refrescar sesión: ${e.message}")
            clearSession()
            false
        }
    }

    suspend fun clearSession() {
        dataStore.edit { it.clear() }
        try {
            supabase.auth.signOut()
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
