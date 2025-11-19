package com.kotlin.u_park.domain.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Parking(
    val id: Int? = null,
    val garageId: String,
    val vehicleId: String,
    val empleadoId: String,
    val horaEntrada: String,
    val horaSalida: String? = null,
    val fotos: List<String> = emptyList(),
    val total: Double? = null
)
