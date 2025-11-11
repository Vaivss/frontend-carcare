package eina.unizar.frontend.notifications

import android.content.Context
import android.content.SharedPreferences

/**
 * Gestor de preferencias de notificaciones del usuario.
 *
 * Utiliza SharedPreferences para persistir la configuración de notificaciones
 * entre sesiones de la aplicación.
 *
 * Tipos de notificaciones configurables:
 * - Notificaciones de reservas (recordatorios 1 hora antes)
 * - Notificaciones de mantenimiento (alertas de revisiones)
 *
 * Por defecto, ambos tipos de notificaciones están habilitadas (true).
 */
object NotificationPreferences {
    private const val PREF_NAME = "notification_preferences"
    private const val KEY_RESERVATION_NOTIFICATIONS = "reservation_notifications_enabled"
    private const val KEY_MAINTENANCE_NOTIFICATIONS = "maintenance_notifications_enabled"
    
    /**
     * Obtiene la instancia de SharedPreferences para las preferencias de notificación.
     *
     * @param context Contexto de la aplicación
     * @return Instancia de SharedPreferences en modo privado
     */
    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Verifica si las notificaciones de reservas están habilitadas.
     *
     * @param context Contexto de la aplicación
     * @return true si están habilitadas, false en caso contrario.
     *         Por defecto retorna true si no se ha configurado previamente.
     */
    fun areReservationNotificationsEnabled(context: Context): Boolean {
        return getPreferences(context).getBoolean(KEY_RESERVATION_NOTIFICATIONS, true)
    }

    /**
     * Actualiza el estado de las notificaciones de reservas.
     *
     * @param context Contexto de la aplicación
     * @param enabled true para habilitar, false para deshabilitar
     */
    fun setReservationNotificationsEnabled(context: Context, enabled: Boolean) {
        getPreferences(context).edit()
            .putBoolean(KEY_RESERVATION_NOTIFICATIONS, enabled)
            .apply()
    }

    /**
     * Verifica si las notificaciones de mantenimiento están habilitadas.
     *
     * @param context Contexto de la aplicación
     * @return true si están habilitadas, false en caso contrario.
     *         Por defecto retorna true si no se ha configurado previamente.
     */
    fun areMaintenanceNotificationsEnabled(context: Context): Boolean {
        return getPreferences(context).getBoolean(KEY_MAINTENANCE_NOTIFICATIONS, true)
    }

    /**
     * Actualiza el estado de las notificaciones de mantenimiento.
     *
     * @param context Contexto de la aplicación
     * @param enabled true para habilitar, false para deshabilitar
     */
    fun setMaintenanceNotificationsEnabled(context: Context, enabled: Boolean) {
        getPreferences(context).edit()
            .putBoolean(KEY_MAINTENANCE_NOTIFICATIONS, enabled)
            .apply()
    }
}
