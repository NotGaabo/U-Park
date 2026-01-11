package com.kotlin.u_park.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SubscriptionBottomSheet(
    userName: String,
    cedula: String,
    phone: String,
    plans: List<Pair<String, String>>, // ("Plan 1", "RD$2000")
    onSubmit: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedPlan by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
    ) {

        Text(
            text = "Suscripción",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(16.dp))

        // Datos del usuario
        InfoField("Nombre", userName)
        InfoField("Cédula", cedula)
        InfoField("Teléfono", phone)

        Spacer(Modifier.height(20.dp))

        Text("Selecciona un plan", fontWeight = FontWeight.Bold)

        Spacer(Modifier.height(8.dp))

        plans.forEachIndexed { index, plan ->
            PlanItem(
                name = plan.first,
                price = plan.second,
                selected = index == selectedPlan,
                onClick = { selectedPlan = index }
            )
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { onSubmit(selectedPlan) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D47A1))
        ) {
            Text("Enviar solicitud", color = Color.White)
        }

        Spacer(Modifier.height(8.dp))

        TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
            Text("Cancelar")
        }
    }
}

@Composable
fun InfoField(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, fontSize = 12.sp, color = Color.Gray)
        Text(
            value,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF1F1F1), RoundedCornerShape(8.dp))
                .padding(12.dp)
        )
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
fun PlanItem(name: String, price: String, selected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (selected) Color(0xFFE3F2FD) else Color.White
        ),
        border = if (selected) BorderStroke(2.dp, Color(0xFF0D47A1)) else null
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(name, fontWeight = FontWeight.Medium)
            Text(price, fontWeight = FontWeight.Bold)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SubscriptionSheetPreview() {
    SubscriptionBottomSheet(
        userName = "Juan Pérez",
        cedula = "001-1234567-8",
        phone = "809-555-1234",
        plans = listOf(
            "Plan 1 - 1 Vehículo" to "RD$2000",
            "Plan 2 - 2 Vehículos" to "RD$3500",
            "Plan 3 - 3 Vehículos" to "RD$5000"
        ),
        onSubmit = {},
        onDismiss = {}
    )
}
