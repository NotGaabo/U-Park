package com.kotlin.u_park.domain.repository

import com.kotlin.u_park.domain.model.Garage
import com.kotlin.u_park.domain.model.Parking
import com.kotlin.u_park.domain.model.Rate
import java.io.File

interface GarageRepository {

    // 🔹 Inserta un nuevo garaje (con o sin imagen)
    suspend fun newGarage(garage: Garage, imageFile: File?): Boolean

    // 🔹 Devuelve todos los garajes de un usuario
    suspend fun getGaragesByUserId(userId: String): List<Garage>

    // 🚗 Tarifas (Rates)
//    suspend fun getRatesByGarage(garageId: String): List<Rate>
//    suspend fun addRate(rate: Rate): Boolean
//    suspend fun updateRate(rate: Rate): Boolean
//
    suspend fun getGarageById(garageId: String): Garage?

//    // 🅿️ Parkings (ocupaciones activas o históricas)
//    suspend fun getParkingsByGarage(garageId: String): List<Parking>
//    suspend fun startParking(parking: Parking): Boolean
//    suspend fun endParking(parkingId: String, horaSalida: String, total: Double): Boolean
}
