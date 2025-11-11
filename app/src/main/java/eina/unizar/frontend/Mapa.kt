@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.carcare.ui

import android.os.Bundle
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import eina.unizar.frontend.R
import eina.unizar.frontend.models.toVehiculo
import eina.unizar.frontend.viewmodels.HomeViewModel
import org.maplibre.android.MapLibre
import org.maplibre.android.WellKnownTileServer
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView

@Composable
fun UbicacionVehiculoScreen(
    onBackClick: () -> Unit = {},
    onInicioClick: () -> Unit = {},
    onMapaClick: () -> Unit = {},
    onReservasClick: () -> Unit = {},
    navController: NavHostController,
    efectiveUserId: String?,
    efectiveToken: String?
) {
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    val currentRoute = navController.currentBackStackEntry?.destination?.route

    // ViewModel y vehículos
    val viewModel = remember { HomeViewModel() }
    val vehiculosDTO by viewModel.vehiculos.collectAsState()
    val vehiculos = vehiculosDTO.map { it.toVehiculo() }

    LaunchedEffect(Unit) {
        if (efectiveUserId != null && efectiveToken != null) {
            viewModel.fetchVehiculos(efectiveUserId, efectiveToken)
        }
    }

    Log.d("Mapa", "Vehículos obtenidos: ${vehiculos.size}")

    // Estado para el vehículo seleccionado y referencia al mapa
    var selectedIndex by remember { mutableIntStateOf(0) }
    val selectedVehiculo = vehiculos.getOrNull(selectedIndex)
    var mapLibreMap by remember { mutableStateOf<MapLibreMap?>(null) }
    var shouldCenterMap by remember { mutableStateOf(false) }
    var showTutorialSnackbar by remember { mutableStateOf(true) }

// Ocultar el snackbar después de 5 segundos
    LaunchedEffect(showTutorialSnackbar) {
        if (showTutorialSnackbar && vehiculos.size > 1) {
            kotlinx.coroutines.delay(5000)
            showTutorialSnackbar = false
        }
    }

    // Función para centrar el mapa en el vehículo seleccionado
    fun centerMapOnVehicle() {
        selectedVehiculo?.ubicacion_actual?.let { ubicacion ->
            mapLibreMap?.let { map ->
                val position = CameraPosition.Builder()
                    .target(LatLng(ubicacion.latitud, ubicacion.longitud))
                    .zoom(16.0)
                    .build()

                map.animateCamera(
                    CameraUpdateFactory.newCameraPosition(position),
                    1000 // Duración de la animación en ms
                )
            }
        }
    }

    // Efecto para centrar cuando cambia el vehículo seleccionado
    LaunchedEffect(selectedIndex) {
        if (vehiculos.isNotEmpty()) {
            centerMapOnVehicle()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ubicación del Vehículo", color = Color.White, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFE53935))
            )
        },
        bottomBar = {
            eina.unizar.frontend.BottomNavigationBar(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    if (route != currentRoute) {
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId) { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { factoryContext ->
                    MapLibre.getInstance(factoryContext, "vzU3m7mUFKYAvOtFHKIq", WellKnownTileServer.MapTiler)
                    val mapView = MapView(factoryContext).apply {
                        onCreate(Bundle())
                        getMapAsync { map ->
                            mapLibreMap = map
                            map.setStyle("https://api.maptiler.com/maps/streets/style.json?key=vzU3m7mUFKYAvOtFHKIq") {
                                // Crear el icono del marcador con tamaño apropiado
                                val iconFactory = org.maplibre.android.annotations.IconFactory.getInstance(context)
                                val markerIcon = iconFactory.fromResource(R.drawable.ic_marker)

                                // Escalar el icono a un tamaño razonable (en píxeles)
                                val scaledIcon = iconFactory.fromBitmap(
                                    android.graphics.Bitmap.createScaledBitmap(
                                        markerIcon.bitmap,
                                        60, // ancho en píxeles
                                        80, // alto en píxeles
                                        false
                                    )
                                )

                                // Añadir marcadores para todos los vehículos
                                vehiculos.forEach { vehiculo ->
                                    vehiculo.ubicacion_actual?.let { ubicacion ->
                                        map.addMarker(
                                            org.maplibre.android.annotations.MarkerOptions()
                                                .position(LatLng(ubicacion.latitud, ubicacion.longitud))
                                                .title(vehiculo.nombre)
                                                .snippet(vehiculo.matricula)
                                                .icon(scaledIcon) // Usa el icono escalado
                                        )
                                    }
                                }

                                // Centrar en el primer vehículo al iniciar
                                if (vehiculos.isNotEmpty()) {
                                    selectedVehiculo?.ubicacion_actual?.let { ubicacion ->
                                        map.cameraPosition = CameraPosition.Builder()
                                            .target(LatLng(ubicacion.latitud, ubicacion.longitud))
                                            .zoom(14.0)
                                            .build()
                                    }
                                }
                            }
                        }
                    }
                    mapView
                },
                update = { mapView ->
                    // Esta función se llama cuando el composable se recompone
                    // Este bloque se ejecuta cada vez que vehiculos cambia
                    mapView.getMapAsync { map ->
                        if (vehiculos.isNotEmpty()) {
                            Log.d("Mapa", "Actualizando marcadores. Vehículos: ${vehiculos.size}")

                            // Limpiar marcadores anteriores
                            map.clear()

                            try {
                                val iconFactory = org.maplibre.android.annotations.IconFactory.getInstance(mapView.context)

                                // Añadir marcadores para TODOS los vehículos
                                vehiculos.forEach { vehiculo ->
                                    vehiculo.ubicacion_actual?.let { ubicacion ->
                                        Log.d("Mapa", "Añadiendo marcador para ${vehiculo.nombre} tipo: ${vehiculo.tipo}")

                                        // AQUÍ ESTÁ LA MAGIA: selecciona el icono según el tipo
                                        val iconResId = when (vehiculo.tipo) {
                                            eina.unizar.frontend.TipoVehiculo.COCHE -> R.drawable.ic_coche
                                            eina.unizar.frontend.TipoVehiculo.MOTO -> R.drawable.ic_moto
                                            eina.unizar.frontend.TipoVehiculo.FURGONETA -> R.drawable.ic_furgoneta
                                            eina.unizar.frontend.TipoVehiculo.CAMION -> R.drawable.ic_camion
                                            eina.unizar.frontend.TipoVehiculo.OTRO -> R.drawable.ic_marker
                                        }

                                        // Cargar y escalar el icono
                                        val markerIcon = iconFactory.fromResource(iconResId)
                                        val scaledIcon = iconFactory.fromBitmap(
                                            android.graphics.Bitmap.createScaledBitmap(
                                                markerIcon.bitmap,
                                                30, // ancho
                                                32, // alto
                                                false
                                            )
                                        )

                                        // Añadir el marcador al mapa
                                        map.addMarker(
                                            org.maplibre.android.annotations.MarkerOptions()
                                                .position(LatLng(ubicacion.latitud, ubicacion.longitud))
                                                .title(vehiculo.nombre)
                                                .snippet("${vehiculo.matricula} - ${vehiculo.tipo}")
                                                .icon(scaledIcon)
                                        )
                                    }
                                }

                                // Centrar SOLO en el vehículo seleccionado
                                selectedVehiculo?.ubicacion_actual?.let { ubicacion ->
                                    map.animateCamera(
                                        CameraUpdateFactory.newCameraPosition(
                                            CameraPosition.Builder()
                                                .target(LatLng(ubicacion.latitud, ubicacion.longitud))
                                                .zoom(15.0)
                                                .build()
                                        ),
                                        500
                                    )
                                }
                            } catch (e: Exception) {
                                Log.e("Mapa", "Error al añadir marcadores: ${e.message}", e)

                                // Si hay error, usar marcadores por defecto
                                vehiculos.forEach { vehiculo ->
                                    vehiculo.ubicacion_actual?.let { ubicacion ->
                                        map.addMarker(
                                            org.maplibre.android.annotations.MarkerOptions()
                                                .position(LatLng(ubicacion.latitud, ubicacion.longitud))
                                                .title(vehiculo.nombre)
                                                .snippet(vehiculo.matricula)
                                        )
                                    }
                                }
                            }
                        } else {
                            Log.d("Mapa", "No hay vehículos para mostrar")
                        }
                    }
                }
            )


            // Pager con las tarjetas de vehículos
            val pagerState = rememberPagerState(pageCount = { vehiculos.size })

            // Sincronizar el índice seleccionado con el pager
            LaunchedEffect(pagerState.currentPage) {
                selectedIndex = pagerState.currentPage
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                contentPadding = PaddingValues(horizontal = 48.dp),
                pageSpacing = 80.dp,
                key = { it }
            ) { page ->
                val vehiculo = vehiculos[page]
                Card(
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = "Vehículo",
                            tint = Color(0xFFE53935),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                vehiculo.nombre,
                                color = Color.Black,
                                fontSize = 18.sp,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                vehiculo.matricula,
                                color = Color.Gray,
                                fontSize = 14.sp,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            vehiculo.ubicacion_actual?.let { ubicacion ->
                                Text(
                                    "Lat: ${String.format("%.4f", ubicacion.latitud)}, Lon: ${String.format("%.4f", ubicacion.longitud)}",
                                    color = Color.Gray,
                                    fontSize = 12.sp,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        Button(
                            onClick = { centerMapOnVehicle() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text("IR", color = Color.White)
                        }
                    }
                }
            }

            // Snackbar tutorial (aparece solo si hay más de 1 vehículo)
            if (showTutorialSnackbar && vehiculos.size > 1) {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp),
                    containerColor = Color(0xFF2D2D2D),
                    contentColor = Color.White,
                    shape = RoundedCornerShape(12.dp),
                    action = {
                        TextButton(onClick = { showTutorialSnackbar = false }) {
                            Text("OK", color = Color(0xFFE53935))
                        }
                    }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.carcare_logo),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            "Desliza para ver el resto de vehículos",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(
    onInicioClick: () -> Unit,
    onMapaClick: () -> Unit,
    onReservasClick: () -> Unit
) {
    NavigationBar(containerColor = Color.White) {
        NavigationBarItem(
            selected = false,
            onClick = onInicioClick,
            icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
            label = { Text("Inicio") }
        )
        NavigationBarItem(
            selected = true,
            onClick = onMapaClick,
            icon = { Icon(Icons.Default.Place, contentDescription = "Mapa") },
            label = { Text("Mapa") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = Color(0xFFE53935),
                indicatorColor = Color(0xFFE53935)
            )
        )
        NavigationBarItem(
            selected = false,
            onClick = onReservasClick,
            icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Reservas") },
            label = { Text("Reservas") }
        )
    }
}