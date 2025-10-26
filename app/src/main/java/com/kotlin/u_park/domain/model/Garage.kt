package com.kotlin.u_park.domain.model

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class Garage(
    val id_garage: String = "",
    val nombre: String = "",
    val image_url: String? = null,
    val direccion: String = "",
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val capacidad_total: Int = 0,
    val horario: String = "",
    val fecha_creacion: LocalDateTime? = null
)
