package com.kotlin.u_park.data.repository

import com.kotlin.u_park.domain.model.Garage
import com.kotlin.u_park.domain.repository.GarageRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import kotlinx.serialization.Serializable
import java.io.File
import java.util.UUID

class GarageRepositoryImpl(
    private val supabase: SupabaseClient
) : GarageRepository {

    // 🔹 Obtener lista de garajes por userId
    override suspend fun getGaragesByUserId(userId: String): List<Garage> {
        return try {
            supabase.from("garages")
                .select()
                .decodeList<Garage>()
                .filter { it.userId == userId } // filtro manual
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    override suspend fun getGarageById(garageId: String): Garage? {
        return try {
            supabase.from("garages").select {
                filter {
                    eq("id_garage", garageId)
                }
            }.decodeSingle<Garage>()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // 🔹 Insertar nuevo garaje (con imagen opcional)
    override suspend fun newGarage(garage: Garage, imageFile: File?): Boolean {
        return try {

            val garageId = garage.idGarage ?: UUID.randomUUID().toString()
            var imageUrl: String? = null

            // 📌 Subir imagen si existe
            if (imageFile != null) {

                val bucket = supabase.storage.from("garages-image")
                val imagePath = "garage_$garageId.jpg"

                bucket.upload(
                    path = imagePath,
                    data = imageFile.readBytes()
                ) { upsert = true }

                imageUrl = bucket.publicUrl(imagePath)
            }

            // 📌 Objeto para insertar a Supabase
            val newData = GarageInsert(
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

            // 📌 Insertar en tabla
            supabase.from("garages").insert(newData)

            true

        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

@Serializable
data class GarageInsert(
    val id_garage: String,
    val nombre: String,
    val direccion: String,
    val latitud: Double,
    val longitud: Double,
    val capacidad_total: Int,
    val horario: String? = null,
    val fecha_creacion: String? = null,
    val image_url: String? = null,
    val is_active: Boolean = true,
    val user_id: String?
)
