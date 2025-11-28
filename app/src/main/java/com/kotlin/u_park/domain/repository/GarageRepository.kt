package com.kotlin.u_park.domain.repository

import com.kotlin.u_park.domain.model.Garage
import java.io.File

interface GarageRepository {

    suspend fun newGarage(garage: Garage, imageFile: File?): Boolean

    // 🔹 Devuelve todos los garajes del usuario (dueño)
    suspend fun getGaragesByUserId(userId: String): List<Garage>

    // 🔹 Para ParkingViewModel (dueño)
    suspend fun getGaragesByOwner(ownerId: String): List<Garage>

    suspend fun getGarageById(garageId: String): Garage?
}
