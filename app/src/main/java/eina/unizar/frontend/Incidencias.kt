package eina.unizar.frontend

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavHostController
import eina.unizar.frontend.models.Vehiculo



// Clases solicitadas
data class Usuario(
    val id: String,
    val nombre: String,
    val iniciales: String,
    val email: String
)

//! HAY QUE PONER ICONOS PERSONALIZADOS
enum class TipoVehiculo(val iconRes: Int, val color: Color) {
    CAMION(R.drawable.ic_camion, Color(0xFF3B82F6)),
    FURGONETA(R.drawable.ic_furgoneta, Color(0xFFF59E0B)),
    COCHE(R.drawable.ic_coche, Color(0xFF10B981)),
    MOTO(R.drawable.ic_moto, Color(0xFFEF4444)),
    OTRO(R.drawable.ic_otro, Color(0xFF6B7280))
}

data class Incidencia(
    val id: String,
    val titulo: String,
    val descripcion: String,
    val tipo: TipoIncidencia,
    val prioridad: PrioridadIncidencia,
    val reportadoPor: Usuario,
    val fecha: LocalDate,
    val vehiculo: Vehiculo,
    val estado: EstadoIncidencia
)

enum class TipoIncidencia(val nombre: String) {
    AVERIA("Avería"),
    ACCIDENTE("Accidente"),
    MANTENIMIENTO("Mantenimiento"),
    OTRO("Otro")
}

enum class PrioridadIncidencia(val color: Color, val nombre: String) {
    ALTA(Color(0xFFEF4444), "ALTA PRIORIDAD"),
    MEDIA(Color(0xFFF59E0B), "MEDIA"),
    BAJA(Color(0xFF10B981), "BAJA")
}

enum class EstadoIncidencia {
    ACTIVA,
    RESUELTA
}

