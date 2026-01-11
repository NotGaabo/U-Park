package com.kotlin.u_park.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class SubscriptionPlan(
    val id: String,
    val name: String,
    val max_vehicles: Int,
    val price: Double,
    val active: Boolean
)
@Serializable
data class SubscriptionRequest(
    val id: String,
    val user_id: String,
    val garage_id: String,
    val plan_id: String,
    val status: String,
    val created_at: String
)
@Serializable
data class Subscription(
    val id: String,
    val user_id: String,
    val garage_id: String,
    val plan_id: String,
    val start_date: String,
    val active: Boolean,
    val plan: SubscriptionPlan? = null
)

@Serializable
data class SubscriptionRequestWithDetails(
    val id: String,
    val user_id: String,
    val garage_id: String,
    val plan_id: String,
    val status: String,
    val created_at: String,
    val user: User? = null,
    val plan: SubscriptionPlan? = null
)
