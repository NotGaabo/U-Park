package com.kotlin.u_park.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Rate(
    val id: String,
    val garage_id: String?,
    val vehicle_type_id: Int?,
    val base_rate: Double,
    val time_unit: String?, // "hora", "día", "semana", "mes"
    val hora_inicio: String?,
    val hora_fin: String?,
    val dias_aplicables: List<String>?,
    val special_rate: Double?,
    val active: Boolean?,
    val start_date: String?,
    val end_date: String?,
    val created_at: String?
)
