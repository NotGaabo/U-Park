package com.kotlin.u_park.domain.repository

import com.kotlin.u_park.domain.model.Parking

interface ParkingRepository {
    suspend fun registrarEntrada(
        parking: Parking,
        fotosBytes: List<ByteArray>
    ): Parking

    suspend fun registrarSalida(parkingId: Int, horaSalida: String): Parking

    suspend fun getParkingById(id: Int): Parking?
}
