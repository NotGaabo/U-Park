package com.kotlin.u_park.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Garage(
    @SerialName("id_garage")
    val idGarage: String = "", // UUID autogenerado por la DB
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

@Serializable
data class GarageSimple(
    @SerialName("id_garage")
    val idGarage: String,
    val nombre: String,
    @SerialName("user_id")
    val userId: String?
)

@Serializable
data class GarageNameSimple(
    val id_garage: String,
    val nombre: String
)

@Serializable
data class GarageInsert(
    val id_garage: String,
    val nombre: String,
    val direccion: String,
    val latitud: Double,
    val longitud: Double,
    val capacidad_total: Int,
    val horario: String? = null,
    val fecha_creacion: String? = null,
    val image_url: String? = null,
    val is_active: Boolean = true,
    val user_id: String?
)

