package eina.unizar.frontend.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import java.util.*

/**
 * Gestor de programación de notificaciones usando AlarmManager.
 *
 * Responsabilidades:
 * - Programar alarmas exactas para recordatorios de reservas (1 hora antes)
 * - Programar alarmas para recordatorios de mantenimiento (día del mantenimiento a las 9:00 AM)
 * - Cancelar notificaciones programadas cuando sea necesario
 * - Respetar las preferencias del usuario antes de programar
 * - Manejar diferencias entre versiones de Android (especialmente Android 12+)
 *
 * Las alarmas programadas disparan un NotificationReceiver que muestra la notificación.
 */
object NotificationScheduler {

    /**
     * Programa una notificación de recordatorio de reserva.
     *
     * La notificación se mostrará 1 hora antes de la fecha/hora de la reserva.
     *
     * @param context Contexto de la aplicación
     * @param reservationId ID único de la reserva (usado como requestCode del PendingIntent)
     * @param reservationDateTime Fecha y hora exacta de la reserva
     * @param serviceName Nombre del servicio/tipo de reserva (ej: "Cambio de aceite")
     *
     * Comportamiento:
     * - Si las notificaciones de reservas están deshabilitadas, no hace nada
     * - Solo programa si la notificación es en el futuro
     * - En Android 12+, verifica permiso canScheduleExactAlarms()
     * - Usa setExactAndAllowWhileIdle() para que funcione en modo Doze
     * - Maneja SecurityException si no hay permisos
     */
    fun scheduleReservationNotification(
        context: Context,
        reservationId: Int,
        reservationDateTime: Date,
        serviceName: String
    ) {
        if (!NotificationPreferences.areReservationNotificationsEnabled(context)) {
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Notificación 1 hora antes
        val notificationTime = Calendar.getInstance().apply {
            time = reservationDateTime
            add(Calendar.HOUR_OF_DAY, -1)
        }

        // Solo programar si la notificación es en el futuro
        if (notificationTime.timeInMillis > System.currentTimeMillis()) {
            val intent = Intent(context, NotificationReceiver::class.java).apply {
                putExtra("type", "reservation")
                putExtra("id", reservationId)
                putExtra("title", "Recordatorio de Reserva")
                putExtra("message", "Tu cita de $serviceName es en 1 hora")
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                reservationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            notificationTime.timeInMillis,
                            pendingIntent
                        )
                        Log.d("NotificationScheduler", "Reservation notification scheduled for $notificationTime")
                    }
                } else {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        notificationTime.timeInMillis,
                        pendingIntent
                    )
                }
            } catch (e: SecurityException) {
                Log.e("NotificationScheduler", "Error scheduling alarm", e)
            }
        }
    }

    /**
     * Programa una notificación de recordatorio de mantenimiento.
     *
     * La notificación se mostrará el día del mantenimiento a las 9:00 AM.
     *
     * @param context Contexto de la aplicación
     * @param maintenanceId ID único del mantenimiento
     * @param dueDate Fecha en que toca el mantenimiento
     * @param maintenanceType Tipo de mantenimiento (ej: "Revisión de frenos")
     *
     * Comportamiento:
     * - Si las notificaciones de mantenimiento están deshabilitadas, no hace nada
     * - Programa la notificación para las 9:00 AM del día indicado
     * - Solo programa si es en el futuro
     * - Usa requestCode distinto (maintenanceId + 10000) para evitar colisiones con reservas
     */
    fun scheduleMaintenanceNotification(
        context: Context,
        maintenanceId: Int,
        dueDate: Date,
        maintenanceType: String
    ) {
        if (!NotificationPreferences.areMaintenanceNotificationsEnabled(context)) {
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Notificación el día del mantenimiento a las 9:00 AM
        val notificationTime = Calendar.getInstance().apply {
            time = dueDate
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        // Solo programar si la notificación es en el futuro
        if (notificationTime.timeInMillis > System.currentTimeMillis()) {
            val intent = Intent(context, NotificationReceiver::class.java).apply {
                putExtra("type", "maintenance")
                putExtra("id", maintenanceId)
                putExtra("title", "Recordatorio de Mantenimiento")
                putExtra("message", "Hoy toca: $maintenanceType")
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                maintenanceId + 10000,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            notificationTime.timeInMillis,
                            pendingIntent
                        )
                        Log.d("NotificationScheduler", "Maintenance notification scheduled for $notificationTime")
                    }
                } else {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        notificationTime.timeInMillis,
                        pendingIntent
                    )
                }
            } catch (e: SecurityException) {
                Log.e("NotificationScheduler", "Error scheduling alarm", e)
            }
        }
    }

    /**
     * Cancela una notificación de reserva previamente programada.
     *
     * Útil cuando:
     * - El usuario cancela una reserva
     * - Se modifica la fecha/hora de una reserva (cancelar la anterior y crear una nueva)
     * - El usuario desactiva todas las notificaciones de reservas
     *
     * @param context Contexto de la aplicación
     * @param reservationId ID de la reserva cuya notificación se quiere cancelar
     */
    fun cancelReservationNotification(context: Context, reservationId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reservationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    /**
     * Cancela una notificación de mantenimiento previamente programada.
     *
     * Útil cuando:
     * - Se completa el mantenimiento antes de tiempo
     * - Se pospone el mantenimiento (cancelar y reprogramar)
     * - El usuario desactiva todas las notificaciones de mantenimiento
     *
     * @param context Contexto de la aplicación
     * @param maintenanceId ID del mantenimiento cuya notificación se quiere cancelar
     */
    fun cancelMaintenanceNotification(context: Context, maintenanceId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            maintenanceId + 10000,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
