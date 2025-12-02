package com.kotlin.u_park.domain.repository

import com.kotlin.u_park.domain.model.HistorialParking
import com.kotlin.u_park.domain.model.Parking
import com.kotlin.u_park.domain.model.ParkingActividad
import com.kotlin.u_park.domain.model.ReservaConUsuario

interface ParkingRepository {
    suspend fun registrarEntrada(parking: Parking, fotosBytes: List<ByteArray>): Parking
    suspend fun registrarSalida(parkingId: String, horaSalida: String): Parking
    suspend fun getParkingById(id: String): Parking?
    suspend fun crearReserva(parking: Parking): Parking
    suspend fun getVehiculosDentro(): List<ParkingActividad>
    suspend fun getVehicleIdByPlate(plate: String): String?
    suspend fun getVehiculosFuera(): List<Parking>
    suspend fun getReservas(): List<Parking>
    suspend fun getReservasByGarage(garageId: String): List<Parking>
    suspend fun getReservasConUsuario(garageId: String): List<ReservaConUsuario>
    suspend fun registrarEntradaDesdeReserva(
        reserva: ReservaConUsuario,
        fotosBytes: List<ByteArray>,
        empleadoId: String
    ): Parking
    suspend fun activarReserva(reservaId: Int): Parking
    suspend fun cancelarReserva(reservaId: Int): Boolean
    suspend fun getActividadReciente(garageId: String): List<ParkingActividad>
    suspend fun estaVehiculoDentro(vehicleId: String): Boolean
    suspend fun getHistorialByUser(userId: String): List<HistorialParking>
}