package io.github.nnazuna.rules.util

import android.content.Context

data class CapturedNotification(
    val packageName: String,
    val appLabel: String,
    val text: String,
    val capturedAt: Long = System.currentTimeMillis(),
)

object LastNotificationStore {
    private const val PREFS_NAME = "nna_rules_last_notification"
    private const val KEY_PACKAGE = "package_name"
    private const val KEY_LABEL = "app_label"
    private const val KEY_TEXT = "text"
    private const val KEY_TIME = "captured_at"

    fun save(context: Context, captured: CapturedNotification) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_PACKAGE, captured.packageName)
            .putString(KEY_LABEL, captured.appLabel)
            .putString(KEY_TEXT, captured.text)
            .putLong(KEY_TIME, captured.capturedAt)
            .apply()
    }

    fun load(context: Context): CapturedNotification? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val packageName = prefs.getString(KEY_PACKAGE, null) ?: return null
        val label = prefs.getString(KEY_LABEL, null) ?: packageName
        val text = prefs.getString(KEY_TEXT, null).orEmpty()
        val capturedAt = prefs.getLong(KEY_TIME, 0L)
        return CapturedNotification(
            packageName = packageName,
            appLabel = label,
            text = text,
            capturedAt = capturedAt,
        )
    }
}
