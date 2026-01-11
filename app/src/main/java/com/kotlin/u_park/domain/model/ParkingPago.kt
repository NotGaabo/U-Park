package com.kotlin.u_park.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ParkingPago(
    val id: String? = null,
    val parking_id: String,
    val metodo: String,
    val comprobante_url: String? = null,
    val empleado_id: String
)
