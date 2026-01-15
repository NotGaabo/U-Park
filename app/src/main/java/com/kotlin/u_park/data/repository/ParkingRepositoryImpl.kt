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
import com.kotlin.u_park.domain.model.ParkingRecordDTO
import com.kotlin.u_park.domain.model.UserSimpleDto
import com.kotlin.u_park.presentation.screens.employee.ParkingRecord
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement

val json = Json { ignoreUnknownKeys = true }

fun JsonElement?.toUserDTO(): UserSimpleDto? {
    if (this == null || this is kotlinx.serialization.json.JsonNull) return null
    return json.decodeFromJsonElement<UserSimpleDto>(this)
}

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
        return try {
            val result = client.postgrest.rpc(
                "historial_parking_usuario",
                mapOf("p_user_id" to userId)
            ).decodeList<HistorialParking>()

            println("✅ RPC historial_parking_usuario OK → ${result.size} registros")
            result.forEach {
                println("📄 parking= estado=${it.estado}")
            }

            result
        } catch (e: Exception) {
            println("❌ RPC historial_parking_usuario FALLÓ: ${e.message}")
            emptyList()
        }
    }


    // ------------------------------------------------------------
    // 🔵 3. REGISTRAR ENTRADA NORMAL
    // ------------------------------------------------------------

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
    // 🔥 REGISTRAR ENTRADA CON MÚLTIPLES FOTOS
    // ------------------------------------------------------------
    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun registrarEntrada(
        parking: Parking,
        fotosBytes: List<ByteArray>
    ): Parking {

        val urls = fotosBytes.mapIndexed { i, foto ->
            val path = "parking/entrada_${parking.vehicle_id}_${System.currentTimeMillis()}_$i.jpg"
            client.storage.from("parking_photos").upload(path, foto)
            client.storage.from("parking_photos").publicUrl(path)
        }

        val body = parking.copy(
            fotos_entrada = urls,
            fotos = emptyList() // compatibilidad
        )

        return table.insert(body) {
            select()
        }.decodeSingle()
    }


    // ------------------------------------------------------------
    // 🔥 REGISTRAR SALIDA CON MÚLTIPLES FOTOS Y PAGO
    // ------------------------------------------------------------
    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun registrarSalidaConPago(
        parkingId: String,
        horaSalida: String,
        empleadoId: String,
        metodoPago: String,
        fotosSalidaBytes: List<ByteArray>,   // 🔥 muchas fotos del vehículo
        comprobanteBytes: ByteArray?         // 🔥 solo 1
    ): Parking {

        // 1️⃣ Subir fotos del vehículo
        val fotosSalidaUrls = fotosSalidaBytes.mapIndexed { i, foto ->
            val path = "parking/salida_${parkingId}_${System.currentTimeMillis()}_$i.jpg"
            client.storage.from("parking_photos").upload(path, foto)
            client.storage.from("parking_photos").publicUrl(path)
        }

        // 2️⃣ Subir comprobante (si es transferencia)
        var comprobanteUrl: String? = null
        if (metodoPago == "TRANSFERENCIA") {
            if (comprobanteBytes == null) {
                throw IllegalArgumentException("La transferencia requiere comprobante")
            }

            val fileName = "payments/comp_${parkingId}_${System.currentTimeMillis()}.jpg"
            client.storage.from("parking_payments").upload(fileName, comprobanteBytes) {
                upsert = true
            }
            comprobanteUrl = client.storage.from("parking_payments").publicUrl(fileName)
        }

        // 3️⃣ RPC REAL (calcula, cobra, valida)
        val updated = client.postgrest.rpc(
            "registrar_salida_con_pago",
            mapOf(
                "p_parking_id" to parkingId,
                "p_hora_salida" to horaSalida,
                "p_empleado_id" to empleadoId,
                "p_metodo" to metodoPago,
                "p_comprobante_url" to comprobanteUrl
            )
        ).decodeList<Parking>().first()

        // 4️⃣ Guardar las fotos del vehículo
        table.update(
            mapOf("fotos_salida" to fotosSalidaUrls)
        ) {
            filter { eq("id", parkingId) }
        }

        // 5️⃣ Si era reserva → cerrarla
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

        return updated.copy(fotos_salida = fotosSalidaUrls)
    }

    // En getParkingRecords y getParkingRecordById, agrega es_incidencia al select:

    override suspend fun getParkingRecords(garageId: String): Result<List<ParkingRecord>> {
        return try {
            val response = client.from("parkings")
                .select(
                    Columns.raw(
                        """
                    id,
                    garage_id,
                    vehicle_id,
                    rate_id,
                    hora_entrada,
                    hora_salida,
                    total,
                    pagado,
                    created_at,
                    tipo,
                    estado,
                    created_by_user_id,
                    fotos_entrada,
                    fotos_salida,
                    es_incidencia,

                    vehicles:vehicle_id (
                        plate
                    ),

                    users:created_by_user_id (
                        nombre
                    )
                    """.trimIndent()
                    )
                ) {
                    filter { eq("garage_id", garageId) }
                    order("hora_entrada", Order.DESCENDING)
                }
                .decodeList<ParkingRecordDTO>()

            val records = response.map { dto ->
                ParkingRecord(
                    id = dto.id,
                    vehicleId = dto.vehicleId,
                    garageId = dto.garageId,
                    rateId = dto.rateId,
                    horaEntrada = dto.horaEntrada,
                    horaSalida = dto.horaSalida,
                    total = dto.total,
                    pagado = dto.pagado,
                    createdAt = dto.createdAt,
                    tipo = dto.tipo,
                    estado = dto.estado,
                    createdByUserId = dto.createdByUserId,
                    fotosEntrada = dto.fotosEntrada,
                    fotosSalida = dto.fotosSalida,
                    vehiclePlate = dto.vehicles?.plate,
                    employeeName = dto.users?.nombre,
                    esIncidencia = dto.esIncidencia  // 🔥 NUEVO
                )
            }

            Result.success(records)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getParkingRecordById(recordId: String): Result<ParkingRecord> {
        return try {
            val response = client.from("parkings")
                .select(
                    Columns.raw(
                        """
                    id,
                    garage_id,
                    vehicle_id,
                    rate_id,
                    hora_entrada,
                    hora_salida,
                    total,
                    pagado,
                    created_at,
                    tipo,
                    estado,
                    created_by_user_id,
                    fotos_entrada,
                    fotos_salida,

                    vehicles:vehicle_id (
                        plate
                    ),

                    users:created_by_user_id (
                        nombre
                    )
                    """.trimIndent()
                    )
                ) {
                    filter { eq("id", recordId) }
                    limit(1)
                }
                .decodeSingle<ParkingRecordDTO>()

            val record = ParkingRecord(
                id = response.id,
                vehicleId = response.vehicleId,
                garageId = response.garageId,
                rateId = response.rateId,
                horaEntrada = response.horaEntrada,
                horaSalida = response.horaSalida,
                total = response.total,
                pagado = response.pagado,
                createdAt = response.createdAt,
                tipo = response.tipo,
                estado = response.estado,
                createdByUserId = response.createdByUserId,
                fotosEntrada = response.fotosEntrada,
                fotosSalida = response.fotosSalida,
                vehiclePlate = response.vehicles?.plate,
                employeeName = response.users?.nombre
            )

            Result.success(record)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    // ------------------------------------------------------------
    // REGISTRAR SALIDA SIN PAGO (para compatibilidad)
    // ------------------------------------------------------------
    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun registrarSalida(
        parkingId: String,
        horaSalida: String,
        empleadoId: String
    ): Parking {

        val updated = table.update(
            mapOf(
                "hora_salida" to horaSalida,
                "estado" to "completada",
                "created_by_user_id" to empleadoId
            )
        ) {
            filter { eq("id", parkingId) }
            select()
        }.decodeSingle<Parking>()

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

    override suspend fun MarkasIncident(parkingId: String, esIncidencia: Boolean): Boolean {
        return try {
            client.from("parkings").update(
                mapOf("es_incidencia" to esIncidencia)
            ) {
                filter { eq("id", parkingId) }
            }
            true
        } catch (e: Exception) {
            println("❌ Error marcando incidencia: ${e.message}")
            false
        }
    }

    // ------------------------------------------------------------
    // 🔥 ENTRADA DESDE RESERVA CON MÚLTIPLES FOTOS
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
            created_by_user_id = empleadoId,
            hora_entrada = hora,
            tipo = "reserva",
            estado = "activa",
            fotos = emptyList(),
            fotos_entrada = emptyList()
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
    // ACTIVIDAD RECIENTE
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
}