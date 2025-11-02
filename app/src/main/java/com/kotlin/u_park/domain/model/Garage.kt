package com.kotlin.u_park.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Garage(
    @SerialName("id_garage")
    val idGarage: String? = null, // UUID autogenerado por la DB

    val nombre: String = "",
    val direccion: String = "",
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,

    @SerialName("capacidad_total")
    val capacidadTotal: Int = 0,

    val horario: String? = null,

    @SerialName("fecha_creacion")
    val fechaCreacion: String? = null, // Supabase devuelve timestamp como string

    @SerialName("image_url")
    val imageUrl: String? = null,

    @SerialName("is_active")
    val isActive: Boolean = true,

    @SerialName("user_id")
    val userId: String? = null
)
