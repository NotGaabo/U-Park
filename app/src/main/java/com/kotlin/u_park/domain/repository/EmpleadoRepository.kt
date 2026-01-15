package com.kotlin.u_park.domain.repository

import com.kotlin.u_park.domain.model.EmpleadoGarage
import com.kotlin.u_park.domain.model.Stats

interface EmpleadoGarageRepository {
    suspend fun getGarageByEmpleadoId(cedula: Long): String?
    suspend fun getEmpleadosByGarage(garageId: String): List<EmpleadoGarage>
    suspend fun addEmpleadoToGarage(garageId: String, cedula: Long): Boolean
    suspend fun removeEmpleadoFromGarage(garageId: String, cedula: Long): Boolean
    suspend fun getStats(garageId: String): Stats
}