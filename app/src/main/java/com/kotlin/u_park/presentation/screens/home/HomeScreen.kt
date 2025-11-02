package com.kotlin.u_park.presentation.screens.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.android.gms.location.*
import com.kotlin.u_park.domain.model.Garage
import com.kotlin.u_park.presentation.components.GarageCard
import com.kotlin.u_park.presentation.components.GarageDetailBottomSheet
import com.kotlin.u_park.presentation.components.GarageSkeleton
import com.kotlin.u_park.presentation.screens.auth.AuthViewModel
import com.kotlin.u_park.presentation.utils.NetworkViewModel
import kotlinx.coroutines.launch

private val RedSoft = Color(0xFFE60023)
private val BackgroundColor = Color(0xFFF5F5F5)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    networkViewModel: NetworkViewModel = viewModel()
) {
    val homeViewModel: HomeViewModel = viewModel(factory = HomeViewModelFactory(authViewModel))
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isConnected by networkViewModel.isConnected.observeAsState(true)
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
        if (!hasPermission) locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        else permissionGranted = true
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
    var selectedGarage by remember { mutableStateOf<Garage?>(null) }
    var selectedLocationLine by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        containerColor = BackgroundColor,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("U-Park", fontWeight = FontWeight.Bold, color = RedSoft) },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Notifications, null, tint = Color.Black)
                    }
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(Icons.Default.Settings, null, tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundColor)
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(
                    selected = true,
                    onClick = {},
                    icon = { Icon(Icons.Default.Home, null, tint = RedSoft) },
                    label = { Text("Home", color = RedSoft) }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = {},
                    icon = { Icon(Icons.Default.Add, null) },
                    label = { Text("Agregar") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = {},
                    icon = { Icon(Icons.Default.History, null) },
                    label = { Text("Historial") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("settings") },
                    icon = { Icon(Icons.Default.Person, null) },
                    label = { Text("Perfil") }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundColor)
                .padding(padding)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar un garaje", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Search, "", tint = Color.Gray) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                )
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isGettingLocation -> Text("📡 Obteniendo ubicación...", color = Color.Gray)
                    userLocation != null -> Text(
                        "📍 Coordenadas: ${userLocation!!.first}, ${userLocation!!.second}",
                        color = Color.DarkGray,
                        fontWeight = FontWeight.Medium
                    )
                    else -> Text("⚠️ No se pudo obtener la ubicación", color = Color.Red)
                }
            }

            LazyColumn(
                state = listState,
                verticalArrangement = Arrangement.spacedBy(20.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when {
                    isLoading -> items(4) { GarageSkeleton() }
                    garages.isEmpty() -> item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 80.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No hay garajes cerca (1 km)", color = Color.Gray)
                        }
                    }
                    else -> {
                        val filtered = garages.filter {
                            it.nombre.contains(searchQuery, ignoreCase = true)
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

    selectedGarage?.let {
        ModalBottomSheet(
            onDismissRequest = { selectedGarage = null },
            sheetState = sheetState
        ) {
            GarageDetailBottomSheet(
                garage = it,
                onDismiss = { selectedGarage = null },
                locationLine = selectedLocationLine,
                onReserve = {},
                onDetails = {}
            )
        }
    }
}

@SuppressLint("MissingPermission")
fun getCurrentLocation(
    context: Context,
    fusedLocationClient: FusedLocationProviderClient,
    onLocationReceived: (Pair<Double, Double>?) -> Unit
) {
    if (!checkLocationPermission(context)) {
        onLocationReceived(null)
        return
    }
    fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
        if (location != null) {
            onLocationReceived(Pair(location.latitude, location.longitude))
        } else {
            val request = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, 1000L
            ).setMaxUpdates(1).build()
            fusedLocationClient.requestLocationUpdates(
                request,
                object : LocationCallback() {
                    override fun onLocationResult(result: LocationResult) {
                        fusedLocationClient.removeLocationUpdates(this)
                        val loc = result.lastLocation
                        onLocationReceived(loc?.let { Pair(it.latitude, it.longitude) })
                    }
                },
                context.mainLooper
            )
        }
    }.addOnFailureListener {
        onLocationReceived(null)
    }
}

fun checkLocationPermission(context: Context): Boolean {
    val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    return fine || coarse
}
