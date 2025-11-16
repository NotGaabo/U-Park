package com.kotlin.u_park.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class EmpleadoGarage(
    val id: Int? = null,
    val garage_id: String,
    val empleado_id: String,
    val fecha_registro: String? = null,
    val users: User? = null // relación con users
)
