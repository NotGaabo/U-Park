package com.kotlin.u_park.presentation.screens.suscriptions

import com.kotlin.u_park.domain.model.SubscriptionPlan
import com.kotlin.u_park.domain.model.User
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import com.kotlin.u_park.ui.theme.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionScreen(
    userId: String,
    garageId: String,
    user: User?,
    viewModel: SubscriptionViewModel,
    onSuccess: () -> Unit
) {
    LaunchedEffect(userId, garageId) {
        viewModel.loadGarageData(userId, garageId)
    }

    val plans = viewModel.plans
    val loading = viewModel.loading
    val spaces = viewModel.availableSpaces
    val error = viewModel.error

    var selectedPlan by remember { mutableStateOf<SubscriptionPlan?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }

    val filteredPlans = plans.filter { it.max_vehicles <= spaces }

    // ✅ Success Dialog with Auto-Return
    if (showSuccessDialog) {
        SuccessDialog(
            onDismiss = {
                showSuccessDialog = false
                onSuccess()
            }
        )
    }

    Scaffold(
        containerColor = BackgroundColor,
        topBar = {
            Surface(
                color = SurfaceColor,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        onClick = onSuccess,
                        shape = CircleShape,
                        color = BackgroundColor,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Volver",
                                tint = TextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            "Suscripción",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            "Elige tu plan mensual",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // 🚨 Error Banner
            error?.let {
                item {
                    ErrorBanner(message = it)
                }
            }

            // 📊 Availability Card
            item {
                AvailabilityCard(spaces = spaces)
            }

            // 👤 User Info Section
            item {
                UserInfoSection(
                    user = user,
                    cedula = user?.cedula?.toString() ?: "",
                    nombre = user?.nombre ?: "",
                    telefono = user?.telefono ?: ""
                )
            }

            // 💳 Plans Section Header
            item {
                Text(
                    "Planes disponibles",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // 💎 Plan Cards
            if (filteredPlans.isEmpty()) {
                item {
                    EmptyPlansState()
                }
            } else {
                items(filteredPlans) { plan ->
                    PlanCard(
                        plan = plan,
                        isSelected = selectedPlan?.id == plan.id,
                        onClick = { selectedPlan = plan }
                    )
                }
            }

            // Spacer for bottom button
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // 🎯 Floating Action Button
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                shape = RoundedCornerShape(16.dp),
                shadowElevation = 8.dp,
                color = SurfaceColor
            ) {
                Button(
                    onClick = {
                        selectedPlan?.let { plan ->
                            isSubmitting = true
                            viewModel.requestSubscription(
                                userId = userId,
                                garageId = garageId,
                                planId = plan.id,
                                onSuccess = {
                                    isSubmitting = false
                                    showSuccessDialog = true
                                }
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = selectedPlan != null && !loading && !isSubmitting && spaces > 0,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryRed,
                        disabledContainerColor = BorderColor
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                "Confirmar suscripción",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AvailabilityCard(spaces: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = if (spaces > 0) LightRed else Color(0xFFFFF3E0)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (spaces > 0) PrimaryRed.copy(alpha = 0.15f)
                        else WarningOrange.copy(alpha = 0.15f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (spaces > 0) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (spaces > 0) PrimaryRed else WarningOrange,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    if (spaces > 0) "Espacios disponibles" else "Capacidad limitada",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    if (spaces > 0) "$spaces espacios libres en este garaje"
                    else "No hay espacios disponibles",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }

            Text(
                "$spaces",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = if (spaces > 0) PrimaryRed else WarningOrange
            )
        }
    }
}

@Composable
fun UserInfoSection(
    user: User?,
    cedula: String,
    nombre: String,
    telefono: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Información del titular",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = SurfaceColor,
            border = BorderStroke(1.dp, BorderColor)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                InfoRow(
                    icon = Icons.Outlined.Badge,
                    label = "Cédula",
                    value = cedula.ifEmpty { "No disponible" }
                )

                Divider(color = BorderColor)

                InfoRow(
                    icon = Icons.Outlined.Person,
                    label = "Nombre completo",
                    value = nombre.ifEmpty { "No disponible" }
                )

                Divider(color = BorderColor)

                InfoRow(
                    icon = Icons.Outlined.Phone,
                    label = "Teléfono",
                    value = telefono.ifEmpty { "No disponible" }
                )
            }
        }
    }
}

@Composable
fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(BackgroundColor, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                label,
                fontSize = 12.sp,
                color = TextSecondary,
                fontWeight = FontWeight.Medium
            )
            Text(
                value,
                fontSize = 15.sp,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun PlanCard(
    plan: SubscriptionPlan,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) PrimaryRed else BorderColor
    val backgroundColor = if (isSelected) LightRed else SurfaceColor

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        border = BorderStroke(2.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        plan.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        "Plan mensual",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = if (isSelected) PrimaryRed else BorderColor,
                    border = if (!isSelected) BorderStroke(2.dp, BorderColor) else null,
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.animation.AnimatedVisibility(
                            visible = isSelected,
                            enter = scaleIn() + fadeIn(),
                            exit = scaleOut() + fadeOut()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Divider(color = BorderColor.copy(alpha = 0.5f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FeatureChip(
                    icon = Icons.Outlined.DirectionsCar,
                    text = "${plan.max_vehicles} vehículos"
                )
                FeatureChip(
                    icon = Icons.Outlined.CalendarMonth,
                    text = "30 días"
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "Precio mensual",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            "RD$",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryRed
                        )
                        Text(
                            "${plan.price}",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryRed
                        )
                    }
                }

                if (isSelected) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = PrimaryRed.copy(alpha = 0.15f)
                    ) {
                        Text(
                            "Seleccionado",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryRed
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FeatureChip(icon: ImageVector, text: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = BackgroundColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text,
                fontSize = 13.sp,
                color = TextPrimary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun EmptyPlansState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(BackgroundColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Outlined.ErrorOutline,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = TextSecondary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "No hay planes disponibles",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Text(
            "Este garaje no tiene espacios suficientes",
            fontSize = 14.sp,
            color = TextSecondary,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
fun ErrorBanner(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFFEECEB)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Error,
                contentDescription = null,
                tint = Color(0xFFD32F2F),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                message,
                fontSize = 14.sp,
                color = Color(0xFFD32F2F),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun SuccessDialog(onDismiss: () -> Unit) {
    var progress by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        while (progress < 1f) {
            delay(30)
            progress += 0.02f
        }
        delay(500)
        onDismiss()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceColor,
        shape = RoundedCornerShape(24.dp),
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(SuccessGreen.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = SuccessGreen,
                        modifier = Modifier.size(48.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    "¡Solicitud enviada!",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    "Tu solicitud de suscripción ha sido enviada exitosamente. Te notificaremos cuando sea aprobada.",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = SuccessGreen,
                    trackColor = SuccessGreen.copy(alpha = 0.2f)
                )
            }
        },
        confirmButton = {}
    )
}