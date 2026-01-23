package org.khlug.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import org.khlug.service.NotificationForwarderService

object NotificationPermissionUtil {

    fun isNotificationListenerEnabled(context: Context): Boolean {
        val componentName = ComponentName(context, NotificationForwarderService::class.java)
        val enabledListeners = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        ) ?: return false

        return enabledListeners.contains(componentName.flattenToString())
    }

    fun createNotificationListenerSettingsIntent(): Intent {
        return Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
    }
}
