package com.kotlin.u_park.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.kotlin.u_park.domain.model.Parking
import com.kotlin.u_park.domain.model.ParkingActividad
import com.kotlin.u_park.domain.model.ReservaConUsuario
import com.kotlin.u_park.domain.repository.ParkingRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.query.Columns

import io.github.jan.supabase.storage.storage
import java.time.OffsetDateTime

class ParkingRepositoryImpl(
    private val client: SupabaseClient
) : ParkingRepository {

    private val table = client.from("parkings")

    // ------------------------------------------------------------
    // REGISTRAR ENTRADA
    // ------------------------------------------------------------
    override suspend fun registrarEntrada(parking: Parking, fotosBytes: List<ByteArray>): Parking {

        val urls = fotosBytes.mapIndexed { i, foto ->
            val path = "parking/${parking.vehicle_id}_${System.currentTimeMillis()}_$i.jpg"
            client.storage.from("parking_photos").upload(path, foto)
            client.storage.from("parking_photos").publicUrl(path)
        }

        val body = parking.copy(fotos = urls)

        return table.insert(body) { select() }.decodeSingle()
    }

    // ------------------------------------------------------------
    override suspend fun estaVehiculoDentro(vehicleId: String): Boolean {
        return table.select {
            filter {
                eq("vehicle_id", vehicleId)
                eq("estado", "activa")
            }
        }.decodeList<Parking>().isNotEmpty()
    }

    // ------------------------------------------------------------
    override suspend fun registrarSalida(parkingId: String, horaSalida: String): Parking {
        val update = mapOf(
            "hora_salida" to horaSalida,
            "estado" to "completada"
        )

        return table.update(update) {
            filter { eq("id", parkingId) }
            select()
        }.decodeSingle()
    }

    // ------------------------------------------------------------
    override suspend fun getParkingById(id: String): Parking? {
        return table.select {
            filter { eq("id", id) }
        }.decodeSingleOrNull()
    }

    // ------------------------------------------------------------
    override suspend fun crearReserva(parking: Parking): Parking {
        val data = parking.copy(tipo = "reserva", estado = "pendiente")
        return table.insert(data) { select() }.decodeSingle()
    }

    // ------------------------------------------------------------
    override suspend fun getVehiculosDentro(): List<Parking> {
        return table.select {
            filter { eq("estado", "activa") }
        }.decodeList()
    }

    override suspend fun getVehiculosFuera(): List<Parking> {
        return table.select {
            filter { eq("estado", "completada") }
        }.decodeList()
    }

    // ------------------------------------------------------------
    override suspend fun getReservas(): List<Parking> {
        return table.select {
            filter { eq("tipo", "reserva") }
        }.decodeList()
    }

    override suspend fun getReservasByGarage(garageId: String): List<Parking> {
        return table.select {
            filter {
                eq("garage_id", garageId)
                eq("tipo", "reserva")
            }
        }.decodeList()
    }

    // ------------------------------------------------------------
    override suspend fun getReservasConUsuario(garageId: String): List<ReservaConUsuario> {

        return client.from("reservas").select(
            Columns.raw(
                """
                *,
                vehicles (
                    plate,
                    users (nombre)
                )
                """
            )
        ) {
            filter { eq("garage_id", garageId) }
        }.decodeList()
    }

    // ------------------------------------------------------------
    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun registrarEntradaDesdeReserva(
        reserva: ReservaConUsuario,
        fotosBytes: List<ByteArray>,
        empleadoId: String
    ): Parking {

        val hora = OffsetDateTime.now().toString()

        val parking = Parking(
            id = null,
            garage_id = reserva.garage_id,
            vehicle_id = reserva.vehicle_id!!,
            created_by_user_id = empleadoId,
            hora_entrada = hora,
            tipo = "entrada",
            estado = "activa"
        )

        val creado = registrarEntrada(parking, fotosBytes)

        client.from("reservas").update(
            mapOf(
                "estado" to "activa",
                "hora_llegada" to hora,
                "empleado_id" to empleadoId
            )
        ) {
            filter { eq("id", reserva.id!!) }
        }

        return creado
    }

    // ------------------------------------------------------------
    override suspend fun activarReserva(reservaId: Int): Parking {
        val update = mapOf(
            "estado" to "activa"
        )

        return table.update(update) {
            filter { eq("id", reservaId) }
            select()
        }.decodeSingle()
    }

    override suspend fun cancelarReserva(reservaId: Int): Boolean {
        table.update(mapOf("estado" to "cancelada")) {
            filter { eq("id", reservaId) }
        }
        return true
    }

    // ------------------------------------------------------------
    override suspend fun getActividadReciente(garageId: String): List<ParkingActividad> {
        return client
            .from("parkings")
            .select(
                Columns.raw(
                    """
                id,
                tipo,
                hora_entrada,
                hora_salida,
                vehicles:vehicle_id (plate)
                """.trimIndent()
                )
            ) {
                filter { eq("garage_id", garageId) }

                order(
                    column = "hora_entrada",
                    order = Order.DESCENDING
                )

                limit(20)
            }
            .decodeList()
    }
}
