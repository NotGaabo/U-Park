package com.kotlin.u_park.presentation.screens.suscriptions

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kotlin.u_park.domain.model.Subscription
import com.kotlin.u_park.domain.model.SubscriptionPlan

@Composable
fun ManageSubscriptionScreen(
    userId: String,
    garageId: String,
    viewModel: SubscriptionViewModel
) {
    val subscription = viewModel.subscription
    val plans = viewModel.plans

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        Text("Suscripción", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        if (subscription != null) {
            ActiveSubscriptionCard(subscription, viewModel)
        } else {
            NoSubscription(viewModel, userId, garageId, plans)
        }
    }
}


@Composable
fun ActiveSubscriptionCard(
    subscription: Subscription,
    viewModel: SubscriptionViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Plan activo", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text(subscription.plan?.name ?: "")
            Text("Vehículos: ${subscription.plan?.max_vehicles}")
            Text("Precio: RD$ ${subscription.plan?.price}")

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    viewModel.cancelSubscription(
                        subscription.id,
                        subscription.garage_id
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("Cancelar suscripción", color = Color.White)
            }
        }
    }
}
@Composable
fun NoSubscription(
    viewModel: SubscriptionViewModel,
    userId: String,
    garageId: String,
    plans: List<SubscriptionPlan>
) {
    Text("No tienes una suscripción activa")
    Spacer(Modifier.height(12.dp))

    plans.forEach { plan ->
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(plan.name, fontWeight = FontWeight.Bold)
                Text("Vehículos: ${plan.max_vehicles}")
                Text("RD$ ${plan.price}")

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = {
                        viewModel.requestSubscription(
                            userId,
                            garageId,
                            plan.id
                        ) {}
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Solicitar")
                }
            }
        }
    }
}
