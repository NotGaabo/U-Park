package com.kotlin.u_park.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String? = null,
    val nombre: String,
    val usuario: String,
    val cedula: Long,
    val telefono: String,
    val correo: String,
    val contrasena: String,
    val roles: List<String> = listOf("user"), // <-- ahora es lista de roles
    val direccion: String? = null,
)

@Serializable
data class UserInsertDTO(
    val id: String,
    val nombre: String,
    val usuario: String,
    val cedula: Long,       // int8
    val telefono: String,   // varchar
    val correo: String
)



@Serializable
data class UserDto(
    val id: String,
    val nombre: String,
    val usuario: String,
    val cedula: Long,       // int8
    val telefono: String,   // varchar
    val correo: String
)


