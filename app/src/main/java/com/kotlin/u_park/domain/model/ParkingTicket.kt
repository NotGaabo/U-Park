package com.kotlin.u_park.domain.model

data class ParkingTicket(
    val plate: String,
    val horaEntrada: String,
    val fotos: List<String>,
    val garage: String,
    val parkingId: String
)
