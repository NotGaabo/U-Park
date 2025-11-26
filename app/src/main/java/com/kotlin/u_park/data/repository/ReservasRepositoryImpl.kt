package com.kotlin.u_park.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.kotlin.u_park.domain.model.Parking
import com.kotlin.u_park.domain.model.Reserva
import com.kotlin.u_park.domain.repository.ReservasRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from

class ReservasRepositoryImpl(
    private val client: SupabaseClient
) : ReservasRepository {

    private val table = client.from("reservas")

    // -------------------------------------------
    // CREAR RESERVA
    // -------------------------------------------
    override suspend fun crearReserva(reserva: Parking): Reserva {

        val data = mapOf(
            "garage_id" to reserva.garageId,
            "vehicle_id" to reserva.vehicleId,
            "hora_reserva" to reserva.horaEntrada,
            "estado" to reserva.estado
        )

        val res = table.insert(data) {
            select()   // devuelve la fila creada
        }

        return res.decodeSingle()
    }

    // -------------------------------------------
    // ACTIVAR RESERVA
    // -------------------------------------------
    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun activarReserva(id: String): Reserva {

        val data = mapOf(
            "estado" to "activa",
            "hora_llegada" to java.time.OffsetDateTime.now().toString()
        )

        val res = table.update(data) {
            filter { eq("id", id) }
            select()
        }

        return res.decodeSingle()
    }

    // -------------------------------------------
    // CANCELAR RESERVA
    // -------------------------------------------
    override suspend fun cancelarReserva(id: String): Boolean {
        table.update(
            mapOf("estado" to "cancelada")
        ) {
            filter { eq("id", id) }
        }
        return true
    }

    // -------------------------------------------
    // LISTAR RESERVAS POR GARAGE
    // -------------------------------------------
    override suspend fun listarReservasPorGarage(garageId: String): List<Reserva> {

        val res = table.select {
            filter {
                eq("garage_id", garageId)
            }
        }

        return res.decodeList()
    }
}