/**
 * Pantalla de gestión y visualización de incidencias de los vehículos.
 *
 * - Muestra dos pestañas: “Activas” y “Resueltas”.
 * - Cada incidencia contiene título, descripción, tipo, prioridad y estado.
 * - Permite seleccionar vehículo y navegar al detalle de la incidencia.
 *
 * Elementos destacados:
 * - `Scaffold` con barra superior y navegación inferior.
 * - Uso de `TabRow` o control similar para cambiar entre incidencias activas/resueltas.
 * - Botón flotante (`onAddIncidenciaClick`) para crear nuevas incidencias.
 *
 * Callbacks:
 * - `onBackClick()` → Regresa a la vista anterior.
 * - `onVehiculoClick()` → Abre selector de vehículo.
 * - `onIncidenciaClick(id)` → Navega al detalle de la incidencia.
 * - `onAddIncidenciaClick()` → Crea una nueva incidencia.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncidenciasScreen(
    vehiculoSeleccionado: Vehiculo?,
    incidenciasActivas: List<Incidencia>,
    incidenciasResueltas: List<Incidencia>,
    onBackClick: () -> Unit,
    onVehiculoClick: () -> Unit,
    onIncidenciaClick: (String) -> Unit,
    onAddIncidenciaClick: () -> Unit,
    navController: NavHostController
) {
    var tabSeleccionada by remember { mutableIntStateOf(0) } // 0: Activas, 1: Resueltas
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
                    color = Color(0xFFEF4444)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver",
                                tint = Color.White
                            )
                        }
                        Text(
                            text = "Incidencias",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.weight(1f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.width(48.dp))
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 20.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(15.dp))

                        // Selector de vehículo
                        vehiculoSeleccionado?.let { vehiculo ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .shadow(2.dp, RoundedCornerShape(25.dp))
                                    .clickable(onClick = onVehiculoClick),
                                shape = RoundedCornerShape(25.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(30.dp)
                                            .background(
                                                vehiculo.tipo.color.copy(alpha = 0.1f),
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            painter = painterResource(id = vehiculo.tipo.iconRes),
                                            contentDescription = vehiculo.tipo.name,
                                            tint = vehiculo.tipo.color,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "${vehiculo.nombre} - ${vehiculo.matricula}",
                                        fontSize = 15.sp,
                                        color = Color(0xFF1F2937),
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Cambiar vehículo",
                                        tint = Color(0xFF6B7280)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(15.dp))

                        // Tabs
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            TabButton(
                                text = "Activas (${incidenciasActivas.size})",
                                selected = tabSeleccionada == 0,
                                onClick = { tabSeleccionada = 0 },
                                modifier = Modifier.weight(1f)
                            )
                            TabButton(
                                text = "Resueltas",
                                selected = tabSeleccionada == 1,
                                onClick = { tabSeleccionada = 1 },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    // Lista de incidencias
                    val incidencias =
                        if (tabSeleccionada == 0) incidenciasActivas else incidenciasResueltas

                    if (incidencias.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (tabSeleccionada == 0)
                                        "No hay incidencias activas"
                                    else
                                        "No hay incidencias resueltas",
                                    fontSize = 14.sp,
                                    color = Color(0xFF9CA3AF)
                                )
                            }
                        }
                    } else {
                        items(incidencias) { incidencia ->
                            IncidenciaCard(
                                incidencia = incidencia,
                                onClick = { onIncidenciaClick(incidencia.id) }
                            )
                            Spacer(modifier = Modifier.height(15.dp))
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(20.dp))

                        // Estadísticas
                        Text(
                            text = "Resumen",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F2937)
                        )

                        Spacer(modifier = Modifier.height(15.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            EstadisticaCard(
                                numero = incidenciasActivas.size.toString(),
                                texto = "Incidencias\nactivas",
                                color = Color(0xFFEF4444),
                                modifier = Modifier.weight(1f)
                            )
                            EstadisticaCard(
                                numero = incidenciasResueltas.size.toString(),
                                texto = "Incidencias\nresueltas",
                                color = Color(0xFF10B981),
                                modifier = Modifier.weight(1f)
                            )
                            EstadisticaCard(
                                numero = (incidenciasActivas.size + incidenciasResueltas.size).toString(),
                                texto = "Total\nhistórico",
                                color = Color(0xFF3B82F6),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(120.dp))
                    }
                }

                // Botón flotante añadir incidencia
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 20.dp, bottom = 10.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    FloatingActionButton(
                        onClick = onAddIncidenciaClick,
                        containerColor = Color(0xFFEF4444),
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Añadir incidencia",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    }

@Composable
fun TabButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(45.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) Color(0xFFEF4444) else Color.White,
            contentColor = if (selected) Color.White else Color(0xFF6B7280)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = if (selected) 0.dp else 0.dp
        )
    ) {
        Text(
            text = text,
            fontSize = 15.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun IncidenciaCard(
    incidencia: Incidencia,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Barra de color según prioridad
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .height(110.dp)
                    .background(incidencia.prioridad.color)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Icono
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                incidencia.prioridad.color.copy(alpha = 0.1f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (incidencia.prioridad) {
                                PrioridadIncidencia.ALTA -> Icons.Default.KeyboardArrowUp
                                PrioridadIncidencia.MEDIA -> Icons.Default.KeyboardArrowDown
                                PrioridadIncidencia.BAJA -> Icons.Default.Info
                            },
                            contentDescription = null,
                            tint = incidencia.prioridad.color,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Información
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = incidencia.titulo,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F2937)
                        )
                        Text(
                            text = "Reportado por ${incidencia.reportadoPor.nombre}",
                            fontSize = 13.sp,
                            color = Color(0xFF6B7280)
                        )
                        Text(
                            text = incidencia.fecha.toString(),
                            fontSize = 12.sp,
                            color = Color(0xFF9CA3AF)
                        )
                    }

                    // Flecha
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Ver detalles",
                        tint = Color(0xFF9CA3AF)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Badge de prioridad
                Surface(
                    shape = RoundedCornerShape(11.dp),
                    color = incidencia.prioridad.color.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = incidencia.prioridad.nombre,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = incidencia.prioridad.color,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EstadisticaCard(
    numero: String,
    texto: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(110.dp)
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
            Text(
                text = numero,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = texto,
                fontSize = 12.sp,
                color = Color(0xFF6B7280),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 14.sp
            )
        }
    }
}