package com.kotlin.u_park.data.repository

import com.kotlin.u_park.domain.model.GarageSimple
import com.kotlin.u_park.domain.model.GarageNameSimple
import com.kotlin.u_park.domain.model.Rate
import com.kotlin.u_park.domain.model.SalidaResponse
import com.kotlin.u_park.domain.model.VehicleTypeSimple
import com.kotlin.u_park.domain.model.VehiculoSimple
import com.kotlin.u_park.domain.repository.RatesRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc

class RatesRepositoryImpl(
    private val supabase: SupabaseClient
) : RatesRepository {

    // ----------------------------------------------------------
    // RPC existentes
    // ----------------------------------------------------------
    override suspend fun asignarTarifa(garageId: String, vehicleId: String): String {
        val response = supabase.postgrest.rpc(
            "asignar_tarifa",
            mapOf(
                "garage" to garageId,
                "vehicle" to vehicleId
            )
        )
        return response.decodeAs<String>()
    }

    override suspend fun calcularSalida(parkingId: String): SalidaResponse {
        return supabase.postgrest.rpc(
            "calcular_salida",
            mapOf("p_parking_id" to parkingId)
        ).decodeAs()
    }

    suspend fun paymethod(){

    }

    // ----------------------------------------------------------
    // Obtener tipos de vehículos
    // ----------------------------------------------------------
    override suspend fun getVehicleTypes(): List<Pair<Int, String>> {
        val result = supabase.from("vehicle_types")
            .select()
            .decodeList<VehicleTypeSimple>()

        return result.map { it.id to it.name }
    }

    // ----------------------------------------------------------
    // Obtener garages
    // ----------------------------------------------------------
    override suspend fun getGarages(): List<Pair<String, String>> {
        val result = supabase.from("garages")
            .select()
            .decodeList<GarageSimple>()

        return result.map { it.idGarage to it.nombre }
    }

    // ----------------------------------------------------------
    // CRUD de tarifas
    // ----------------------------------------------------------
    override suspend fun getRatesByGarage(garageId: String): List<Rate> {
        return supabase.from("rates")
            .select {
                filter { eq("garage_id", garageId) }
            }
            .decodeList()
    }

    override suspend fun createRate(rate: Rate): Rate {
        return supabase.from("rates")
            .insert(rate) {
                select()
            }
            .decodeSingle()
    }

    override suspend fun updateRate(id: String, rate: Rate): Rate {
        val body = mapOf(
            "garage_id" to rate.garageId,
            "vehicle_type_id" to rate.vehicleTypeId,
            "base_rate" to rate.baseRate,
            "time_unit" to rate.timeUnit,
            "hora_inicio" to rate.horaInicio,
            "hora_fin" to rate.horaFin,
            "dias_aplicables" to rate.diasAplicables,
            "special_rate" to rate.specialRate,
            "active" to rate.active,
            "start_date" to rate.startDate,
            "end_date" to rate.endDate
        )

        return supabase.from("rates")
            .update(body) {
                filter { eq("id", id) }
                select()
            }
            .decodeSingle()
    }

    override suspend fun deleteRate(id: String) {
        supabase.from("rates").delete {
            filter { eq("id", id) }
        }
    }

    override suspend fun toggleActive(id: String, active: Boolean): Rate {
        return supabase.from("rates")
            .update(mapOf("active" to active)) {
                filter { eq("id", id) }
                select()
            }
            .decodeSingle()
    }


    // ----------------------------------------------------------
    // Obtener nombre real del vehículo
    // ----------------------------------------------------------
    override suspend fun getVehicleNameById(id: String): String {
        val result = supabase.from("vehicles")
            .select {
                filter { eq("id", id) }
                limit(1)
            }
            .decodeList<VehiculoSimple>()

        if (result.isEmpty()) return "Vehículo"

        val row = result.first()

        // ✔ Ya no es Map, es un data class
        return row.model ?: row.color ?: "Vehículo"
    }

    override suspend fun getVehicleIdByPlate(plate: String): String? {
        val result = supabase.from("vehicles")
            .select {
                filter { eq("plate", plate) }
                limit(1)
            }
            .decodeList<VehiculoSimple>() // crea un data class con id
        return result.firstOrNull()?.id
    }


    // ----------------------------------------------------------
// Obtener nombre real del garage
// ----------------------------------------------------------
    override suspend fun getGarageNameById(id: String): String {
        val result = supabase.from("garages")
            .select {
                filter { eq("id_garage", id) }
                limit(1)
            }
            .decodeList<GarageNameSimple>()

        if (result.isEmpty()) return "Garage"

        val row = result.first()

        // ✔ También es un data class
        return row.nombre
    }

}
