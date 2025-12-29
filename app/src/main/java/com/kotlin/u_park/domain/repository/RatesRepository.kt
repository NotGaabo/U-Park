package com.kotlin.u_park.domain.repository

import com.kotlin.u_park.domain.model.Rate
import com.kotlin.u_park.domain.model.SalidaResponse

interface RatesRepository {

    // RPC functions
    suspend fun asignarTarifa(garageId: String, vehicleId: String): String
    suspend fun calcularSalida(parkingId: String): SalidaResponse

    // Vehicle types
    suspend fun getVehicleTypes(): List<Pair<Int, String>>

    // 🔥 ACTUALIZADO: Garages con filtro opcional por userId
    suspend fun getGarages(userId: String? = null): List<Pair<String, String>>

    // Rates CRUD
    suspend fun getAllRatesForOwner(userId: String): Map<String, List<Rate>>
    suspend fun createRate(rate: Rate): Rate
    suspend fun updateRate(id: String, rate: Rate): Rate
    suspend fun deleteRate(id: String)
    suspend fun toggleActive(id: String, active: Boolean): Rate

    // Helper functions
    suspend fun getVehicleNameById(id: String): String
    suspend fun getVehicleIdByPlate(plate: String): String?
    suspend fun getGarageNameById(id: String): String
}