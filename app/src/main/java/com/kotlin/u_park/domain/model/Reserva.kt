package com.kotlin.u_park.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Reserva(
    val id: String? = null,
    val garage_id: String,
    val vehicle_id: String,
    val empleado_id: String? = null,
    val hora_reserva: String,
    val hora_llegada: String? = null,
    val estado: String = "pendiente"  // ✔ valor por defecto
)

@Serializable
data class ReservaConUsuario(
    val id: String? = null,
    val garage_id: String,
    val vehicle_id: String? = null,
    val estado: String? = null,
    val hora_reserva: String? = null,
    val hora_llegada: String? = null,
    val vehicles: VehicleUser? = null,
    val users: UsuarioData? = null   // 👈 viene directo ahora
)

@Serializable
data class VehicleUser(
    val plate: String? = null,
    val user_id: String? = null
)

@Serializable
data class UsuarioData(
    val id: String? = null,               // ✔ AGREGADO (muy usado)
    val nombre: String? = null,
    val usuario: String? = null,
    val cedula: Long? = null,
    val telefono: String? = null,
    val correo: String? = null
)
