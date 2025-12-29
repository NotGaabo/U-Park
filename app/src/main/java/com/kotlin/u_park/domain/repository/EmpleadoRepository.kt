package com.kotlin.u_park.domain.repository

import com.kotlin.u_park.domain.model.EmpleadoGarage
import com.kotlin.u_park.domain.model.Stats

interface EmpleadoGarageRepository {
    suspend fun addEmpleadoToGarage(garageId: String, empleadoCedula: Long): Boolean
    suspend fun getEmpleadosByGarage(garageId: String): List<EmpleadoGarage>
    suspend fun removeEmpleadoFromGarage(garageId: String, empleadoCedula: Long): Boolean
    suspend fun getStats(garageId: String): Stats
    suspend fun getGarageByEmpleadoId(cedula: Long): String?
}