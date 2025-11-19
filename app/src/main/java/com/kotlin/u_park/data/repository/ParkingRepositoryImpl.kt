package com.kotlin.u_park.data.repository

import com.kotlin.u_park.domain.model.Parking
import com.kotlin.u_park.domain.repository.ParkingRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage

class ParkingRepositoryImpl(
    private val client: SupabaseClient
) : ParkingRepository {

    override suspend fun registrarEntrada(
        parking: Parking,
        fotosBytes: List<ByteArray>
    ): Parking {

        // ---- 1. Subir fotos ----
        val urls = fotosBytes.mapIndexed { index, foto ->
            val path = "parking/${parking.vehicleId}_${System.currentTimeMillis()}_$index.jpg"

            client.storage.from("parking_photos").upload(path, foto)
            client.storage.from("parking_photos").publicUrl(path)
        }

        val registro = parking.copy(fotos = urls)

        // ---- 2. Insertar con select ----
        val res = client.from("parking")
            .insert(registro) {
                select()
            }

        return res.decodeSingle()
    }

    override suspend fun registrarSalida(parkingId: Int, horaSalida: String): Parking {

        val res = client.from("parking").update(
            mapOf("hora_salida" to horaSalida)
        ) {
            filter {
                eq("id", parkingId)   // ← ESTA ES LA SINTAXIS CORRECTA
            }
            select()
        }

        return res.decodeSingle()
    }

    override suspend fun getParkingById(id: Int): Parking? {
        val res = client.from("parking")
            .select {
                filter {
                    eq("id", id)      // ← ESTA ES LA SINTAXIS CORRECTA
                }
            }

        return res.decodeSingleOrNull()
    }
}
