package com.kotlin.u_park.domain.repository

import com.kotlin.u_park.domain.model.*

interface ParkingRepository {

    suspend fun registrarEntrada(
        parking: Parking,
        fotosBytes: List<ByteArray>
    ): Parking

    suspend fun registrarSalida(
        parkingId: String,
        horaSalida: String,
        empleadoId: String
    ): Parking

    suspend fun registrarSalidaConPago(
        parkingId: String,
        horaSalida: String,
        empleadoId: String,
        metodoPago: String,
        comprobanteBytes: ByteArray?
    ): Parking

    suspend fun getParkingById(id: String): Parking?
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

    suspend fun cancelarReserva(reservaId: String): Boolean
    suspend fun crearReserva(parking: Parking): Parking
    suspend fun activarReserva(reservaId: String): Parking

    suspend fun getActividadReciente(garageId: String): List<ParkingActividad>
    suspend fun estaVehiculoDentro(vehicleId: String): Boolean
    suspend fun getHistorialByUser(userId: String): List<HistorialParking>
}
