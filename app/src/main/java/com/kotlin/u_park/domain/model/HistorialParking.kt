package com.kotlin.u_park.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class HistorialParking(
    val parking_id: String,
    val garage_nombre: String,
    val vehicle_model: String?,
    val plate: String,
    val hora_entrada: String,
    val hora_salida: String?,
    val estado: String,
    val tipo: String
)
