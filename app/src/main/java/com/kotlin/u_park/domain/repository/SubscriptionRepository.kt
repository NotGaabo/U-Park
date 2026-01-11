package com.kotlin.u_park.domain.repository

import com.kotlin.u_park.domain.model.*
import com.kotlin.u_park.presentation.screens.suscriptions.SubscriptionViewModel

interface ISubscriptionRepository {


    suspend fun getPlans(): List<SubscriptionPlan>

    suspend fun getActiveSubscription(userId: String, garageId: String): Subscription?

    suspend fun getAvailableSpaces(garageId: String): Int

    suspend fun requestSubscription(userId: String, garageId: String, planId: String)

    suspend fun cancelSubscription(id: String)
    suspend fun getSolicitudesByGarage(garageId: String): List<SubscriptionRequestWithDetails>
    suspend fun aprobarSolicitud(requestId: String)
    suspend fun rechazarSolicitud(requestId: String)
}
