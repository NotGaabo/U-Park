package com.kotlin.u_park.domain.repository

import com.kotlin.u_park.domain.model.Rate
import com.kotlin.u_park.domain.model.SalidaResponse

interface RatesRepository {
    // ---- Cálculo salida (ya lo tenías) ----
    suspend fun asignarTarifa(garageId: String, vehicleId: String): String
    suspend fun calcularSalida(parkingId: String): SalidaResponse
    suspend fun getVehicleNameById(id: String): String
    suspend fun getGarageNameById(id: String): String
    suspend fun getVehicleIdByPlate(plate: String): String?
    // ---- CRUD de tarifas ----
    suspend fun getRatesByGarage(garageId: String): List<Rate>
    suspend fun getVehicleTypes(): List<Pair<Int, String>>
    suspend fun getGarages(): List<Pair<String, String>>
    suspend fun createRate(rate: Rate): Rate
    suspend fun updateRate(id: String, rate: Rate): Rate
    suspend fun deleteRate(id: String)
    suspend fun toggleActive(id: String, active: Boolean): Rate
}