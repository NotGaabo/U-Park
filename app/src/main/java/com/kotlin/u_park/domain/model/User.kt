package com.kotlin.u_park.domain.model

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable


@Serializable
data class User(
    val nombre: String,
    val usuario: String,
    val cedula: String,
    val telefono: String,
    val correo: String,
    val contrasena: String,
    val rol: String = "user",
    val direccion: String? = null,
)