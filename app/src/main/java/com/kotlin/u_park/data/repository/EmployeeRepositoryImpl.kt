package com.kotlin.u_park.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.kotlin.u_park.domain.model.EmpleadoGarage
import com.kotlin.u_park.domain.model.Garage
import com.kotlin.u_park.domain.repository.EmpleadoGarageRepository
import com.kotlin.u_park.presentation.screens.employee.Stats
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import com.kotlin.u_park.domain.model.Parking
import kotlinx.serialization.Serializable
import java.util.UUID

class EmpleadoGarageRepositoryImpl(
    private val supabase: SupabaseClient
) : EmpleadoGarageRepository {

    override suspend fun addEmpleadoToGarage(garageId: String, empleadoId: String): Boolean {
        if (garageId.isBlank() || empleadoId.isBlank()) {
            android.util.Log.e("SUPA_ADD", "ERROR → garageId o empleadoId vacíos")
            return false
        }

        return try {
            // 1️⃣ Insertar en empleados_garage
            supabase.postgrest["empleados_garage"].insert(
                mapOf(
                    "garage_id" to garageId,
                    "empleado_id" to empleadoId
                )
            )

            // 2️⃣ Asignar rol "employee" si no lo tiene
            assignEmployeeRoleIfNotExists(empleadoId)

            true
        } catch (e: Exception) {
            android.util.Log.e("SUPA_ADD", "ERROR → ${e.message}")
            false
        }
    }

    override suspend fun getGarageByEmpleadoId(userId: String): String? {
        return try {

            // 1️⃣ Hacemos la consulta estilo URL (igual que tú)
            val response = supabase.postgrest[
                "empleados_garage?empleado_id=eq.$userId&select=garage_id"
            ].select()

            // 2️⃣ Decodificar lista de mapas
            val lista = response.decodeList<Map<String, String>>()

            // 3️⃣ Tomar el garage_id o null
            lista.firstOrNull()?.get("garage_id")

        } catch (e: Exception) {
            android.util.Log.e("SUPA_GARAGE", "ERROR → ${e.message}")
            null
        }
    }


    override suspend fun getEmpleadosByGarage(garageId: String): List<EmpleadoGarage> {
        if (garageId.isBlank()) {
            android.util.Log.e("SUPA_GET", "ERROR → garageId vacío")
            return emptyList()
        }

        return try {
            val select =
                "id,garage_id,empleado_id,fecha_registro," +
                        "users:empleado_id(id,nombre,usuario,cedula,telefono,correo,direccion)"

            supabase.postgrest["empleados_garage?garage_id=eq.$garageId&select=$select"]
                .select()
                .decodeList<EmpleadoGarage>()

        } catch (e: Exception) {
            android.util.Log.e("SUPA_GET", "ERROR → ${e.message}")
            emptyList()
        }
    }

    override suspend fun removeEmpleadoFromGarage(garageId: String, empleadoId: String): Boolean {
        if (garageId.isBlank() || empleadoId.isBlank()) {
            android.util.Log.e("SUPA_DEL", "ERROR → IDs vacíos")
            return false
        }

        return try {
            supabase.postgrest[
                "empleados_garage?garage_id=eq.$garageId&empleado_id=eq.$empleadoId"
            ].delete()

            true
        } catch (e: Exception) {
            android.util.Log.e("SUPA_DEL", "ERROR → ${e.message}")
            false
        }
    }

    // ───────────────
    // 🔹 Helper para asignar rol "employee" evitando duplicados usando columns.ALL
    // ───────────────
    private suspend fun assignEmployeeRoleIfNotExists(userId: String) {
        try {
            // 1️⃣ Obtener el rol 'employee'
            val roles = supabase.from("roles")
                .select()
                .decodeList<Role>()
                .filter { it.nombre == "employee" }

            if (roles.isEmpty()) {
                android.util.Log.e("SUPA_ROLE", "No existe rol 'employee'")
                return
            }

            val roleId = roles.first().id

            // 2️⃣ Verificar si el usuario ya tiene ese rol
            val existingRoles = supabase.from("user_roles")
                .select()
                .decodeList<UserRole>()
                .filter { it.user_id == userId && it.role_id == roleId }

            if (existingRoles.isEmpty()) {
                // 3️⃣ Insertar rol
                supabase.from("user_roles")
                    .insert(UserRole(user_id = userId, role_id = roleId))
                android.util.Log.d("SUPA_ROLE", "Rol 'employee' asignado a $userId")
            } else {
                android.util.Log.d("SUPA_ROLE", "Usuario ya tiene rol 'employee'")
            }

        } catch (e: Exception) {
            android.util.Log.e("SUPA_ROLE", "No se pudo asignar rol employee → ${e.message}")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun getStats(garageId: String): Stats {
        return try {
            // 🔹 Obtener garage
            val garageList = supabase.from("garages")
                .select()
                .decodeList<Garage>()

            val garage = garageList.firstOrNull { it.idGarage == garageId }
                ?: return Stats() // Si no existe, devolver stats vacíos

            val capacidadTotal = garage.capacidadTotal

            // 🔹 Obtener todos los parkings
            val parkingList = supabase.from("parkings")
                .select()
                .decodeList<Parking>()

            // 🔹 Filtrar por garageId
            val parkingsGarage = parkingList.filter { it.garage_id == garageId }

            // 🔹 Autos activos dentro del garage (hora_salida = null)
            val autosActivos = parkingsGarage.count { it.hora_salida == null }

            // 🔹 Entradas y salidas de hoy
            val hoy = java.time.LocalDate.now().toString() // "yyyy-MM-dd"

            val entradasHoy = parkingsGarage.count {
                it.hora_entrada.toString().startsWith(hoy)
            }

            val salidasHoy = parkingsGarage.count {
                it.hora_salida?.toString()?.startsWith(hoy) == true
            }

            // 🔹 Espacios libres
            val espaciosLibres = capacidadTotal - autosActivos

            Stats(
                autosActivos = autosActivos,
                espaciosLibres = espaciosLibres,
                entradasHoy = entradasHoy,
                salidasHoy = salidasHoy
            )

        } catch (e: Exception) {
            e.printStackTrace()
            Stats()
        }
    }

}

@Serializable
data class Role(
    val id: Int,
    val nombre: String
)

@Serializable
data class UserRole(
    val user_id: String,
    val role_id: Int
)
