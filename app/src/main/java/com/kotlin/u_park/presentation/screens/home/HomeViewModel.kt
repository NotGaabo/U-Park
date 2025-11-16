package com.kotlin.u_park.presentation.screens.home

import android.content.Context
import android.location.Geocoder
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kotlin.u_park.data.remote.supabase
import com.kotlin.u_park.domain.model.Garage
import com.kotlin.u_park.presentation.screens.auth.AuthViewModel
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.*

class HomeViewModel(
    private val authViewModel: AuthViewModel
) : ViewModel() {

    private val _garages = MutableStateFlow<List<Garage>>(emptyList())
    val garages: StateFlow<List<Garage>> = _garages

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadGarages(context: Context, userLat: Double?, userLng: Double?) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = supabase.postgrest["garages"].select()
                val allGarages = response.decodeAs<List<Garage>>()
                    .filter { it.latitud != null && it.longitud != null }

                Log.d("HomeVM", "📦 Garages obtenidos: ${allGarages.size}")

                // ✅ Filtrar solo garages dentro de 1 km si hay ubicación
                val filteredGarages = if (userLat != null && userLng != null) {
                    allGarages.filter {
                        val distance = distanceInKm(userLat, userLng, it.latitud!!, it.longitud!!)
                        distance <= 1.0
                    }.sortedBy {
                        distanceInKm(userLat, userLng, it.latitud!!, it.longitud!!)
                    }
                } else {
                    allGarages // Si no hay ubicación, muestra todos
                }

                filteredGarages.forEachIndexed { index, g ->
                    Log.d(
                        "HomeVM",
                        "✅ Garage ${index + 1}: ${g.nombre} (${g.latitud}, ${g.longitud})"
                    )
                }

                _garages.value = filteredGarages
            } catch (e: Exception) {
                Log.e("HomeVM", "❌ Error al cargar garages", e)
                _garages.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }


    fun distanceInKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2.0) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return R * c
    }

    fun getAddressFromLocationShort(context: Context, lat: Double, lng: Double): String? {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val result = geocoder.getFromLocation(lat, lng, 1)
            if (!result.isNullOrEmpty()) {
                val addr = result[0]
                val street = addr.thoroughfare
                val area = addr.subLocality
                when {
                    street != null && area != null -> "$street, $area"
                    street != null -> street
                    else -> area
                }
            } else null
        } catch (e: Exception) {
            null
        }
    }

    fun formatDistance(km: Double): String {
        return if (km < 1) "${(km * 1000).toInt()} m" else String.format("%.1f km", km)
    }
}
