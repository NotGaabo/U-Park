package com.kotlin.u_park.data.repository

import com.kotlin.u_park.data.remote.supabase
import com.kotlin.u_park.domain.model.Subscription
import com.kotlin.u_park.domain.model.SubscriptionPlan
import com.kotlin.u_park.domain.model.SubscriptionRequestWithDetails
import com.kotlin.u_park.domain.model.User
import com.kotlin.u_park.domain.repository.ISubscriptionRepository
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.serialization.Serializable

class SubscriptionRepository : ISubscriptionRepository {

    override suspend fun getPlans(): List<SubscriptionPlan> =
        supabase.from("subscription_plans")
            .select {
                filter {
                    eq("active", true)
                }
            }
            .decodeList()




    override suspend fun requestSubscription(
        userId: String,
        garageId: String,
        planId: String
    ) {
        supabase.postgrest.rpc(
            "solicitar_suscripcion",
            mapOf(
                "p_user_id" to userId,
                "p_garage_id" to garageId,
                "p_plan_id" to planId
            )
        )
    }

    @Serializable
    data class GarageCapacity(
        val capacidad_total: Int
    )

    @kotlinx.serialization.Serializable
    data class ParkingCount(
        val id: String
    )


    // ✅ BUSCAR CUALQUIER SUSCRIPCIÓN ACTIVA DEL USUARIO
    suspend fun getActiveSubscriptionByUser(
        userId: String
    ): Subscription? =
        supabase.from("subscriptions")
            .select {
                filter {
                    eq("user_id", userId)
                    eq("active", true)
                }
            }
            .decodeSingleOrNull()

    // ✅ BUSCAR SUSCRIPCIÓN ACTIVA POR GARAGE
    override suspend fun getActiveSubscription(
        userId: String,
        garageId: String
    ): Subscription? =
        supabase.from("subscriptions")
            .select {
                filter {
                    eq("user_id", userId)
                    eq("garage_id", garageId)
                    eq("active", true)
                }
            }
            .decodeSingleOrNull()

    override suspend fun getAvailableSpaces(garageId: String): Int {
        return try {
            println("🔍 Calculando espacios libres para garage: $garageId")

            // Obtener capacidad total
            val garage = supabase.from("garages")
                .select {
                    filter { eq("id_garage", garageId) }
                }
                .decodeSingle<GarageCapacity>()

            // Contar parkings activos
            val activos = supabase.from("parkings")
                .select {
                    filter {
                        eq("garage_id", garageId)
                        eq("estado", "activa")
                    }
                }
                .decodeList<ParkingCount>()

            val espaciosLibres = garage.capacidad_total - activos.size
            println("✅ Espacios libres: $espaciosLibres (Total: ${garage.capacidad_total}, Dentro: ${activos.size})")

            espaciosLibres.coerceAtLeast(0) // Nunca devolver negativos

        } catch (e: Exception) {
            println("❌ Error calculando espacios: ${e.message}")
            e.printStackTrace()
            0
        }
    }

    override suspend fun getSolicitudesByGarage(garageId: String): List<SubscriptionRequestWithDetails> {
        return supabase.from("subscription_requests")
            .select {
                filter {
                    eq("garage_id", garageId)
                }
            }
            .decodeList()
    }

    override suspend fun aprobarSolicitud(requestId: String) {
        supabase.postgrest.rpc(
            "aprobar_suscripcion",
            mapOf("p_request_id" to requestId)
        )
    }

    override suspend fun rechazarSolicitud(requestId: String) {
        supabase.from("subscription_requests")
            .update(
                { set("status", "rechazada") }
            ) {
                filter { eq("id", requestId) }
            }
    }


    override suspend fun cancelSubscription(subscriptionId: String) {
        supabase.from("subscriptions")
            .update(
                {
                    set("active", false)
                }
            ) {
                filter {
                    eq("id", subscriptionId)
                }
            }
    }
}
