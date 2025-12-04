package com.kotlin.u_park.domain.repository

import com.kotlin.u_park.domain.model.Vehicle

interface VehiclesRepository {
    suspend fun getVehiclesByUser(userId: String): List<Vehicle>
}