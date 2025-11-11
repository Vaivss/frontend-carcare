package eina.unizar.frontend

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import eina.unizar.frontend.models.Vehiculo
import eina.unizar.frontend.models.VehiculoDTO
import eina.unizar.frontend.viewmodels.HomeViewModel
import androidx.compose.ui.res.painterResource
import eina.unizar.frontend.models.toVehiculo
import eina.unizar.frontend.models.toVehiculoDTO
import eina.unizar.frontend.viewmodels.AuthViewModel
import android.content.Context
import android.app.Application
import androidx.compose.ui.platform.LocalContext
import eina.unizar.frontend.notifications.NotificationPreferences
import eina.unizar.frontend.notifications.NotificationScheduler
import java.util.Calendar

enum class EstadoVehiculo(val color: Color, val texto: String) {
    DISPONIBLE(Color(0xFF10B981), "Disponible"),
    EN_USO(Color(0xFFF59E0B), "En uso"),
    EN_REPARACION(Color(0xFFEF4444), "En reparación")
}

/**
 * Pantalla principal tras iniciar sesión.
 *
 * `HomeScreenWrapper`:
 * - Inicializa `HomeViewModel` y obtiene vehículos y nombre de usuario.
 * - Convierte objetos `VehiculoDTO` a `Vehiculo` para la UI.
 * - Llama a `HomeScreen()` con los datos obtenidos.
 *
 * `HomeScreen()`:
 * - Muestra el saludo al usuario y la lista de vehículos.
 * - Incluye navegación inferior mediante `BottomNavigationBar`.
 * - Ofrece accesos directos a otras secciones (Mapa, Incidencias, Calendario).
 *
 * El enum `EstadoVehiculo` define estados visuales con colores y texto.
 */

@Composable
fun HomeScreenWrapper(
    userId: String,
    token: String,
    vehiculos: List<Vehiculo>,
    onVehiculoClick: (String) -> Unit,
    onAddVehiculoClick: () -> Unit,
    onMapaClick: () -> Unit,
    onCalendarioClick: () -> Unit,
    onIncidenciasClick: () -> Unit,
    onViajesClick: () -> Unit,
    onRepostajesClick: () -> Unit,
    onRevisionesClick: () -> Unit,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    val viewModel = remember { HomeViewModel() }
    val vehiculosDTO by viewModel.vehiculos.collectAsState()
    val vehiculos = vehiculosDTO.map { it.toVehiculo() }

    LaunchedEffect(Unit) {
        viewModel.fetchVehiculos(userId, token)
        viewModel.fetchUserName(userId, token)
    }

    HomeScreen(
        userName = viewModel.userName,
        vehiculos = vehiculos,
        onVehiculoClick = onVehiculoClick,
        onAddVehiculoClick = onAddVehiculoClick,
        onMapaClick = onMapaClick,
        onCalendarioClick = onCalendarioClick,
        onIncidenciasClick = onIncidenciasClick,
        onViajesClick = onViajesClick,
        onRepostajesClick = onRepostajesClick,
        onRevisionesClick = onRevisionesClick,
        selectedTab = selectedTab,
        onTabSelected = onTabSelected,
        navController = navController,
        authViewModel = authViewModel
    )
}

