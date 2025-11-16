package com.kotlin.u_park.domain.repository

import com.kotlin.u_park.domain.model.Garage
import java.io.File

interface GarageRepository {

    // 🔹 Inserta un nuevo garaje (con o sin imagen)
    suspend fun newGarage(garage: Garage, imageFile: File?): Boolean

    // 🔹 Devuelve todos los garajes de un usuario
    suspend fun getGaragesByUserId(userId: String): List<Garage>
}
