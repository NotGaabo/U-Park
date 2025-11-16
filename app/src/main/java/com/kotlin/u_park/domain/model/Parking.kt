package com.kotlin.u_park.domain.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Parking(
    val id: String = UUID.randomUUID().toString(),
    val vehicle_id: String? = null,
    val garage_id: String? = null,
    val rate_id: String? = null,
    val hora_entrada: String, // Timestamp como String
    val hora_salida: String? = null, // Timestamp como String
    val total: Double? = null,
    val pagado: Boolean? = false,
    val created_at: String? = null
)