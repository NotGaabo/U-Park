package com.kotlin.u_park.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Reserva(
    val id: String? = null,
    val garage_id: String,
    val vehicle_id: String,
    val empleado_id: String? = null,
    val hora_reserva: String,
    val hora_llegada: String? = null,
    val estado: String
)
