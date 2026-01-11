package com.kotlin.u_park.presentation.screens.home

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.android.gms.location.*
import com.kotlin.u_park.R
import com.kotlin.u_park.domain.model.Garage
import com.kotlin.u_park.presentation.components.GarageCard
import com.kotlin.u_park.presentation.components.GarageDetailBottomSheet
import com.kotlin.u_park.presentation.components.GarageSkeleton
import com.kotlin.u_park.presentation.navigation.Routes
import com.kotlin.u_park.presentation.screens.auth.AuthViewModel
import com.kotlin.u_park.presentation.utils.NetworkViewModel
import com.kotlin.u_park.presentation.utils.checkLocationPermission
import com.kotlin.u_park.presentation.utils.getCurrentLocation
import kotlinx.coroutines.launch

// 🎨 Modern Color System
private val PrimaryRed = Color(0xFFE60023)
private val DarkRed = Color(0xFFB8001C)
private val LightRed = Color(0xFFFFE5E9)
private val BackgroundColor = Color(0xFFFAFAFA)
private val SurfaceColor = Color(0xFFFFFFFF)
private val TextPrimary = Color(0xFF0D0D0D)
private val TextSecondary = Color(0xFF6E6E73)
private val BorderColor = Color(0xFFE5E5EA)
private val SuccessGreen = Color(0xFF34C759)
private val WarningOrange = Color(0xFFFF9500)

