package com.kotlin.u_park.domain.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Parking(
    val id: Int? = null,
    val garageId: String,
    val vehicleId: String,
    val empleadoId: String? = null,       // empleado para ENTRADAS
    val createdByUserId: String? = null,  // usuario para RESERVAS
    val horaEntrada: String,
    val horaSalida: String? = null,
    val fotos: List<String> = emptyList(),
    val total: Double? = null,
    val tipo: String = "entrada",     // entrada o reserva
    val estado: String = "activa"
)