@Composable
fun HomeScreen(
    userName: String,
    vehiculos: List<Vehiculo>,
    onVehiculoClick: (String) -> Unit,
    onAddVehiculoClick: () -> Unit,
    onMapaClick: () -> Unit,
    onCalendarioClick: () -> Unit,
    onIncidenciasClick: () -> Unit,
    onViajesClick: () -> Unit,
    onRepostajesClick: () -> Unit,
    onRevisionesClick: () -> Unit,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    navController: NavHostController,
    authViewModel: AuthViewModel
) {

    val currentRoute = navController.currentBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
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
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
        ) {


        // Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFFEF4444),
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Hola,",
                        fontSize = 16.sp,
                        color = Color.White
                    )
                    Text(
                        text = userName,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                // Icono perfil
                val context = LocalContext.current
                PerfilMenu(
                    onCerrarSesion = {
                        authViewModel.logout()
                        // Borra también de SharedPreferences si es necesario
                        val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
                        prefs.edit().remove("user_id").remove("token").apply()
                        navController.navigate("eleccion") {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    navController = navController
                )
            }
        }

        // Contenido
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 20.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(20.dp))

                // Título Mis Vehículos
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Mis Vehículos",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937)
                    )
                    IconButton(onClick = onAddVehiculoClick) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Añadir vehículo",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
            }

            // Lista de vehículos
            items(vehiculos) { vehiculo ->
                VehiculoCard(
                    vehiculo = vehiculo.toVehiculoDTO(),
                    onClick = { onVehiculoClick(vehiculo.id.toString()) }
                )
                Spacer(modifier = Modifier.height(15.dp))
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))

                // Acceso Rápido
                Text(
                    text = "Acceso Rápido",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937)
                )

                Spacer(modifier = Modifier.height(15.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    QuickAccessCard(
                        icon = Icons.Default.LocationOn,
                        title = "Mapa",
                        color = Color(0xFFEF4444),
                        onClick = onMapaClick,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    QuickAccessCard(
                        icon = Icons.Default.DateRange,
                        title = "Calendario",
                        color = Color(0xFF3B82F6),
                        onClick = onCalendarioClick,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    QuickAccessCard(
                        icon = Icons.Default.Warning,
                        title = "Incidencias",
                        color = Color(0xFF10B981),
                        onClick = onIncidenciasClick,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Segunda fila
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickAccessCard(
                        icon = Icons.Default.Place,
                        title = "Viajes",
                        color = Color(0xFF8B5CF6),
                        onClick = onViajesClick,
                        modifier = Modifier.weight(1f)
                    )
                    QuickAccessCard(
                        icon = Icons.Default.Settings,
                        title = "Repostajes",
                        color = Color(0xFFF97316),
                        onClick = onRepostajesClick,
                        modifier = Modifier.weight(1f)
                    )
                    QuickAccessCard(
                        icon = Icons.Default.Build,
                        title = "Revisiones",
                        color = Color(0xFF9333EA),
                        onClick = onRevisionesClick,
                        modifier = Modifier.weight(1f)
                    )
                    
                }

                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}
    }



@Composable
fun VehiculoCard(
    vehiculo: VehiculoDTO,
    onClick: () -> Unit
) {
    // Asigna color, icono y nombre según el string tipo
    val (color, iconRes, name) = when (vehiculo.tipo.trim().lowercase()) {
        "coche" -> Triple(Color(0xFF3B82F6), R.drawable.ic_coche, "Coche")
        "moto" -> Triple(Color(0xFFF59E0B), R.drawable.ic_moto, "Moto")
        "furgoneta" -> Triple(Color(0xFF10B981), R.drawable.ic_furgoneta, "Furgoneta")
        "camion" -> Triple(Color(0xFFEF4444), R.drawable.ic_camion, "Camión")
        else -> Triple(Color(0xFF6B7280), R.drawable.ic_otro, "Otro")
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .clickable(onClick = onClick)
            .shadow(2.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = name,
                    tint = color,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = vehiculo.nombre,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937)
                )
                Text(
                    text = vehiculo.matricula,
                    fontSize = 14.sp,
                    color = Color(0xFF6B7280)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "● ",
                        fontSize = 13.sp,
                        color = when (vehiculo.estado) {
                            "Activo" -> Color(0xFF10B981)
                            "En uso" -> Color(0xFFF59E0B)
                            "En reparación" -> Color(0xFFEF4444)
                            else -> Color(0xFF10B981)
                        }
                    )
                    Text(
                        text = vehiculo.estado,
                        fontSize = 13.sp,
                        color = when (vehiculo.estado) {
                            "Activo" -> Color(0xFF10B981)
                            "En uso" -> Color(0xFFF59E0B)
                            "En reparación" -> Color(0xFFEF4444)
                            else -> Color(0xFF10B981)
                        }
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Ver detalles",
                tint = Color(0xFF9CA3AF),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun QuickAccessCard(
    icon: ImageVector,
    title: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(90.dp)
            .clickable(onClick = onClick)
            .shadow(2.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                color = Color(0xFF1F2937)
            )
        }
    }
}



@Composable
fun BottomNavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        if (selected) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(Color(0xFFEF4444), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        } else {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color(0xFF9CA3AF),
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = if (selected) Color(0xFFEF4444) else Color(0xFF9CA3AF),
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun PerfilMenu(
    onCerrarSesion: () -> Unit,
    navController: NavHostController
) {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    
    var reservationNotificationsEnabled by remember {
        mutableStateOf(NotificationPreferences.areReservationNotificationsEnabled(context))
    }
    var maintenanceNotificationsEnabled by remember {
        mutableStateOf(NotificationPreferences.areMaintenanceNotificationsEnabled(context))
    }

    Box(
        modifier = Modifier
            .size(50.dp)
            .background(Color.White, CircleShape)
            .clickable { expanded = true },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "Perfil",
            tint = Color(0xFFEF4444),
            modifier = Modifier.size(30.dp)
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Ver invitaciones") },
                onClick = {
                    expanded = false
                    navController.navigate("invitaciones")
                }
            )
            
            Divider()
            
            // Título de notificaciones
            DropdownMenuItem(
                text = { 
                    Text(
                        "Notificaciones",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF6B7280)
                    ) 
                },
                onClick = { }
            )
            
            // Notificaciones de Reservas
            DropdownMenuItem(
                text = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Reservas", fontSize = 14.sp)
                            Text(
                                "1h antes de la cita",
                                fontSize = 11.sp,
                                color = Color(0xFF9CA3AF)
                            )
                        }
                        Checkbox(
                            checked = reservationNotificationsEnabled,
                            onCheckedChange = { enabled ->
                                reservationNotificationsEnabled = enabled
                                NotificationPreferences.setReservationNotificationsEnabled(context, enabled)
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color(0xFFEF4444)
                            )
                        )
                    }
                },
                onClick = {
                    reservationNotificationsEnabled = !reservationNotificationsEnabled
                    NotificationPreferences.setReservationNotificationsEnabled(
                        context,
                        reservationNotificationsEnabled
                    )
                }
            )
            
            // Notificaciones de Mantenimiento
            DropdownMenuItem(
                text = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Mantenimientos", fontSize = 14.sp)
                            Text(
                                "Cuando toque revisión",
                                fontSize = 11.sp,
                                color = Color(0xFF9CA3AF)
                            )
                        }
                        Checkbox(
                            checked = maintenanceNotificationsEnabled,
                            onCheckedChange = { enabled ->
                                maintenanceNotificationsEnabled = enabled
                                NotificationPreferences.setMaintenanceNotificationsEnabled(context, enabled)
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color(0xFFEF4444)
                            )
                        )
                    }
                },
                onClick = {
                    maintenanceNotificationsEnabled = !maintenanceNotificationsEnabled
                    NotificationPreferences.setMaintenanceNotificationsEnabled(
                        context,
                        maintenanceNotificationsEnabled
                    )
                }
            )
            
            Divider()
            
            DropdownMenuItem(
                text = { Text("Cerrar Sesión") },
                onClick = {
                    expanded = false
                    onCerrarSesion()
                }
            )
        }
    }
}