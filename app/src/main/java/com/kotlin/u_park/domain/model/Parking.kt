package com.kotlin.u_park.domain.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Parking(
    val id: String? = null,
    val vehicle_id: String? = null,
    val garage_id: String? = null,
    val rate_id: String? = null,
    val hora_entrada: String,
    val hora_salida: String? = null,
    val total: Double? = null,
    val pagado: Boolean = false,
    val created_at: String? = null,
    val fotos: List<String> = emptyList(),
    val tipo: String = "entrada",
    val estado: String = "pendiente",
    val created_by_user_id: String? = null
)
@Serializable
data class ParkingActividad(
    val id: String? = null,
    val tipo: String? = null,
    val hora_entrada: String? = null,
    val hora_salida: String? = null,
    val vehicles: VehiclePlate? = null
)

@Serializable
data class VehiclePlate(
    val plate: String? = null
)



