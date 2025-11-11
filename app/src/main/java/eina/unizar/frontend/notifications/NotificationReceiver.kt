package eina.unizar.frontend.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * BroadcastReceiver que recibe las alarmas programadas por NotificationScheduler
 * y delega la creación de notificaciones a NotificationHelper.
 *
 * Flujo de ejecución:
 * 1. NotificationScheduler programa una alarma con AlarmManager
 * 2. Cuando llega el momento, AlarmManager envía un Intent a este receiver
 * 3. Este receiver extrae los datos del Intent y llama al método correspondiente
 *    de NotificationHelper para mostrar la notificación al usuario
 *
 * Debe estar declarado en AndroidManifest.xml con:
 * <receiver
 *     android:name=".notifications.NotificationReceiver"
 *     android:enabled="true"
 *     android:exported="false" />
 */
class NotificationReceiver : BroadcastReceiver() {

    /**
     * Método llamado automáticamente cuando se dispara la alarma programada.
     *
     * @param context Contexto de la aplicación
     * @param intent Intent con los datos de la notificación:
     *               - "type": Tipo de notificación ("reservation" o "maintenance")
     *               - "id": ID único de la reserva o mantenimiento
     *               - "title": Título de la notificación
     *               - "message": Mensaje/contenido de la notificación
     *
     * Comportamiento:
     * - Extrae los datos del Intent
     * - Registra en el log la recepción de la notificación (útil para debugging)
     * - Según el tipo, llama al método apropiado de NotificationHelper
     * - Si el tipo no coincide, no hace nada (ignora silenciosamente)
     */
    override fun onReceive(context: Context, intent: Intent) {
        val type = intent.getStringExtra("type")
        val id = intent.getIntExtra("id", -1)
        val title = intent.getStringExtra("title") ?: ""
        val message = intent.getStringExtra("message") ?: ""

        Log.d("NotificationReceiver", "Received notification: type=$type, id=$id")

        when (type) {
            "reservation" -> {
                NotificationHelper.showReservationNotification(context, id, title, message)
            }
            "maintenance" -> {
                NotificationHelper.showMaintenanceNotification(context, id, title, message)
            }
        }
    }
}
