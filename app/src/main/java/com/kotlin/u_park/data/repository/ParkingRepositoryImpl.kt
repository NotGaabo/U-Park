package com.kotlin.u_park.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.kotlin.u_park.domain.model.Parking
import com.kotlin.u_park.domain.repository.ParkingRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage

class ParkingRepositoryImpl(
    private val client: SupabaseClient
) : ParkingRepository {

    private val table = client.from("parkings")

    // ----------------------------------------------------
    // 1. Registrar ENTRADA
    // ----------------------------------------------------
    override suspend fun registrarEntrada(
        parking: Parking,
        fotosBytes: List<ByteArray>
    ): Parking {

        // 1. Subir fotos al bucket "parking_photos"
        val urls = fotosBytes.mapIndexed { index, foto ->
            val path = "parking/${parking.vehicleId}_${System.currentTimeMillis()}_$index.jpg"

            client.storage.from("parking_photos").upload(
                path = path,
                data = foto
            )

            // URL pública de la imagen
            client.storage.from("parking_photos").publicUrl(path)
        }

        // 2. Copiar registro con las urls
        val registro = parking.copy(fotos = urls)

        // 3. Insertar en Supabase
        val res = table.insert(registro) {
            select() // necesario para decodeSingle
        }

        return res.decodeSingle()
    }

    // ----------------------------------------------------
    // 2. Registrar SALIDA
    // ----------------------------------------------------
    override suspend fun registrarSalida(parkingId: Int, horaSalida: String): Parking {

        val update = mapOf(
            "hora_salida" to horaSalida,
            "estado" to "completada"
        )

        val res = table.update(update) {
            // Filtro según ID
            filter {
                eq("id", parkingId)
            }
            select()
        }

        return res.decodeSingle()
    }

    // ----------------------------------------------------
    // 3. Obtener Parking por ID
    // ----------------------------------------------------
    override suspend fun getParkingById(id: Int): Parking? {
        val res = table.select {
            filter {
                eq("id", id)
            }
        }

        return res.decodeSingleOrNull()
    }

    // ----------------------------------------------------
    // 4. Crear RESERVA
    // ----------------------------------------------------
    override suspend fun crearReserva(
        parking: Parking
    ): Parking {

        val data = parking.copy(
            tipo = "reserva",
            estado = "pendiente"
        )

        val res = table.insert(data) {
            select()
        }

        return res.decodeSingle()
    }

    // ----------------------------------------------------
    // 5. Vehículos dentro / fuera (desactivado por ahora)
    // Los dejamos para después, como pediste.
    // ----------------------------------------------------
    override suspend fun getVehiculosDentro(): List<Parking> {
        return emptyList() // TEMPORAL
    }

    override suspend fun getVehiculosFuera(): List<Parking> {
        return emptyList() // TEMPORAL
    }

    // ----------------------------------------------------
// 6. Reservas por GARAGE
// ----------------------------------------------------
    override suspend fun getReservasByGarage(garageId: String): List<Parking> {
        val res = table.select {
            filter {
                eq("garage_id", garageId)
                eq("tipo", "reserva")
                eq("estado", "pendiente")
            }
        }
        return res.decodeList()
    }

    // ----------------------------------------------------
// 7. Activar reserva → convertir a entrada real
// ----------------------------------------------------
    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun activarReserva(reservaId: Int): Parking {

        val update = mapOf(
            "tipo" to "entrada",
            "estado" to "activa",
            "hora_entrada" to java.time.OffsetDateTime.now().toString()
        )

        val res = table.update(update) {
            filter { eq("id", reservaId) }
            select()
        }

        return res.decodeSingle()
    }

    // ----------------------------------------------------
// 8. Cancelar reserva
// ----------------------------------------------------
    override suspend fun cancelarReserva(reservaId: Int): Boolean {
        table.update(
            mapOf("estado" to "cancelada")
        ) {
            filter { eq("id", reservaId) }
        }
        return true
    }


    // ----------------------------------------------------
    // 6. Lista de RESERVAS
    // ----------------------------------------------------
    override suspend fun getReservas(): List<Parking> {
        val res = table.select {
            filter {
                eq("tipo", "reserva")
            }
        }
        return res.decodeList()
    }
}
