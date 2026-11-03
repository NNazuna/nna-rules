package io.github.nnazuna.rules.action

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.PersistableBundle
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import io.github.nnazuna.rules.R
import io.github.nnazuna.rules.data.ActionType
import io.github.nnazuna.rules.data.Rule
import io.github.nnazuna.rules.extraction.ClipboardOutcome
import io.github.nnazuna.rules.extraction.TextExtractor
import io.github.nnazuna.rules.service.NotificationContent
import java.util.concurrent.atomic.AtomicInteger

object ActionExecutor {
    private const val CHANNEL_SILENT = "rule_notifications"
    private const val CHANNEL_SOUND = "rule_alerts"
    private val notificationIds = AtomicInteger(1000)

    fun execute(context: Context, rule: Rule, content: NotificationContent): ClipboardOutcome? =
        when (rule.action) {
            ActionType.NOTIFY -> {
                postNotification(context, rule, content, withSound = false)
                null
            }
            ActionType.SOUND -> {
                postNotification(context, rule, content, withSound = true)
                null
            }
            ActionType.CLIPBOARD -> copyToClipboard(context, rule, content)
        }

    private fun postNotification(
        context: Context,
        rule: Rule,
        content: NotificationContent,
        withSound: Boolean,
    ) {
        ensureChannels(context)
        val channelId = if (withSound) CHANNEL_SOUND else CHANNEL_SILENT
        val body = context.getString(
            R.string.rule_notification_body,
            rule.appLabel,
            content.searchableText,
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_stat_nna)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setPriority(
                if (withSound) {
                    NotificationCompat.PRIORITY_DEFAULT
                } else {
                    NotificationCompat.PRIORITY_LOW
                },
            )
            .build()

        try {
            NotificationManagerCompat.from(context)
                .notify(notificationIds.incrementAndGet(), notification)
        } catch (_: SecurityException) {
            // POST_NOTIFICATIONS was revoked; nothing to do.
        }
    }

    private fun copyToClipboard(
        context: Context,
        rule: Rule,
        content: NotificationContent,
    ): ClipboardOutcome? {
        val resolved = TextExtractor.resolveForClipboard(
            text = content.searchableText,
            mode = rule.clipboardMode,
            extractionMode = rule.extractionMode,
            customRegex = rule.customRegex,
        )
        if (resolved.text.isBlank()) return null
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            ?: return null
        val clip = ClipData.newPlainText(
            context.getString(R.string.app_name),
            resolved.text,
        )
        markSensitive(clip)
        clipboard.setPrimaryClip(clip)
        ClipboardClearScheduler.schedule(context, rule.clearClipboardSeconds)
        return resolved.outcome
    }

    private fun markSensitive(clip: ClipData) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        clip.description.extras = PersistableBundle().apply {
            putBoolean(ClipDescription.EXTRA_IS_SENSITIVE, true)
        }
    }

    private fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return

        if (manager.getNotificationChannel(CHANNEL_SILENT) == null) {
            val silent = NotificationChannel(
                CHANNEL_SILENT,
                context.getString(R.string.channel_silent_name),
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = context.getString(R.string.channel_silent_description)
                setSound(null, null)
                enableVibration(false)
            }
            manager.createNotificationChannel(silent)
        }

        if (manager.getNotificationChannel(CHANNEL_SOUND) == null) {
            val sound = NotificationChannel(
                CHANNEL_SOUND,
                context.getString(R.string.channel_sound_name),
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = context.getString(R.string.channel_sound_description)
                val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val attributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build()
                setSound(uri, attributes)
                enableVibration(true)
            }
            manager.createNotificationChannel(sound)
        }
    }
}
