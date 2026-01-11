package com.kotlin.u_park.data.repository

import com.kotlin.u_park.domain.model.Garage
import com.kotlin.u_park.domain.model.GarageInsert
import com.kotlin.u_park.domain.model.Role
import com.kotlin.u_park.domain.repository.GarageRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import java.io.File
import java.util.UUID

class GarageRepositoryImpl(
    private val supabase: SupabaseClient
) : GarageRepository {

    // -------------------------
    // DUEÑO: obtener sus garajes
    // -------------------------
    override suspend fun getGaragesByOwner(ownerId: String): List<Garage> {
        return try {
            supabase.from("garages")
                .select {
                    filter { eq("user_id", ownerId) }
                }
                .decodeList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // alias → por compatibilidad
    override suspend fun getGaragesByUserId(userId: String): List<Garage> {
        return getGaragesByOwner(userId)
    }

    override suspend fun getGarageById(garageId: String): Garage? {
        return try {
            supabase.from("garages")
                .select {
                    filter { eq("id_garage", garageId) }
                }
                .decodeSingle()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun newGarage(garage: Garage, imageFile: File?): Boolean {
        return try {
            val garageId = garage.idGarage ?: UUID.randomUUID().toString()
            var imageUrl: String? = null

            if (imageFile != null) {
                val bucket = supabase.storage.from("garages-image")
                val imgPath = "garage_$garageId.jpg"

                bucket.upload(
                    path = imgPath,
                    data = imageFile.readBytes()
                ) { upsert = true }

                imageUrl = bucket.publicUrl(imgPath)
            }

            val insert = GarageInsert(
                id_garage = garageId,
                nombre = garage.nombre,
                direccion = garage.direccion,
                latitud = garage.latitud,
                longitud = garage.longitud,
                capacidad_total = garage.capacidadTotal,
                horario = garage.horario,
                fecha_creacion = garage.fechaCreacion,
                image_url = imageUrl,
                is_active = garage.isActive,
                user_id = garage.userId
            )

            // Insertar el garaje
            supabase.from("garages").insert(insert)

            // ASIGNAR ROL DE DUEÑO GARAGE
            garage.userId?.let { userId ->
                // 1. Obtener el role_id del rol "dueno_garage"
                val role = supabase.from("roles")
                    .select {
                        filter { eq("nombre", "dueno_garage") }
                    }
                    .decodeSingleOrNull<Role>()

                role?.let { r ->
                    // 2. Insertar en user_roles (usar upsert para evitar duplicados)
                    supabase.from("user_roles").upsert(
                        mapOf(
                            "user_id" to userId,
                            "role_id" to r.id
                        )
                    )
                } ?: run {
                    println("ERROR: No se encontró el rol 'dueno_garage' en la base de datos")
                }
            }

            true

        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
