package com.kotlin.u_park.domain.repository

import com.kotlin.u_park.domain.model.EmpleadoGarage
import com.kotlin.u_park.presentation.screens.employee.Stats

interface EmpleadoGarageRepository {

    suspend fun addEmpleadoToGarage(garageId: String, empleadoId: String): Boolean

    suspend fun getEmpleadosByGarage(garageId: String): List<EmpleadoGarage>

    suspend fun removeEmpleadoFromGarage(garageId: String, empleadoId: String): Boolean
    suspend fun getStats(garageId: String): Stats
    suspend fun getGarageByEmpleadoId(userId: String): String?
}

