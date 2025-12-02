package com.kotlin.u_park.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Vehicle(
    val id: String,
    val user_id: String? = null,
    val plate: String,
    val model: String? = null,
    val registration: String? = null,
    val color: String? = null,
    val type_id: Int? = null,
    val year: Int? = null
)
@Serializable
data class VehicleTypeSimple(
    val id: Int,
    val name: String
)

@Serializable
data class VehiculoSimple(
    val id: String,
    val model: String? = null,
    val color: String? = null
)