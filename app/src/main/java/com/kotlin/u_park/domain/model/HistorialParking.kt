package com.kotlin.u_park.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class HistorialParking(
    val parking_id: String,
    val garage_id: String,
    val garage_nombre: String?,
    val garage_direccion: String?,
    val garage_imagen: String?,
    val vehicle_id: String,
    val plate: String?,
    val model: String?,
    val hora_entrada: String?,
    val hora_salida: String?,
    val estado: String,
    val tipo: String
)
