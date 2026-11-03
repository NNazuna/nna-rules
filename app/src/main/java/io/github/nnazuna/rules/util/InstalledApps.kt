package io.github.nnazuna.rules.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build

data class AppInfo(
    val packageName: String,
    val label: String,
)

fun loadLaunchableApps(context: Context): List<AppInfo> {
    val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
    val packageManager = context.packageManager
    val resolved = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        packageManager.queryIntentActivities(
            intent,
            PackageManager.ResolveInfoFlags.of(0),
        )
    } else {
        @Suppress("DEPRECATION")
        packageManager.queryIntentActivities(intent, 0)
    }

    return resolved
        .map { info ->
            AppInfo(
                packageName = info.activityInfo.packageName,
                label = info.loadLabel(packageManager).toString(),
            )
        }
        .distinctBy { it.packageName }
        .sortedBy { it.label.lowercase() }
}
