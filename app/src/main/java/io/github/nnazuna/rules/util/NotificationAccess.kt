package io.github.nnazuna.rules.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
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

fun openAppNotificationSettings(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        try {
            context.startActivity(
                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName),
            )
            return
        } catch (_: ActivityNotFoundException) {
            // Fall through to the app details screen.
        }
    }

    context.startActivity(
        Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse("package:${context.packageName}"),
        ),
    )
}
