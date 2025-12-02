package com.kotlin.u_park.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.kotlin.u_park.domain.model.Parking
import com.kotlin.u_park.domain.model.ParkingActividad
import com.kotlin.u_park.domain.model.ReservaConUsuario
import com.kotlin.u_park.domain.repository.ParkingRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.postgrest.rpc
import kotlinx.serialization.Serializable
import com.kotlin.u_park.domain.model.HistorialParking
import io.github.jan.supabase.postgrest.postgrest
import java.time.OffsetDateTime

class ParkingRepositoryImpl(
    private val client: SupabaseClient
) : ParkingRepository {

    private val table = client.from("parkings")

    @Serializable
    data class VehicleSimple(val id: String)

    // ------------------------------------------------------------
    // 🔵 OBTENER UUID DESDE LA PLACA
    // ------------------------------------------------------------
    override suspend fun getVehicleIdByPlate(plate: String): String? {
        println("🔍 Buscando vehículo por placa = $plate")

        val result = client.from("vehicles")
            .select {
                filter { eq("plate", plate) }
                limit(1)
            }
            .decodeList<VehicleSimple>()

        println("🔍 Resultado búsqueda placa = $result")

        return result.firstOrNull()?.id
    }

    // ------------------------------------------------------------
    // 🔴 HISTORIAL USUARIO
    // ------------------------------------------------------------

    override suspend fun getHistorialByUser(userId: String): List<HistorialParking> {
        return client.postgrest.rpc(
            "historial_parking_usuario",
            mapOf("p_user_id" to userId)
        ).decodeList()
    }



    // ------------------------------------------------------------
    // 🔴 REGISTRAR ENTRADA NORMAL (ya recibe UUID)
    // ------------------------------------------------------------
    override suspend fun registrarEntrada(
        parking: Parking,
        fotosBytes: List<ByteArray>
    ): Parking {
        // Subir fotos y generar URLs públicas
        val urls = fotosBytes.mapIndexed { i, foto ->
            val path = "parking/${parking.vehicle_id}_${System.currentTimeMillis()}_$i.jpg"
            client.storage.from("parking_photos").upload(path, foto)
            client.storage.from("parking_photos").publicUrl(path)
        }

        val body = parking.copy(fotos = urls)

        return table.insert(body) {
            select()
        }.decodeSingle()
    }

    // ------------------------------------------------------------
    // ¿EL VEHÍCULO TIENE ALGÚN PARKING ACTIVO?
    // ------------------------------------------------------------
    override suspend fun estaVehiculoDentro(vehicleId: String): Boolean {
        val list = table.select {
            filter {
                eq("vehicle_id", vehicleId)
                eq("estado", "activa")
            }
        }.decodeList<Parking>()

        return list.isNotEmpty()
    }

    // ------------------------------------------------------------
    // REGISTRAR SALIDA
    // ------------------------------------------------------------
    override suspend fun registrarSalida(
        parkingId: String,
        horaSalida: String
    ): Parking {

        // Obtener el parking actual (si no existe, lanzamos error claro)
        val parking = table.select {
            filter { eq("id", parkingId) }
            limit(1)
        }.decodeSingle<Parking>()

        // Actualizar hora_salida y estado
        val updatedParking = table.update(
            mapOf(
                "hora_salida" to horaSalida,
                "estado" to "completada"
            )
        ) {
            filter { eq("id", parkingId) }
            select()
        }.decodeSingle<Parking>()

        // Si este parking viene de una reserva, marcamos la reserva como completada
        if (parking.tipo == "reserva") {
            client.from("reservas").update(
                mapOf("estado" to "completada")
            ) {
                filter {
                    eq("vehicle_id", parking.vehicle_id!!)
                    eq("garage_id", parking.garage_id!!)
                    neq("estado", "completada")
                }
            }
        }

        return updatedParking
    }

    // ------------------------------------------------------------
    override suspend fun getParkingById(id: String): Parking? {
        return table.select {
            filter { eq("id", id) }
            limit(1)
        }.decodeList<Parking>()
            .firstOrNull()
    }

    // ------------------------------------------------------------
    override suspend fun crearReserva(parking: Parking): Parking {
        val data = parking.copy(
            tipo = "reserva",
            estado = "pendiente"
        )
        return table.insert(data) { select() }.decodeSingle()
    }

    // ------------------------------------------------------------
    override suspend fun getVehiculosDentro(): List<ParkingActividad> {
        return client.from("parkings")
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
                filter { eq("estado", "activa") }
            }
            .decodeList()
    }

    override suspend fun getVehiculosFuera(): List<Parking> {
        return table.select {
            filter { eq("estado", "completada") }
        }.decodeList()
    }

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

    override suspend fun getReservasConUsuario(garageId: String): List<ReservaConUsuario> {
        return client.from("reservas").select(
            Columns.raw(
                """
                *,
                vehicles (
                    plate,
                    users (nombre)
                )
                """.trimIndent()
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

        // Hora actual ISO
        val hora = OffsetDateTime.now().toString()

        // 🔥 Validación fuerte: vehicle_id debe ser UUID válido
        val vehicleId = reserva.vehicle_id ?: throw IllegalArgumentException(
            "La reserva no contiene un vehicle_id válido"
        )

        // Crear el registro Parking
        val parking = Parking(
            id = null,
            garage_id = reserva.garage_id,
            vehicle_id = vehicleId,  // ← siempre UUID
            created_by_user_id = empleadoId,
            hora_entrada = hora,
            tipo = "reserva",
            estado = "activa",
            fotos = emptyList()
        )

        // Registrar entrada (sube fotos, crea URLs, inserta en parkings)
        val creado = registrarEntrada(parking, fotosBytes)

        // Actualizar la reserva original
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
        val update = mapOf("estado" to "activa")

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
        return client.from("parkings")
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
                order("hora_entrada", Order.DESCENDING)
                limit(20)
            }
            .decodeList()
    }
}
