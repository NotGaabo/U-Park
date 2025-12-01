package com.kotlin.u_park.domain.model

@kotlinx.serialization.Serializable
data class SalidaResponse(
    val parking_id: String,
    val total: Double,
    val hora_entrada: String,
    val hora_salida: String,
    val duration_hours: Double,
    val vehiculo_id: String,
    val garage_id: String
)
