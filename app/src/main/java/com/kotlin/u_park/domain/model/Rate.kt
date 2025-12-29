package com.kotlin.u_park.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Rate(
    val id: String? = null,

    @SerialName("garage_id")
    val garageId: String,

    @SerialName("vehicle_type_id")
    val vehicleTypeId: Int? = null,

    @SerialName("base_rate")
    val baseRate: Double,

    @SerialName("time_unit")
    val timeUnit: String, // "hora", "día", "semana", "mes"

    @SerialName("hora_inicio")
    val horaInicio: String? = null, // "HH:MM:SS" o null

    @SerialName("hora_fin")
    val horaFin: String? = null,

    @SerialName("dias_aplicables")
    val diasAplicables: List<String> = listOf(
        "lunes", "martes", "miércoles",
        "jueves", "viernes", "sábado", "domingo"
    ),

    @SerialName("special_rate")
    val specialRate: Double? = null,

    val active: Boolean = true,

    @SerialName("start_date")
    val startDate: String? = null, // "YYYY-MM-DD"

    @SerialName("end_date")
    val endDate: String? = null
)
