package com.kotlin.u_park.data.repository

import com.kotlin.u_park.domain.model.Vehicle
import com.kotlin.u_park.domain.repository.VehiclesRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import java.util.UUID
import com.kotlin.u_park.domain.model.VehicleInsert
import com.kotlin.u_park.domain.model.VehicleTypeSimple


class VehiclesRepositoryImpl(
    private val supabase: SupabaseClient
): VehiclesRepository {

    suspend fun getVehicleTypes(): List<VehicleTypeSimple> {
        return try {
            supabase
                .from("vehicle_types")
                .select {
                    filter {
                        eq("active", true)
                    }
                }
                .decodeList<VehicleTypeSimple>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

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

    override suspend fun addVehicle(vehicle: Vehicle): Boolean {
        return try {
            val vehicleID = vehicle.id ?: UUID.randomUUID().toString()

            val insert = VehicleInsert(
                id = vehicleID,
                user_id = vehicle.user_id,
                plate = vehicle.plate,
                model = vehicle.model,
                color = vehicle.color,
                type_id = vehicle.type_id,
                year = vehicle.year
            )

            supabase.from("vehicles").insert(insert)

            true

        } catch (e: Exception) {
            e.printStackTrace()
            false
        }


    }

    override suspend fun deleteVehicle(vehicleId: String): Boolean {
        return try {
            supabase.from("vehicles")
                .delete {
                    filter {
                        eq("id", vehicleId)
                    }
                }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}