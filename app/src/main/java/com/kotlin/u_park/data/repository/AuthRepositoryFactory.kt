package com.kotlin.u_park.data.repository

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.github.jan.supabase.SupabaseClient

class AuthRepositoryFactory(
    private val supabase: SupabaseClient
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthRepository::class.java)) {
            return AuthRepository(supabase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
