package io.github.nnazuna.rules.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import io.github.nnazuna.rules.NnaRulesApp
import io.github.nnazuna.rules.R
import io.github.nnazuna.rules.action.AndroidActionContext
import io.github.nnazuna.rules.core.action.DigestAction
import io.github.nnazuna.rules.core.engine.RuleEngine
import io.github.nnazuna.rules.core.event.TimeEvent
import io.github.nnazuna.rules.data.RulesDatabase
import io.github.nnazuna.rules.util.ExecutionHistoryStore
import io.github.nnazuna.rules.util.MatchStats
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class ScheduleReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action
        if (action == Intent.ACTION_BOOT_COMPLETED || action == ACTION_TICK) {
            scheduleNextTick(context)
        }

        if (action != ACTION_TICK) return

        val repository = (context.applicationContext as? NnaRulesApp)?.ruleRepository ?: return
        val pendingResult = goAsync()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        scope.launch {
            try {
                val calendar = Calendar.getInstance()
                val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
                val currentMinute = calendar.get(Calendar.MINUTE)

                val definitions = repository.getEnabledDefinitions()
                val database = RulesDatabase.getInstance(context)
                val queueDao = database.notificationQueueDao()
                val actionContext = AndroidActionContext(context, queueDao)

                // 1. Evaluate TimeTriggers
                val timeEvent = TimeEvent(hour = currentHour, minute = currentMinute)
                val timeExecutions = RuleEngine().evaluate(definitions, timeEvent)
                if (timeExecutions.isNotEmpty()) {
                    withContext(Dispatchers.Main.immediate) {
                        timeExecutions.forEach { exec ->
                            exec.rule.actions.forEach { act ->
                                act.execute(exec.event, exec.rule, actionContext)
                            }
                        }
                    }
                    MatchStats.record(context, timeExecutions.map { it.rule.id }, emptyMap())
                    timeExecutions.forEach { exec ->
                        ExecutionHistoryStore.record(
                            context,
                            exec.rule.appLabel,
                            "Time scheduled trigger: %02d:%02d".format(currentHour, currentMinute),
                        )
                    }
                }

                // 2. Check DigestActions matching current hour and minute
                val digestRules = definitions.filter { rule ->
                    rule.actions.filterIsInstance<DigestAction>().any {
                        it.scheduleHour == currentHour && it.scheduleMinute == currentMinute
                    }
                }
                if (digestRules.isNotEmpty()) {
                    val queued = queueDao.getAll()
                    if (queued.isNotEmpty()) {
                        val header = context.getString(R.string.action_digest)
                        val summaryList = queued.take(5).joinToString("\n") { "• ${it.appLabel}: ${it.text.take(50)}" }
                        val more = if (queued.size > 5) "\n+${queued.size - 5} lainnya" else ""
                        val body = "Ringkasan ${queued.size} notifikasi:\n$summaryList$more"

                        withContext(Dispatchers.Main.immediate) {
                            actionContext.postNotification(
                                appLabel = header,
                                text = body,
                                withSound = true,
                            )
                        }
                        queueDao.deleteAll()
                        ExecutionHistoryStore.record(
                            context,
                            header,
                            "Delivered digest of ${queued.size} batched notifications",
                        )
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_TICK = "io.github.nnazuna.rules.action.SCHEDULE_TICK"
        private const val REQUEST_CODE = 9001

        fun scheduleNextTick(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, ScheduleReceiver::class.java).apply {
                action = ACTION_TICK
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

            // Target the next whole minute
            val now = System.currentTimeMillis()
            val nextMinute = (now / 60_000L + 1L) * 60_000L

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    nextMinute,
                    pendingIntent,
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    nextMinute,
                    pendingIntent,
                )
            }
        }
    }
}
