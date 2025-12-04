package com.kotlin.u_park.data.repository

import com.kotlin.u_park.domain.model.Vehicle
import com.kotlin.u_park.domain.repository.VehiclesRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from

class VehiclesRepositoryImpl(
    private val supabase: SupabaseClient
): VehiclesRepository {

    override suspend fun getVehiclesByUser(userId: String): List<Vehicle> {
        return try {
            supabase.from("vehicles")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeList<Vehicle>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}

