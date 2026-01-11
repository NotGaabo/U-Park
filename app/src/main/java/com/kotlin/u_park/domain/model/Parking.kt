package com.kotlin.u_park.domain.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Parking(
    val id: String? = null,
    val vehicle_id: String? = null,
    val garage_id: String? = null,
    val rate_id: String? = null,
    val hora_entrada: String = "",
    val hora_salida: String? = null,
    val total: Double? = null,
    val pagado: Boolean? = false,
    val created_at: String? = null,
    val tipo: String = "entrada",
    val estado: String = "pendiente",
    val created_by_user_id: String? = null,
    val fotos_entrada: List<String> = emptyList(),  // 🔥 Múltiples fotos entrada
    val fotos_salida: List<String> = emptyList(),   // 🔥 Múltiples fotos salida
    val fotos: List<String> = emptyList()           // Mantener compatibilidad
)

@Serializable
data class ParkingActividad(
    val id: String? = null,
    val tipo: String? = null,
    val hora_entrada: String? = null,
    val hora_salida: String? = null,
    val vehicles: VehiclePlate? = null
)

data class ParkingTicket(
    val plate: String,
    val horaEntrada: String,
    val fotos: List<String>,
    val garage: String,
    val parkingId: String
)

@Serializable
data class VehiclePlate(
    val plate: String? = null
)



