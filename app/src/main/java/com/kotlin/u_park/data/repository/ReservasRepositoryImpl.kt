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

    override suspend fun crearReserva(reserva: Reserva): Reserva {
        val data = mapOf(
            "garage_id" to reserva.garage_id,
            "vehicle_id" to reserva.vehicle_id,
            "hora_reserva" to reserva.hora_reserva,
            "estado" to reserva.estado
        )

        val res = table.insert(data) { select() }
        return res.decodeSingle()
    }


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


    override suspend fun cancelarReserva(id: String): Boolean {
        table.update(mapOf("estado" to "cancelada")) {
            filter { eq("id", id) }
        }
        return true
    }

    override suspend fun actualizarEmpleadoReserva(id: String, empleadoId: String) {
        client.from("reservas").update(
            mapOf("empleado_id" to empleadoId)
        ) {
            filter { eq("id", id) }
        }
    }


    override suspend fun listarReservasPorGarage(garageId: String): List<Reserva> {
        val res = table.select { filter { eq("garage_id", garageId) } }
        return res.decodeList()
    }
}
