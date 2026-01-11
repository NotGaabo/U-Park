package com.kotlin.u_park.presentation.screens.suscriptions

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kotlin.u_park.domain.model.Subscription
import com.kotlin.u_park.domain.model.SubscriptionPlan
import com.kotlin.u_park.domain.model.SubscriptionRequestWithDetails
import com.kotlin.u_park.domain.model.User
import com.kotlin.u_park.domain.repository.ISubscriptionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

class SubscriptionViewModel(
    private val repo: ISubscriptionRepository
) : ViewModel() {

    var plans by mutableStateOf<List<SubscriptionPlan>>(emptyList())
        private set

    var subscription by mutableStateOf<Subscription?>(null)
        private set

    var availableSpaces by mutableStateOf(0)
        private set

    var loading by mutableStateOf(false)
        private set

    // ❌ ELIMINAR - Ya no se carga desde el repositorio
    // var user by mutableStateOf<User?>(null)
    //     private set

    var error by mutableStateOf<String?>(null)
        private set

    private val _solicitudes = MutableStateFlow<List<SubscriptionRequestWithDetails>>(emptyList())
    val solicitudes = _solicitudes.asStateFlow()

    fun loadSolicitudesByGarage(garageId: String) {
        viewModelScope.launch {
            loading = true
            error = null

            try {
                _solicitudes.value = repo.getSolicitudesByGarage(garageId)
                println("✅ Solicitudes cargadas: ${_solicitudes.value.size}")
            } catch (e: Exception) {
                println("❌ ERROR: ${e.message}")
                error = e.message
            }

            loading = false
        }
    }

    fun aprobarSolicitud(requestId: String, garageId: String) {
        viewModelScope.launch {
            loading = true
            try {
                repo.aprobarSolicitud(requestId)
                loadSolicitudesByGarage(garageId)
            } catch (e: Exception) {
                error = e.message
            }
            loading = false
        }
    }

    fun rechazarSolicitud(requestId: String, garageId: String) {
        viewModelScope.launch {
            loading = true
            try {
                repo.rechazarSolicitud(requestId)
                loadSolicitudesByGarage(garageId)
            } catch (e: Exception) {
                error = e.message
            }
            loading = false
        }
    }
    fun loadGarageData(userId: String, garageId: String) {
        viewModelScope.launch {
            loading = true
            error = null

            try {
                println("🔍 Cargando datos: userId=$userId, garageId=$garageId")

                // ❌ ELIMINAR ESTA LÍNEA
                // user = repo.getUserById(userId)

                plans = repo.getPlans()
                println("✅ Planes cargados: ${plans.size} planes")
                plans.forEach { println("  - ${it.name}: ${it.max_vehicles} vehículos, RD\$${it.price}") }

                subscription = repo.getActiveSubscription(userId, garageId)
                println("✅ Suscripción: ${subscription?.id ?: "ninguna"}")

                availableSpaces = repo.getAvailableSpaces(garageId)
                println("✅ Espacios disponibles: $availableSpaces")

            } catch (e: Exception) {
                println("❌ ERROR: ${e.message}")
                e.printStackTrace()
                error = e.message ?: "Error cargando datos"
            }

            loading = false
        }
    }

    fun requestSubscription(
        userId: String,
        garageId: String,
        planId: String,
        onSuccess: () -> Unit
    ) = viewModelScope.launch {
        loading = true
        error = null

        try {
            repo.requestSubscription(userId, garageId, planId)
            subscription = repo.getActiveSubscription(userId, garageId)
            availableSpaces = repo.getAvailableSpaces(garageId)
            onSuccess()
        } catch (e: Exception) {
            error = e.message
        }

        loading = false
    }

    fun cancelSubscription(id: String, garageId: String) =
        viewModelScope.launch {
            loading = true
            error = null

            try {
                repo.cancelSubscription(id)
                subscription = null
                availableSpaces = repo.getAvailableSpaces(garageId)
            } catch (e: Exception) {
                error = e.message
            }

            loading = false
        }
}