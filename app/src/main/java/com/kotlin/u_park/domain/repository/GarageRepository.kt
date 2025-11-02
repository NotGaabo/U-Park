package com.kotlin.u_park.domain.repository

import com.kotlin.u_park.domain.model.Garage
import java.io.File

interface GarageRepository {
    suspend fun newGarage(garage: Garage, imageFile: File?): Boolean
    suspend fun getGarageByUserId(userId: String): Garage?
}
