package com.kotlin.u_park.domain.repository

import com.kotlin.u_park.domain.model.Parking

interface ParkingRepository {

    suspend fun registrarEntrada(
        parking: Parking,
        fotosBytes: List<ByteArray>
    ): Parking

    suspend fun registrarSalida(parkingId: Int, horaSalida: String): Parking

    suspend fun getParkingById(id: Int): Parking?

    // NUEVO
    suspend fun crearReserva(parking: Parking): Parking
    suspend fun getVehiculosDentro(): List<Parking>
    suspend fun getVehiculosFuera(): List<Parking>
    suspend fun getReservas(): List<Parking>

        suspend fun getReservasByGarage(garageId: String): List<Parking>

        suspend fun activarReserva(reservaId: Int): Parking

        suspend fun cancelarReserva(reservaId: Int): Boolean

}
