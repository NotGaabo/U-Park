package com.kotlin.u_park.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.kotlin.u_park.domain.model.Garage

@Composable
fun GarageDetailBottomSheet(
    garage: Garage,
    onDismiss: () -> Unit,
    locationLine: String,
    onReserve: () -> Unit,
    onDetails: () -> Unit,
    onGoToGarage: (Garage) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(garage.imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = garage.nombre,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(16.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = garage.nombre,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(text = locationLine, color = Color.Gray, fontSize = 13.sp)

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = garage.horario ?: "-", color = Color.Gray, fontSize = 13.sp)
            Text(
                text = "Capacidad: ${garage.capacidadTotal}",
                color = Color.Gray,
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onDetails,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text(text = "Detalles", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Button(
                onClick = onReserve,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE60023)),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text(text = "Reservar", color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { onGoToGarage(garage) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
            Icon(Icons.Default.Map, contentDescription = "map", tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Ir al garage", color = Color.White)
        }

        Spacer(modifier = Modifier.height(4.dp))
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun GarageDetailSheetPreview() {
    val g = Garage(
        nombre = "Garage Central",
        direccion = "Av. Principal 123",
        horario = "08:00 - 22:00",
        capacidadTotal = 120,
        imageUrl = "https://picsum.photos/800/600?random=2"
    )

    Surface {
        GarageDetailBottomSheet(
            garage = g,
            onDismiss = {},
            locationLine = "Ubicación",
            onReserve = {},
            onDetails = {},
            onGoToGarage = {} // ✅ SE AGREGA ESTO
        )
    }
}
