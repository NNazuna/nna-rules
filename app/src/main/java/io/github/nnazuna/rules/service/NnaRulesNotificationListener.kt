package io.github.nnazuna.rules.service

import android.app.Notification
import android.content.pm.PackageManager
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import io.github.nnazuna.rules.NnaRulesApp
import io.github.nnazuna.rules.action.ActionExecutor
import io.github.nnazuna.rules.util.CapturedNotification
import io.github.nnazuna.rules.util.LastNotificationStore
import io.github.nnazuna.rules.util.MatchStats
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NnaRulesNotificationListener : NotificationListenerService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val recentNotifications = mutableMapOf<String, Pair<String, Long>>()

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        val notification = sbn ?: return
        if (notification.packageName == packageName) return
        if (notification.notification.flags and Notification.FLAG_GROUP_SUMMARY != 0) return

        val content = extractContent(notification) ?: return
        if (!shouldProcess(notification.key, content)) return

        val captured = CapturedNotification(
            packageName = notification.packageName,
            appLabel = resolveAppLabel(notification.packageName),
            text = content.searchableText,
        )
        LastNotificationStore.save(this, captured)

        val repository = (application as NnaRulesApp).ruleRepository
        scope.launch {
            val matches = repository.getEnabledRules()
                .filter { RuleMatcher.matches(it, notification.packageName, content) }
            if (matches.isNotEmpty()) {
                val clipboardOutcomes = withContext(Dispatchers.Main.immediate) {
                    matches.associate { rule ->
                        rule.id to ActionExecutor.execute(
                            this@NnaRulesNotificationListener,
                            rule,
                            content,
                        )
                    }
                }
                MatchStats.record(
                    this@NnaRulesNotificationListener,
                    matches.map { it.id },
                    clipboardOutcomes,
                )
            }
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private fun extractContent(sbn: StatusBarNotification): NotificationContent? {
        val extras = sbn.notification.extras ?: return null

        val title = extras.getCharSequence(Notification.EXTRA_TITLE)
            ?.toString()
            ?.trim()
            .orEmpty()

        val lines = buildList {
            extras.getCharSequence(Notification.EXTRA_TEXT)
                ?.toString()
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
                ?.let(::add)
            extras.getCharSequence(Notification.EXTRA_BIG_TEXT)
                ?.toString()
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
                ?.let(::add)
            extras.getCharSequence(Notification.EXTRA_SUB_TEXT)
                ?.toString()
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
                ?.let(::add)
            extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)
                ?.forEach { line ->
                    line?.toString()
                        ?.trim()
                        ?.takeIf { it.isNotEmpty() }
                        ?.let(::add)
                }
        }.distinct()

        val text = lines.joinToString("\n")
        if (title.isEmpty() && text.isEmpty()) return null
        return NotificationContent(title = title, text = text)
    }

    private fun shouldProcess(key: String, content: NotificationContent): Boolean {
        val now = System.currentTimeMillis()
        val signature = "${content.title}\u0000${content.text}"
        synchronized(recentNotifications) {
            val previous = recentNotifications[key]
            recentNotifications[key] = signature to now
            if (recentNotifications.size > 100) {
                recentNotifications.entries.removeAll { now - it.value.second > 60_000 }
            }
            return previous == null ||
                previous.first != signature ||
                now - previous.second > DUPLICATE_WINDOW_MS
        }
    }

    private fun resolveAppLabel(pkg: String): String = try {
        val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getApplicationInfo(pkg, PackageManager.ApplicationInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            packageManager.getApplicationInfo(pkg, 0)
        }
        packageManager.getApplicationLabel(info).toString()
    } catch (_: PackageManager.NameNotFoundException) {
        pkg
    } catch (_: SecurityException) {
        pkg
    }

    private companion object {
        const val DUPLICATE_WINDOW_MS = 2_000L
    }
}
