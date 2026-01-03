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
    locationLine: String,
    onDetails: (Garage) -> Unit,
    onReserve: (Garage) -> Unit,
    onSubscribe: (Garage) -> Unit,
    onGoToGarage: (Garage) -> Unit
) {

    val red = Color(0xFFE60023)
    val gray = MaterialTheme.colorScheme.surfaceVariant
    val grayText = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // 📸 Imagen
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

        Spacer(Modifier.height(14.dp))

        // 📌 Nombre
        Text(
            text = garage.nombre,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        // 📍 Dirección
        Text(locationLine, color = Color.Gray, fontSize = 13.sp)

        Spacer(Modifier.height(8.dp))

        // 🕒 Horario + Capacidad
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(garage.horario ?: "-", color = Color.Gray, fontSize = 13.sp)
            Text(
                "Capacidad: ${garage.capacidadTotal}",
                color = Color.Gray,
                fontSize = 13.sp
            )
        }

        Spacer(Modifier.height(20.dp))

        // ───────── BOTONES ─────────
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // 🔹 FILA 1 → Gris | Rojo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                Button(
                    onClick = { onDetails(garage) },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = gray),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Text("Detalles", color = grayText, fontWeight = FontWeight.Medium)
                }

                Button(
                    onClick = { onReserve(garage) },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = red),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Text("Reservar", color = Color.White, fontWeight = FontWeight.Medium)
                }
            }

            // 🔹 FILA 2 → Rojo | Gris
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                Button(
                    onClick = { onSubscribe(garage) },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = red),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Text("Suscribirse", color = Color.White, fontWeight = FontWeight.Medium)
                }

                Button(
                    onClick = { onGoToGarage(garage) },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = gray),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Icon(Icons.Default.Map, contentDescription = null, tint = grayText)
                    Spacer(Modifier.width(6.dp))
                    Text("Ir al garage", color = grayText, fontWeight = FontWeight.Medium)
                }
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun GarageDetailSheetPreview() {
    val g = Garage(
        nombre = "Garage Central",
        direccion = "Av. Principal 123",
        horario = "08:00 - 22:00",
        capacidadTotal = 120,
        imageUrl = "https://picsum.photos/800/600"
    )

    Surface {
        GarageDetailBottomSheet(
            garage = g,
            locationLine = "Av. Principal, Santo Domingo",
            onDetails = {},
            onReserve = {},
            onSubscribe = {},
            onGoToGarage = {}
        )
    }
}
