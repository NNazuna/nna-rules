package io.github.nnazuna.rules.core.action

import io.github.nnazuna.rules.core.event.NotificationEvent
import io.github.nnazuna.rules.core.event.RuleEvent
import io.github.nnazuna.rules.core.model.RuleDefinition
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

data class DigestAction(val scheduleHour: Int = 18, val scheduleMinute: Int = 0) : Action {
    override val type: String get() = TYPE

    override fun execute(event: RuleEvent, rule: RuleDefinition, context: ActionContext): ActionReport {
        val notification = event as? NotificationEvent ?: return ActionReport()
        context.queueNotification(
            ruleId = rule.id,
            packageName = notification.packageName,
            appLabel = rule.appLabel,
            text = notification.searchableText,
        )
        return ActionReport()
    }

    companion object : ActionPlugin {
        const val TYPE = "DIGEST"
        override val typeId: String get() = TYPE

        override fun encode(action: Action): JsonObject {
            require(action is DigestAction)
            return buildJsonObject {
                put("scheduleHour", action.scheduleHour)
                put("scheduleMinute", action.scheduleMinute)
            }
        }

        override fun decode(payload: JsonObject): Action {
            val hour = payload["scheduleHour"]?.jsonPrimitive?.contentOrNull?.toIntOrNull() ?: 18
            val minute = payload["scheduleMinute"]?.jsonPrimitive?.contentOrNull?.toIntOrNull() ?: 0
            require(hour in 0..23) { "Schedule hour must be 0-23" }
            require(minute in 0..59) { "Schedule minute must be 0-59" }
            return DigestAction(hour, minute)
        }
    }
}
