package com.kotlin.u_park.data.repository

import android.net.Uri
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
import io.github.jan.supabase.storage.upload
import java.io.File
import java.time.OffsetDateTime
import androidx.core.net.toUri
import com.kotlin.u_park.data.remote.supabase
import com.kotlin.u_park.domain.model.ParkingPago

class ParkingRepositoryImpl(
    private val client: SupabaseClient
) : ParkingRepository {

    private val table = client.from("parkings")

    @Serializable
    data class VehicleSimple(val id: String)

    // ------------------------------------------------------------
    // 🔵 1. OBTENER UUID DESDE LA PLACA
    // ------------------------------------------------------------
    override suspend fun getVehicleIdByPlate(plate: String): String? {
        val result = client.from("vehicles")
            .select {
                filter { eq("plate", plate) }
                limit(1)
            }
            .decodeList<VehicleSimple>()

        return result.firstOrNull()?.id
    }

    // ------------------------------------------------------------
    // 🔵 2. HISTORIAL USUARIO
    // ------------------------------------------------------------
    override suspend fun getHistorialByUser(userId: String): List<HistorialParking> {
        return client.postgrest.rpc(
            "historial_parking_usuario",
            mapOf("p_user_id" to userId)
        ).decodeList<HistorialParking>()
    }

    // ------------------------------------------------------------
    // 🔵 3. REGISTRAR ENTRADA NORMAL
    // ------------------------------------------------------------
    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun registrarEntrada(
        parking: Parking,
        fotosBytes: List<ByteArray>
    ): Parking {

        val urls = fotosBytes.mapIndexed { i, foto ->
            val path = "parking/${parking.vehicle_id}_${System.currentTimeMillis()}_$i.jpg"
            client.storage.from("parking_photos").upload(path, foto)
            client.storage.from("parking_photos").publicUrl(path)
        }

        val body = parking.copy(fotos = urls)

        return table.insert(body) {
            select() // ← devuelve TODAS las columnas, incluyendo created_by_user_id
        }.decodeSingle()
    }

    // ------------------------------------------------------------
    // 🔵 4. ¿EL VEHÍCULO ESTÁ ACTUALMENTE DENTRO?
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


    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun registrarSalidaConPago(
        parkingId: String,
        horaSalida: String,
        empleadoId: String,
        metodoPago: String,
        comprobanteBytes: ByteArray?
    ): Parking {

        var comprobanteUrlFinal: String? = null

        // 1️⃣ Subir comprobante si es transferencia
        if (metodoPago == "TRANSFERENCIA") {
            if (comprobanteBytes == null) {
                throw IllegalArgumentException("La transferencia requiere comprobante")
            }

            val fileName = "payments/$parkingId-${System.currentTimeMillis()}.jpg"

            client.storage
                .from("parking_payments")
                .upload(fileName, comprobanteBytes) {
                    upsert = true
                }

            comprobanteUrlFinal = client.storage
                .from("parking_payments")
                .publicUrl(fileName)
        }

        // 2️⃣ Ejecutar RPC (TRANSACCIÓN REAL)
        val updated = client.postgrest.rpc(
            "registrar_salida_con_pago",
            mapOf(
                "p_parking_id" to parkingId,
                "p_hora_salida" to horaSalida,
                "p_empleado_id" to empleadoId,
                "p_metodo" to metodoPago,
                "p_comprobante_url" to comprobanteUrlFinal
            )
        ).decodeList<Parking>().first()

        // 3️⃣ Si venía de reserva → completar reserva
        if (updated.tipo == "reserva") {
            client.from("reservas").update(
                mapOf("estado" to "completada")
            ) {
                filter {
                    eq("vehicle_id", updated.vehicle_id!!)
                    eq("garage_id", updated.garage_id!!)
                    neq("estado", "completada")
                }
            }
        }

        return updated
    }

    // ------------------------------------------------------------
    // 🔥 5. REGISTRAR SALIDA (VERSIÓN PERFECTA)
    // ------------------------------------------------------------
    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun registrarSalida(
        parkingId: String,
        horaSalida: String,
        empleadoId: String               // ⬅ lo añadí porque lo necesitas
    ): Parking {

        // 1. UPDATE perfecto con created_by_user_id
        val updated = table.update(
            mapOf(
                "hora_salida" to horaSalida,
                "estado" to "completada",
                "created_by_user_id" to empleadoId   // ⬅ AQUÍ ESTÁ LO IMPORTANTE
            )
        ) {
            filter { eq("id", parkingId) }
            select()   // ← DEVUELVE TODO CORRECTAMENTE
        }.decodeSingle<Parking>()

        // 2. Si venía desde reserva → completar reserva
        if (updated.tipo == "reserva") {
            client.from("reservas").update(
                mapOf("estado" to "completada")
            ) {
                filter {
                    eq("vehicle_id", updated.vehicle_id!!)
                    eq("garage_id", updated.garage_id!!)
                    neq("estado", "completada")
                }
            }
        }

        return updated
    }


    // ------------------------------------------------------------
    // 🔵 6. OBTENER PARKING POR ID
    // ------------------------------------------------------------
    override suspend fun getParkingById(id: String): Parking? {
        return table.select {
            filter { eq("id", id) }
            limit(1)
        }.decodeList<Parking>().firstOrNull()
    }

    // ------------------------------------------------------------
    // 🔵 7. CREAR RESERVA
    // ------------------------------------------------------------
    override suspend fun crearReserva(parking: Parking): Parking {
        val data = parking.copy(
            tipo = "reserva",
            estado = "pendiente"
        )
        return table.insert(data) { select() }.decodeSingle()
    }

    private suspend fun subirComprobanteTransferencia(
        parkingId: String,
        comprobanteBytes: ByteArray
    ): String {

        val fileName = "payments/$parkingId-${System.currentTimeMillis()}.jpg"

        client.storage
            .from("parking_payments")
            .upload(
                path = fileName,
                data = comprobanteBytes
            ) {
                upsert = true
            }

        return client.storage
            .from("parking_payments")
            .publicUrl(fileName)
    }



    // ------------------------------------------------------------
    // 🔵 8. VEHÍCULOS DENTRO
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
            }.decodeList()
    }

    // ------------------------------------------------------------
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

    // ------------------------------------------------------------
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
        return try {
            println("🔍 DEBUG: Buscando reservas para garage: $garageId")

            val result = client.from("reservas").select(
                Columns.raw("""
        id,
        garage_id,
        vehicle_id,
        empleado_id,
        hora_reserva,
        hora_llegada,
        estado,

        vehicles:vehicle_id (
            plate,
            user_id
        ),

        users:vehicles(user_id) (
            id,
            nombre,
            usuario,
            cedula,
            telefono,
            correo
        )
    """.trimIndent())
            ) {
                filter {
                    eq("garage_id", garageId)
                    eq("estado", "pendiente")
                }
                order("hora_reserva", Order.ASCENDING)
            }.decodeList<ReservaConUsuario>()

            println("✅ DEBUG: ${result.size} reservas encontradas")
            result.forEach { reserva ->
                println("   📋 ID: ${reserva.id}")
                println("      Placa: ${reserva.vehicles?.plate}")
                println("      Usuario: ${reserva.users?.nombre}")
                println("      Hora: ${reserva.hora_reserva}")
            }

            result

        } catch (e: Exception) {
            println("❌ ERROR en getReservasConUsuario: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    // ------------------------------------------------------------
    // 🔵 13. ENTRADA DESDE RESERVA
    // ------------------------------------------------------------
    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun registrarEntradaDesdeReserva(
        reserva: ReservaConUsuario,
        fotosBytes: List<ByteArray>,
        empleadoId: String
    ): Parking {

        val hora = OffsetDateTime.now().toString()

        val vehicleId = reserva.vehicle_id
            ?: throw IllegalArgumentException("Reserva sin vehículo")

        val parking = Parking(
            id = null,
            garage_id = reserva.garage_id,
            vehicle_id = vehicleId,
            created_by_user_id = empleadoId, // ✔ se envía correctamente
            hora_entrada = hora,
            tipo = "reserva",
            estado = "activa",
            fotos = emptyList()
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
    override suspend fun cancelarReserva(reservaId: String): Boolean {
        client.from("reservas").update(
            mapOf("estado" to "cancelada")
        ) {
            filter { eq("id", reservaId) }
        }
        return true
    }

    override suspend fun activarReserva(reservaId: String): Parking {
        return client.from("reservas").update(
            mapOf("estado" to "activa")
        ) {
            filter { eq("id", reservaId) }
            select()
        }.decodeSingle()
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
            }.decodeList()
    }
}
