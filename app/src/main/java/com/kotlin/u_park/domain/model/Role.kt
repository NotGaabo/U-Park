package com.kotlin.u_park.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Role(
    val id: Int,
    val nombre: String
)

@Serializable
data class UserRole(
    val user_id: String,
    val role_id: Int
)

@Serializable
data class RoleRow(
    val id: Int,
    val nombre: String
)

@Serializable
data class UserRoleWithName(
    val role_id: Int,
    val roles: RoleRow
)

@Serializable
data class UserRoleInsertDTO(
    val user_id: String,
    val role_id: Int
)