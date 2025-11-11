package eina.unizar.frontend

import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eina.unizar.frontend.models.Vehiculo
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import eina.unizar.frontend.models.NuevaReservaData
import eina.unizar.frontend.models.ReservaRequest
import eina.unizar.frontend.models.ReservaResponse
import eina.unizar.frontend.network.RetrofitClient
import eina.unizar.frontend.viewmodels.AuthViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate
import java.time.LocalTime
import androidx.compose.ui.res.painterResource
import java.time.format.DateTimeFormatter
import eina.unizar.frontend.viewmodels.HomeViewModel
import eina.unizar.frontend.models.toVehiculo
import eina.unizar.frontend.notifications.NotificationScheduler
import java.util.Calendar

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CrearReservaWrapper(
    userId: String,
    token: String,
    onBackClick: () -> Unit,
    onCrearReserva: (NuevaReservaData) -> Unit
) {
    val viewModel = remember { HomeViewModel() }
    val vehiculosDTO by viewModel.vehiculos.collectAsState()
    val vehiculos = vehiculosDTO.map { it.toVehiculo() }

    LaunchedEffect(userId, token) {
        Log.d("CrearReservaWrapper", "Cargando vehículos para userId: $userId")
        viewModel.fetchVehiculos(userId, token)
    }

    LaunchedEffect(vehiculos.size) {
        Log.d("CrearReservaWrapper", "Vehículos cargados: ${vehiculos.size}")
        vehiculos.forEach { vehiculo ->
            Log.d("CrearReservaWrapper", "Vehículo: ${vehiculo.nombre} - ${vehiculo.matricula}")
        }
    }

    NuevaReservaScreen(
        vehiculos = vehiculos,
        onBackClick = onBackClick,
        onCrearReserva = onCrearReserva
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevaReservaScreen(
    vehiculos: List<Vehiculo>,
    onBackClick: () -> Unit,
    onCrearReserva: (NuevaReservaData) -> Unit
) {
    var vehiculoSeleccionado by remember { mutableStateOf<Vehiculo?>(vehiculos.firstOrNull()) }
    var fechaInicio by remember { mutableStateOf(LocalDate.now()) }
    var fechaFin by remember { mutableStateOf(LocalDate.now()) }
    var horaInicio by remember { mutableStateOf("09:00") }
    var horaFin by remember { mutableStateOf("14:00") }
    var tipoSeleccionado by remember { mutableStateOf(TipoReserva.TRABAJO) }
    var notas by remember { mutableStateOf("") }
    var mostrarDatePickerInicio by remember { mutableStateOf(false) }
    var mostrarDatePickerFin by remember { mutableStateOf(false) }
    var expandedVehiculo by remember { mutableStateOf(false) }
    var disponible by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel()
    val scope = rememberCoroutineScope()
    val token by authViewModel.token.collectAsState()

    // Actualizar vehículo seleccionado cuando cambie la lista
    LaunchedEffect(vehiculos) {
        if (vehiculoSeleccionado == null && vehiculos.isNotEmpty()) {
            vehiculoSeleccionado = vehiculos.firstOrNull()
        }
    }

    // DatePicker para fecha inicio
    LaunchedEffect(mostrarDatePickerInicio) {
        if (mostrarDatePickerInicio) {
            val dialog = android.app.DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    fechaInicio = LocalDate.of(year, month + 1, dayOfMonth)
                    mostrarDatePickerInicio = false
                },
                fechaInicio.year,
                fechaInicio.monthValue - 1,
                fechaInicio.dayOfMonth
            )
            dialog.show()
        }
    }

    // DatePicker para fecha fin
    LaunchedEffect(mostrarDatePickerFin) {
        if (mostrarDatePickerFin) {
            val dialog = android.app.DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    fechaFin = LocalDate.of(year, month + 1, dayOfMonth)
                    mostrarDatePickerFin = false
                },
                fechaFin.year,
                fechaFin.monthValue - 1,
                fechaFin.dayOfMonth
            )
            dialog.show()
        }
    }

    Column(
        modifier = Modifier
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
                    text = "Nueva Reserva",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.width(48.dp))
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 30.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Detalles de la reserva",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Selector de vehículo
            Text(
                text = "Vehículo",
                fontSize = 13.sp,
                color = Color(0xFF6B7280),
                modifier = Modifier.padding(bottom = 5.dp)
            )

            if (vehiculos.isEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color(0xFFEF4444),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Cargando vehículos...",
                            fontSize = 15.sp,
                            color = Color(0xFF9CA3AF),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            } else {
                ExposedDropdownMenuBox(
                    expanded = expandedVehiculo,
                    onExpandedChange = { expandedVehiculo = it }
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(55.dp)
                            .menuAnchor()
                            .clickable { expandedVehiculo = true },
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            vehiculoSeleccionado?.let { vehiculo ->
                                val tipoString = vehiculo.tipo.toString().trim().lowercase()
                                val (color, iconRes) = when (tipoString) {
                                    "coche" -> Pair(Color(0xFF3B82F6), R.drawable.ic_coche)
                                    "moto" -> Pair(Color(0xFFF59E0B), R.drawable.ic_moto)
                                    "furgoneta" -> Pair(Color(0xFF10B981), R.drawable.ic_furgoneta)
                                    "camion" -> Pair(Color(0xFFEF4444), R.drawable.ic_camion)
                                    else -> Pair(Color(0xFF6B7280), R.drawable.ic_otro)
                                }

                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .background(color.copy(alpha = 0.1f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(id = iconRes),
                                        contentDescription = vehiculo.tipo.toString(),
                                        modifier = Modifier.size(18.dp),
                                        tint = color
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "${vehiculo.nombre} - ${vehiculo.matricula}",
                                    fontSize = 15.sp,
                                    color = Color(0xFF1F2937),
                                    modifier = Modifier.weight(1f)
                                )
                            } ?: run {
                                Text(
                                    text = "Selecciona un vehículo",
                                    fontSize = 15.sp,
                                    color = Color(0xFF9CA3AF),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Expandir",
                                tint = Color(0xFF9CA3AF)
                            )
                        }
                    }

                    ExposedDropdownMenu(
                        expanded = expandedVehiculo,
                        onDismissRequest = { expandedVehiculo = false }
                    ) {
                        vehiculos.forEach { vehiculo ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        val tipoString = vehiculo.tipo.toString().trim().lowercase()
                                        val (color, iconRes) = when (tipoString) {
                                            "coche" -> Pair(Color(0xFF3B82F6), R.drawable.ic_coche)
                                            "moto" -> Pair(Color(0xFFF59E0B), R.drawable.ic_moto)
                                            "furgoneta" -> Pair(Color(0xFF10B981), R.drawable.ic_furgoneta)
                                            "camion" -> Pair(Color(0xFFEF4444), R.drawable.ic_camion)
                                            else -> Pair(Color(0xFF6B7280), R.drawable.ic_otro)
                                        }

                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .background(color.copy(alpha = 0.1f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                painter = painterResource(id = iconRes),
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp),
                                                tint = color
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = "${vehiculo.nombre} - ${vehiculo.matricula}",
                                            fontSize = 14.sp,
                                            color = Color(0xFF1F2937)
                                        )
                                    }
                                },
                                onClick = {
                                    vehiculoSeleccionado = vehiculo
                                    expandedVehiculo = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Fecha Inicio
            Text(
                text = "Fecha de inicio",
                fontSize = 13.sp,
                color = Color(0xFF6B7280),
                modifier = Modifier.padding(bottom = 5.dp)
            )
            OutlinedTextField(
                value = fechaInicio.toString(),
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { mostrarDatePickerInicio = true },
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFEF4444),
                    unfocusedBorderColor = Color(0xFFE5E7EB)
                ),
                leadingIcon = {
                    Icon(Icons.Default.DateRange, contentDescription = "Fecha Inicio")
                },
                enabled = false
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Fecha Fin
            Text(
                text = "Fecha de fin",
                fontSize = 13.sp,
                color = Color(0xFF6B7280),
                modifier = Modifier.padding(bottom = 5.dp)
            )
            OutlinedTextField(
                value = fechaFin.toString(),
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { mostrarDatePickerFin = true },
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFEF4444),
                    unfocusedBorderColor = Color(0xFFE5E7EB)
                ),
                leadingIcon = {
                    Icon(Icons.Default.DateRange, contentDescription = "Fecha Fin")
                },
                enabled = false
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Hora inicio y fin
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Hora de inicio",
                        fontSize = 13.sp,
                        color = Color(0xFF6B7280),
                        modifier = Modifier.padding(bottom = 5.dp)
                    )
                    OutlinedTextField(
                        value = horaInicio,
                        onValueChange = { horaInicio = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFEF4444),
                            unfocusedBorderColor = Color(0xFFE5E7EB)
                        ),
                        leadingIcon = {
                            Icon(Icons.Default.DateRange, contentDescription = "Hora")
                        }
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Hora de fin",
                        fontSize = 13.sp,
                        color = Color(0xFF6B7280),
                        modifier = Modifier.padding(bottom = 5.dp)
                    )
                    OutlinedTextField(
                        value = horaFin,
                        onValueChange = { horaFin = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFEF4444),
                            unfocusedBorderColor = Color(0xFFE5E7EB)
                        ),
                        leadingIcon = {
                            Icon(Icons.Default.DateRange, contentDescription = "Hora")
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Tipo de reserva
            Text(
                text = "Tipo de reserva",
                fontSize = 13.sp,
                color = Color(0xFF6B7280),
                modifier = Modifier.padding(bottom = 5.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TipoReservaCard(
                    tipo = TipoReserva.TRABAJO,
                    selected = tipoSeleccionado == TipoReserva.TRABAJO,
                    onClick = { tipoSeleccionado = TipoReserva.TRABAJO },
                    modifier = Modifier.weight(1f)
                )
                TipoReservaCard(
                    tipo = TipoReserva.PERSONAL,
                    selected = tipoSeleccionado == TipoReserva.PERSONAL,
                    onClick = { tipoSeleccionado = TipoReserva.PERSONAL },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Notas
            Text(
                text = "Notas (opcional)",
                fontSize = 13.sp,
                color = Color(0xFF6B7280),
                modifier = Modifier.padding(bottom = 5.dp)
            )
            OutlinedTextField(
                value = notas,
                onValueChange = { notas = it },
                placeholder = { Text("Añade detalles sobre tu reserva...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFEF4444),
                    unfocusedBorderColor = Color(0xFFE5E7EB)
                ),
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Mensaje de error
            errorMessage?.let {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFEE2E2)
                    )
                ) {
                    Text(
                        text = it,
                        color = Color(0xFF991B1B),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Verificación de disponibilidad
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (disponible) Color(0xFFECFDF5) else Color(0xFFFEE2E2)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (disponible) Color(0xFF10B981) else Color(0xFFEF4444)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .background(
                                if (disponible) Color(0xFF10B981) else Color(0xFFEF4444),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (disponible) Icons.Default.Check else Icons.Default.Close,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (disponible) "Horario disponible" else "Conflicto detectado",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (disponible) Color(0xFF065F46) else Color(0xFF991B1B)
                        )
                        Text(
                            text = if (disponible)
                                "No hay conflictos con otras reservas"
                            else
                                "Ya existe una reserva en este horario",
                            fontSize = 12.sp,
                            color = if (disponible) Color(0xFF047857) else Color(0xFFDC2626)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Botón Crear reserva
            Button(
                onClick = {
                    vehiculoSeleccionado?.let { vehiculo ->
                        if (token.isNullOrBlank()) {
                            errorMessage = "No se encontró el token de autenticación"
                            return@Button
                        }

                        isLoading = true
                        errorMessage = null

                        val reservaRequest = ReservaRequest(
                            vehiculoId = vehiculo.id,
                            fechaInicio = fechaInicio.format(DateTimeFormatter.ISO_LOCAL_DATE),
                            fechaFinal = fechaFin.format(DateTimeFormatter.ISO_LOCAL_DATE),
                            horaInicio = horaInicio,
                            horaFin = horaFin,
                            tipo = tipoSeleccionado.nombre.uppercase(),
                            notas = notas.ifBlank { null }
                        )
                        Log.d("CrearReserva", "VehiculoId enviado: ${vehiculo.id}") // <-- AÑADE ESTO
                        Log.d("CrearReserva", "Vehiculo completo: $vehiculo")      // <-- Y ESTO
                        Log.d("CrearReserva", "Enviando reserva: $reservaRequest")
                        Log.d("CrearReserva", "Token: Bearer $token")

                        RetrofitClient.instance.crearReserva("Bearer $token", reservaRequest)
                            .enqueue(object : Callback<ReservaResponse> {
                                override fun onResponse(
                                    call: Call<ReservaResponse>,
                                    response: Response<ReservaResponse>
                                ) {
                                    isLoading = false
                                    if (response.isSuccessful) {
                                        val reservaCreada = response.body()
                                        
                                        scope.launch(Dispatchers.Main) {
                                            Toast.makeText(
                                                context,
                                                "Reserva creada exitosamente",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            // Programar notificación 1 hora antes de la reserva
                                            reservaCreada?.let { reserva ->
                                                try {
                                                    // Combinar fecha y hora de inicio
                                                    val fechaHoraReserva = Calendar.getInstance().apply {
                                                        set(Calendar.YEAR, fechaInicio.year)
                                                        set(Calendar.MONTH, fechaInicio.monthValue - 1)
                                                        set(Calendar.DAY_OF_MONTH, fechaInicio.dayOfMonth)
                                                        
                                                        // Parsear hora de inicio (formato "HH:mm")
                                                        val horaPartes = horaInicio.split(":")
                                                        set(Calendar.HOUR_OF_DAY, horaPartes[0].toInt())
                                                        set(Calendar.MINUTE, horaPartes[1].toInt())
                                                        set(Calendar.SECOND, 0)
                                                        set(Calendar.MILLISECOND, 0)
                                                    }
                                                    
                                                    // Programar la notificación
                                                    NotificationScheduler.scheduleReservationNotification(
                                                        context = context,
                                                        reservationId = reserva.id,
                                                        reservationDateTime = fechaHoraReserva.time,
                                                        serviceName = "${vehiculo.nombre} - ${tipoSeleccionado.nombre}"
                                                    )
                                                    
                                                    Log.d("CrearReserva", "Notificación programada para reserva ${reserva.id}")
                                                } catch (e: Exception) {
                                                    Log.e("CrearReserva", "Error al programar notificación", e)
                                                }
                                            }
                                            onCrearReserva(
                                                NuevaReservaData(
                                                    vehiculoId = vehiculo.id,
                                                    fechaInicio = fechaInicio,
                                                    fechaFinal = fechaFin,
                                                    horaInicio = LocalTime.parse(horaInicio),
                                                    horaFin = LocalTime.parse(horaFin),
                                                    tipo = tipoSeleccionado,
                                                    notas = notas
                                                )
                                            )
                                        }
                                    } else {
                                        val errorBody = response.errorBody()?.string() ?: "Error desconocido"
                                        Log.e("CrearReserva", "Error: $errorBody")
                                        scope.launch(Dispatchers.Main) {
                                            errorMessage = extractErrorMessage(errorBody)
                                        }
                                    }
                                }

        override fun onFailure(call: Call<ReservaResponse>, t: Throwable) {
            isLoading = false
            Log.e("CrearReserva", "Error de conexión", t)
            scope.launch(Dispatchers.Main) {
                errorMessage = "Error de conexión: ${t.message}"
            }
        }
    })
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEF4444)
                ),
                enabled = !isLoading && disponible && vehiculoSeleccionado != null
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = "Crear Reserva",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

fun extractErrorMessage(errorBody: String): String {
    val regex = "\"error\":\"(.*?)\"".toRegex()
    val matchResult = regex.find(errorBody)
    return matchResult?.groupValues?.getOrNull(1) ?: "Error al crear la reserva"
}

@Composable
fun TipoReservaCard(
    tipo: TipoReserva,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (selected) tipo.color else Color.White
    val textColor = if (selected) Color.White else Color(0xFF1F2937)
    val borderWidth = if (selected) 0.dp else 1.dp

    Card(
        modifier = modifier
            .height(55.dp)
            .border(borderWidth, Color(0xFFE5E7EB), RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .background(
                        if (selected) Color.White.copy(alpha = 0.2f) else tipo.color.copy(alpha = 0.1f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (tipo == TipoReserva.TRABAJO)
                        Icons.Default.Build
                    else
                        Icons.Default.Person,
                    contentDescription = null,
                    tint = if (selected) Color.White else tipo.color,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = tipo.nombre,
                fontSize = 15.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = textColor
            )
        }
    }
}