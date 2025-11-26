package com.kotlin.u_park.domain.repository

import com.kotlin.u_park.domain.model.Parking
import com.kotlin.u_park.domain.model.Reserva

interface ReservasRepository {
    suspend fun crearReserva(reserva: Parking): Reserva
    suspend fun activarReserva(id: String): Reserva
    suspend fun cancelarReserva(id: String): Boolean
    suspend fun listarReservasPorGarage(garageId: String): List<Reserva>
}
