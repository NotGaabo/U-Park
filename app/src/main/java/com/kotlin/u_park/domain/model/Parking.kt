package com.kotlin.u_park.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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


@kotlinx.serialization.Serializable
data class ParkingRecordDTO(
    @SerialName("id") val id: String,
    @SerialName("vehicle_id") val vehicleId: String?,
    @SerialName("garage_id") val garageId: String?,
    @SerialName("rate_id") val rateId: String?,
    @SerialName("hora_entrada") val horaEntrada: String,
    @SerialName("hora_salida") val horaSalida: String?,
    @SerialName("total") val total: Double?,
    @SerialName("pagado") val pagado: Boolean?,
    @SerialName("created_at") val createdAt: String?,
    @SerialName("tipo") val tipo: String?,
    @SerialName("estado") val estado: String?,
    @SerialName("created_by_user_id") val createdByUserId: String?,
    @SerialName("fotos_entrada") val fotosEntrada: List<String>,
    @SerialName("fotos_salida") val fotosSalida: List<String>,
    @SerialName("es_incidencia") val esIncidencia: Boolean? = false,
    // Datos de joins
    @SerialName("vehicles") val vehicles: VehicleDTO?,
    @SerialName("users")
    val users: UserSimpleDto?

)

@kotlinx.serialization.Serializable
data class VehicleDTO(
    @SerialName("plate") val plate: String?
)

@Serializable
data class UserSimpleDto(
    val nombre: String?
)