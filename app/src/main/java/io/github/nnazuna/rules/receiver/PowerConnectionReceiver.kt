package io.github.nnazuna.rules.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.github.nnazuna.rules.NnaRulesApp
import io.github.nnazuna.rules.action.AndroidActionContext
import io.github.nnazuna.rules.core.engine.RuleEngine
import io.github.nnazuna.rules.core.event.PowerEvent
import io.github.nnazuna.rules.data.RulesDatabase
import io.github.nnazuna.rules.util.ExecutionHistoryStore
import io.github.nnazuna.rules.util.MatchStats
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PowerConnectionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        val connected = when (action) {
            Intent.ACTION_POWER_CONNECTED -> true
            Intent.ACTION_POWER_DISCONNECTED -> false
            else -> return
        }

        val repository = (context.applicationContext as? NnaRulesApp)?.ruleRepository ?: return
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val pendingResult = goAsync()

        scope.launch {
            try {
                val definitions = repository.getEnabledDefinitions()
                val event = PowerEvent(connected = connected)
                val executions = RuleEngine().evaluate(definitions, event)
                if (executions.isNotEmpty()) {
                    withContext(Dispatchers.Main.immediate) {
                        val queueDao = RulesDatabase.getInstance(context).notificationQueueDao()
                        val actionContext = AndroidActionContext(context, queueDao)
                        executions.forEach { exec ->
                            exec.rule.actions.forEach { act ->
                                act.execute(exec.event, exec.rule, actionContext)
                            }
                        }
                    }
                    MatchStats.record(context, executions.map { it.rule.id }, emptyMap())
                    executions.forEach { exec ->
                        ExecutionHistoryStore.record(
                            context,
                            exec.rule.appLabel,
                            if (connected) "Power connected" else "Power disconnected",
                        )
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
