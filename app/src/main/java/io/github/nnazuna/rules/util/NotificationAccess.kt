package io.github.nnazuna.rules.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat

fun hasNotificationAccess(context: Context): Boolean {
    val enabled = NotificationManagerCompat.getEnabledListenerPackages(context)
    return enabled.contains(context.packageName)
}

fun notificationAccessSettingsIntent(): Intent =
    Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)

fun openNotificationAccessSettings(context: Context) {
    try {
        context.startActivity(notificationAccessSettingsIntent())
    } catch (_: ActivityNotFoundException) {
        context.startActivity(Intent(Settings.ACTION_SETTINGS))
    }
}
