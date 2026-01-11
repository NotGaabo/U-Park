package com.kotlin.u_park.domain.repository

import com.kotlin.u_park.domain.model.Rate
import com.kotlin.u_park.domain.model.SalidaResponse

interface RatesRepository {

    // RPC PREVIEW (solo lectura)
    suspend fun calcularSalidaPreview(parkingId: String): SalidaResponse
    suspend fun confirmarSalida(parkingId: String)

    // Vehicle types
    suspend fun getVehicleTypes(): List<Pair<Int, String>>

    // Garages
    suspend fun getGarages(userId: String? = null): List<Pair<String, String>>

    // Rates CRUD
    suspend fun getAllRatesForOwner(userId: String): Map<String, List<Rate>>
    suspend fun createRate(rate: Rate): Rate
    suspend fun updateRate(id: String, rate: Rate): Rate
    suspend fun deleteRate(id: String)
    suspend fun toggleActive(id: String, active: Boolean): Rate

    // Helpers
    suspend fun getVehicleNameById(id: String): String
    suspend fun getVehicleIdByPlate(plate: String): String?
    suspend fun getGarageNameById(id: String): String
}
