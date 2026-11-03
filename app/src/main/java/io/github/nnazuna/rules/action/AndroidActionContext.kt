package io.github.nnazuna.rules.action

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.RingtoneManager
import android.os.Build
import android.os.PersistableBundle
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import io.github.nnazuna.rules.R
import io.github.nnazuna.rules.core.action.ActionContext
import java.util.concurrent.atomic.AtomicInteger

class AndroidActionContext(private val context: Context) : ActionContext {
    override fun postNotification(appLabel: String, text: String, withSound: Boolean) {
        ensureChannels()
        val channelId = if (withSound) CHANNEL_SOUND else CHANNEL_SILENT
        val body = context.getString(R.string.rule_notification_body, appLabel, text)

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
        }
    }

    override fun copyToClipboard(text: String, clearAfterSeconds: Int) {
        if (text.isBlank()) return
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            ?: return
        val clip = ClipData.newPlainText(context.getString(R.string.app_name), text)
        markSensitive(clip)
        clipboard.setPrimaryClip(clip)
        ClipboardClearScheduler.schedule(context, clearAfterSeconds)
    }

    override fun setVolume(level: Int, streamType: String) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return
        val stream = when (streamType.uppercase()) {
            "MUSIC" -> AudioManager.STREAM_MUSIC
            "ALARM" -> AudioManager.STREAM_ALARM
            "NOTIFICATION" -> AudioManager.STREAM_NOTIFICATION
            else -> AudioManager.STREAM_RING
        }
        val max = audioManager.getStreamMaxVolume(stream)
        val target = (level.coerceIn(0, 100) * max / 100f).toInt()
        try {
            audioManager.setStreamVolume(stream, target, 0)
        } catch (_: SecurityException) {
        }
    }

    override fun setBrightness(level: Int) {
        try {
            val brightness = level.coerceIn(0, 255)
            Settings.System.putInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                brightness,
            )
        } catch (_: Exception) {
        }
    }

    override fun setDndMode(mode: String) {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        try {
            val filter = when (mode.uppercase()) {
                "PRIORITY" -> NotificationManager.INTERRUPTION_FILTER_PRIORITY
                "SILENT" -> NotificationManager.INTERRUPTION_FILTER_NONE
                "ALARMS" -> NotificationManager.INTERRUPTION_FILTER_ALARMS
                else -> NotificationManager.INTERRUPTION_FILTER_ALL
            }
            if (manager.isNotificationPolicyAccessGranted) {
                manager.setInterruptionFilter(filter)
            }
        } catch (_: Exception) {
        }
    }

    private fun markSensitive(clip: ClipData) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        clip.description.extras = PersistableBundle().apply {
            putBoolean(ClipDescription.EXTRA_IS_SENSITIVE, true)
        }
    }

    private fun ensureChannels() {
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

    private companion object {
        const val CHANNEL_SILENT = "rule_notifications"
        const val CHANNEL_SOUND = "rule_alerts"
        val notificationIds = AtomicInteger(1000)
    }
}
