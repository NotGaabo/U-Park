package com.kotlin.u_park.presentation.components

import android.content.Context
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.kotlin.u_park.domain.model.Garage

@Composable
fun GarageCard(
    garage: Garage,
    userLat: Double?,
    userLng: Double?,
    distanceInKm: (Double, Double, Double, Double) -> Double,
    formatDistanceForUi: (Double) -> String,
    getAddressFromLocationShort: (Context, Double, Double) -> String?,
    onClick: (Garage, String) -> Unit
) {
    val context = LocalContext.current
    var isFavorite by remember { mutableStateOf(false) }

    val distanceKm: Double? = remember(userLat, userLng, garage.latitud, garage.longitud) {
        if (userLat != null && userLng != null && garage.latitud != null && garage.longitud != null)
            distanceInKm(userLat, userLng, garage.latitud, garage.longitud)
        else null
    }

    val distanceText = distanceKm?.let { formatDistanceForUi(it) } ?: ""

    val address by produceState<String?>(initialValue = null, garage.latitud, garage.longitud) {
        value = if (garage.latitud != null && garage.longitud != null) {
            getAddressFromLocationShort(context, garage.latitud, garage.longitud)
        } else null
    }

    val locationLine = when {
        !address.isNullOrEmpty() && distanceText.isNotBlank() -> "📍 $address • $distanceText"
        !address.isNullOrEmpty() -> "📍 $address"
        distanceText.isNotBlank() -> "📍 $distanceText"
        else -> "📍 Ubicación desconocida"
    }

    var pressed by remember { mutableStateOf(false) }
    val scaleAnim by animateFloatAsState(if (pressed) 0.98f else 1f, tween(120))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scaleAnim)
            .clickable { onClick(garage, locationLine) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Box(modifier = Modifier.height(220.dp)) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(garage.image_url)
                    .crossfade(true)
                    .build(),
                contentDescription = garage.nombre,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
            ) {
                IconButton(onClick = { isFavorite = !isFavorite }) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) Color.Red else Color.White
                    )
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Text(
                    garage.nombre,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(4.dp))
                Text(locationLine, color = Color(0xFFEEEEEE), fontSize = 13.sp)
                Spacer(Modifier.height(8.dp))
                Surface(color = Color(0x33FFFFFF), shape = RoundedCornerShape(8.dp)) {
                    Text(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        text = "🅿️ ${garage.capacidad_total ?: 0} espacios",
                        color = Color.White,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