data class FilterChip(val label: String, val icon: ImageVector)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    networkViewModel: NetworkViewModel = viewModel()
) {
    val homeViewModel: HomeViewModel = viewModel(factory = HomeViewModelFactory(authViewModel))
    val currentUser by authViewModel.currentUser.collectAsState()
    val userId = currentUser?.id ?: ""
    val context = LocalContext.current

    val coroutineScope = rememberCoroutineScope()
    val garages by homeViewModel.garages.collectAsState()
    val isLoading by homeViewModel.isLoading.collectAsState()

    var userLocation by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var permissionGranted by remember { mutableStateOf(false) }
    var isGettingLocation by remember { mutableStateOf(true) }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> permissionGranted = granted }

    LaunchedEffect(Unit) {
        val hasPermission = checkLocationPermission(context)
        if (!hasPermission)
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        else
            permissionGranted = true
    }

    LaunchedEffect(permissionGranted) {
        if (permissionGranted) {
            getCurrentLocation(context, fusedLocationClient) { loc ->
                userLocation = loc
                isGettingLocation = false
                if (loc != null)
                    homeViewModel.loadGarages(context, loc.first, loc.second)
                else
                    homeViewModel.loadGarages(context, null, null)
            }
        } else {
            isGettingLocation = false
            homeViewModel.loadGarages(context, null, null)
        }
    }

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(0) }
    var selectedGarage by remember { mutableStateOf<Garage?>(null) }
    var selectedLocationLine by remember { mutableStateOf("") }

    val filters = listOf(
        FilterChip(stringResource(R.string.todos), Icons.Outlined.GridView),
        FilterChip(stringResource(R.string.econ_micos), Icons.Outlined.LocalOffer),
        FilterChip(stringResource(R.string.disponibles), Icons.Outlined.CheckCircle)
    )

    val listState = rememberLazyListState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Scroll animation for header
    val showElevation = remember { derivedStateOf { listState.firstVisibleItemScrollOffset > 0 } }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = BackgroundColor,
            bottomBar = {
                ModernBottomBar(
                    selectedIndex = 0,
                    onItemSelected = { index ->
                        when (index) {
                            1 -> navController.navigate(context.getString(R.string.vehicles))
                            2 -> navController.navigate(Routes.HistorialParking.createRoute(userId))
                            3 -> navController.navigate(context.getString(R.string.settings))
                        }
                    }
                )
            }
        ) { padding ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BackgroundColor)
                    .padding(padding)
            ) {

                // 🎯 Premium Header
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = SurfaceColor,
                    shadowElevation = if (showElevation.value) 4.dp else 0.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                    ) {
                        // Top Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    stringResource(R.string.descubre),
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    letterSpacing = (-0.5).sp
                                )

                                AnimatedVisibility(visible = userLocation != null) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(top = 4.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.LocationOn,
                                            contentDescription = null,
                                            tint = PrimaryRed,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            stringResource(R.string.santo_domingo),
                                            fontSize = 14.sp,
                                            color = TextSecondary,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 🔎 Modern Search Bar
                        SearchBarModern(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = stringResource(R.string.buscar_garajes_ubicaciones),
                            isLoading = isGettingLocation
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // 🏷️ Filter Chips
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filters.size) { index ->
                                FilterChipItem(
                                    filter = filters[index],
                                    isSelected = selectedFilter == index,
                                    onClick = { selectedFilter = index }
                                )
                            }
                        }
                    }
                }

                // 📋 Garage List
                LazyColumn(
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {

                    when {
                        isLoading -> {
                            items(5) { GarageSkeleton() }
                        }

                        garages.isEmpty() -> {
                            item {
                                EmptyState(
                                    icon = Icons.Outlined.SearchOff,
                                    title = stringResource(R.string.no_hay_garajes_cercanos),
                                    subtitle = stringResource(R.string.intenta_cambiar_tu_ubicaci_n_o_ajusta_los_filtros),
                                    actionText = stringResource(R.string.actualizar_ubicaci_n),
                                    onAction = {
                                        if (userLocation != null) {
                                            homeViewModel.loadGarages(
                                                context,
                                                userLocation!!.first,
                                                userLocation!!.second
                                            )
                                        }
                                    }
                                )
                            }
                        }

                        else -> {
                            val filtered = garages.filter {
                                it.nombre.contains(searchQuery, ignoreCase = true)
                            }

                            if (filtered.isEmpty()) {
                                item {
                                    EmptyState(
                                        icon = Icons.Outlined.SearchOff,
                                        title = stringResource(R.string.sin_resultados2),
                                        subtitle = stringResource(
                                            R.string.no_encontramos_garajes_con,
                                            searchQuery
                                        ),
                                        showAction = false
                                    )
                                }
                            } else {
                                item {
                                    Text(
                                        stringResource(R.string.garajes_disponibles, filtered.size),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                }

                                items(filtered) { garage ->
                                    GarageCard(
                                        garage = garage,
                                        userLat = userLocation?.first,
                                        userLng = userLocation?.second,
                                        distanceInKm = homeViewModel::distanceInKm,
                                        formatDistanceForUi = homeViewModel::formatDistance,
                                        getAddressFromLocationShort = homeViewModel::getAddressFromLocationShort,
                                        onClick = { selected, location ->
                                            selectedGarage = selected
                                            selectedLocationLine = location
                                            coroutineScope.launch { sheetState.show() }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 🟥 Bottom Sheet
        selectedGarage?.let { garage ->
            ModalBottomSheet(
                onDismissRequest = { selectedGarage = null },
                sheetState = sheetState,
                containerColor = SurfaceColor,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                GarageDetailBottomSheet(
                    garage = garage,
                    locationLine = selectedLocationLine,
                    onReserve = { g ->
                        navController.navigate("registrarReserva/${g.idGarage}")
                    },
                    onDetails = { g ->
                        navController.navigate("garage/${g.idGarage}")
                    },
                    onGoToGarage = { g ->
                        val lat = g.latitud ?: return@GarageDetailBottomSheet
                        val lng = g.longitud ?: return@GarageDetailBottomSheet
                        openGoogleMaps(context, lat, lng)
                    },
                    onSubscribe = { g ->
                        navController.navigate(Routes.Subscription.createRoute(g.idGarage))
                    }
                )
            }
        }
    }
}

// 🎨 Premium Components

@Composable
fun SearchBarModern(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isLoading: Boolean = false
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        color = BackgroundColor,
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Outlined.Search,
                contentDescription = null,
                tint = if (value.isEmpty()) TextSecondary else PrimaryRed,
                modifier = Modifier.size(22.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Box(modifier = Modifier.weight(1f)) {
                if (value.isEmpty()) {
                    Text(
                        placeholder,
                        color = TextSecondary,
                        fontSize = 15.sp
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = PrimaryRed,
                    strokeWidth = 2.dp
                )
            } else if (value.isNotEmpty()) {
                IconButton(
                    onClick = { onValueChange("") },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun FilterChipItem(
    filter: FilterChip,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) PrimaryRed else SurfaceColor,
        border = if (!isSelected) BorderStroke(1.dp, BorderColor) else null,
        modifier = Modifier.height(40.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                filter.icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else TextSecondary,
                modifier = Modifier.size(18.dp)
            )
            Text(
                filter.label,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                color = if (isSelected) Color.White else TextPrimary
            )
        }
    }
}

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String,
    actionText: String = "",
    showAction: Boolean = true,
    onAction: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(BackgroundColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = TextSecondary.copy(alpha = 0.6f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            subtitle,
            fontSize = 15.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 40.dp)
        )

        if (showAction && actionText.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onAction,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(48.dp)
            ) {
                Text(
                    actionText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun ModernBottomBar(
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = SurfaceColor,
        shadowElevation = 12.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomBarItem(
                icon = Icons.Outlined.Home,
                selectedIcon = Icons.Default.Home,
                label = stringResource(R.string.inicio),
                isSelected = selectedIndex == 0,
                onClick = { onItemSelected(0) }
            )
            BottomBarItem(
                icon = Icons.Outlined.DirectionsCar,
                selectedIcon = Icons.Default.DirectionsCar,
                label = stringResource(R.string.veh_culos2),
                isSelected = selectedIndex == 1,
                onClick = { onItemSelected(1) }
            )
            BottomBarItem(
                icon = Icons.Outlined.History,
                selectedIcon = Icons.Default.History,
                label = stringResource(R.string.historial),
                isSelected = selectedIndex == 2,
                onClick = { onItemSelected(2) }
            )
            BottomBarItem(
                icon = Icons.Outlined.Person,
                selectedIcon = Icons.Default.Person,
                label = stringResource(R.string.perfil3),
                isSelected = selectedIndex == 3,
                onClick = { onItemSelected(3) }
            )
        }
    }
}

@Composable
fun BottomBarItem(
    icon: ImageVector,
    selectedIcon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val animatedColor by animateColorAsState(
        targetValue = if (isSelected) PrimaryRed else TextSecondary,
        animationSpec = tween(300)
    )

    Surface(
        onClick = onClick,
        color = Color.Transparent,
        modifier = Modifier.size(64.dp, 56.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                if (isSelected) selectedIcon else icon,
                contentDescription = label,
                tint = animatedColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = animatedColor
            )
        }
    }
}

fun openGoogleMaps(context: Context, lat: Double, lng: Double) {
    val uri = "geo:$lat,$lng?q=$lat,$lng"
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
    intent.setPackage("com.google.android.apps.maps")
    context.startActivity(intent)
}