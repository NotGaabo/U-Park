package com.kotlin.u_park.presentation.utils

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

fun formatHora(fecha: String?): String {
    if (fecha == null) return "—"

    return try {
        val date = OffsetDateTime.parse(fecha)
        date.format(
            DateTimeFormatter.ofPattern(
                "dd MMM yyyy • hh:mm a",
                Locale("es", "ES")
            )
        )
    } catch (e: Exception) {
        fecha
    }
}
