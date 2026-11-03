package io.github.nnazuna.rules.core.trigger

import io.github.nnazuna.rules.core.event.NotificationEvent
import io.github.nnazuna.rules.core.event.RuleEvent
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

data class NotificationTrigger(val packageName: String) : Trigger {
    override val type: String get() = TYPE

    override fun matches(event: RuleEvent): Boolean =
        event is NotificationEvent && event.packageName == packageName

    companion object : TriggerPlugin {
        const val TYPE = "NOTIFICATION"

        override val typeId: String get() = TYPE

        override fun encode(trigger: Trigger): JsonObject {
            require(trigger is NotificationTrigger) {
                "Expected NotificationTrigger, got ${trigger::class.simpleName}"
            }
            return buildJsonObject {
                put("packageName", trigger.packageName)
            }
        }

        override fun decode(payload: JsonObject): Trigger {
            val packageName = payload["packageName"]?.jsonPrimitive?.contentOrNull
            require(!packageName.isNullOrBlank()) {
                "Notification trigger requires a non-blank packageName"
            }
            return NotificationTrigger(packageName)
        }
    }
}
