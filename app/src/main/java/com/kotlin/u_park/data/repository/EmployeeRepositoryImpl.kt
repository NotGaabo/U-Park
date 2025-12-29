package com.kotlin.u_park.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.kotlin.u_park.domain.model.EmpleadoGarage
import com.kotlin.u_park.domain.model.EmpleadoGarageInsert
import com.kotlin.u_park.domain.model.Garage
import com.kotlin.u_park.domain.repository.EmpleadoGarageRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import com.kotlin.u_park.domain.model.Parking
import com.kotlin.u_park.domain.model.Role
import com.kotlin.u_park.domain.model.Stats
import com.kotlin.u_park.domain.model.UserRole
import kotlinx.serialization.Serializable

class EmpleadoGarageRepositoryImpl(
    private val supabase: SupabaseClient
) : EmpleadoGarageRepository {

    override suspend fun addEmpleadoToGarage(garageId: String, empleadoCedula: Long): Boolean {
        if (garageId.isBlank()) {
            android.util.Log.e("SUPA_ADD", "ERROR → garageId vacío")
            return false
        }

        return try {

            val body = EmpleadoGarageInsert(
                garage_id = garageId,
                empleado_id = empleadoCedula
            )

            supabase.postgrest["empleados_garage"].insert(body)

            assignEmployeeRoleIfNotExists(empleadoCedula)

            true

        } catch (e: Exception) {
            android.util.Log.e("SUPA_ADD", "ERROR → ${e.message}")
            false
        }
    }

    override suspend fun getGarageByEmpleadoId(cedula: Long): String? {
        return try {
            val response = supabase.postgrest[
                "empleados_garage?empleado_id=eq.$cedula&select=garage_id"
            ].select()

            val lista = response.decodeList<Map<String, String>>()

            lista.firstOrNull()?.get("garage_id")
        } catch (e: Exception) {
            android.util.Log.e("SUPA_GARAGE", "ERROR → ${e.message}")
            null
        }
    }

    override suspend fun getEmpleadosByGarage(garageId: String): List<EmpleadoGarage> {
        if (garageId.isBlank()) return emptyList()

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

    override suspend fun removeEmpleadoFromGarage(garageId: String, empleadoCedula: Long): Boolean {
        if (garageId.isBlank()) return false

        return try {
            supabase.postgrest[
                "empleados_garage?garage_id=eq.$garageId&empleado_id=eq.$empleadoCedula"
            ].delete()

            true
        } catch (e: Exception) {
            android.util.Log.e("SUPA_DEL", "ERROR → ${e.message}")
            false
        }
    }

    private suspend fun assignEmployeeRoleIfNotExists(cedula: Long) {
        try {
            val roles = supabase.from("roles")
                .select()
                .decodeList<Role>()
                .filter { it.nombre == "employee" }

            if (roles.isEmpty()) return

            val roleId = roles.first().id

            val existing = supabase.from("user_roles")
                .select()
                .decodeList<UserRole>()
                .any { it.user_id == cedula.toString() && it.role_id == roleId }

            if (!existing) {
                supabase.from("user_roles").insert(
                    UserRole(
                        user_id = cedula.toString(),
                        role_id = roleId
                    )
                )
            }

        } catch (e: Exception) {
            android.util.Log.e("SUPA_ROLE", "ERROR → ${e.message}")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun getStats(garageId: String): Stats {
        return try {
            val garageList = supabase.from("garages").select().decodeList<Garage>()

            val garage = garageList.firstOrNull { it.idGarage == garageId }
                ?: return Stats()

            val capacidadTotal = garage.capacidadTotal

            val parkingList = supabase.from("parkings").select().decodeList<Parking>()

            val parkingsGarage = parkingList.filter { it.garage_id == garageId }

            val autosActivos = parkingsGarage.count { it.hora_salida == null }

            val hoy = java.time.LocalDate.now().toString()

            val entradasHoy = parkingsGarage.count { it.hora_entrada.toString().startsWith(hoy) }
            val salidasHoy = parkingsGarage.count { it.hora_salida?.toString()?.startsWith(hoy) == true }

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

