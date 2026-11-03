package io.github.nnazuna.rules.core.trigger

import io.github.nnazuna.rules.core.event.RuleEvent
import io.github.nnazuna.rules.core.event.TimeEvent
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

data class TimeTrigger(val hour: Int, val minute: Int) : Trigger {
    override val type: String get() = TYPE

    override fun matches(event: RuleEvent): Boolean {
        if (event !is TimeEvent) return false
        val eventHour = (event.timestamp / (60 * 60 * 1000) % 24).toInt()
        val eventMinute = (event.timestamp / (60 * 1000) % 60).toInt()
        return eventHour == hour && eventMinute == minute
    }

    companion object : TriggerPlugin {
        const val TYPE = "TIME"

        override val typeId: String get() = TYPE

        override fun encode(trigger: Trigger): JsonObject {
            require(trigger is TimeTrigger) {
                "Expected TimeTrigger, got ${trigger::class.simpleName}"
            }
            return buildJsonObject {
                put("hour", trigger.hour)
                put("minute", trigger.minute)
            }
        }

        override fun decode(payload: JsonObject): Trigger {
            val hour = payload["hour"]?.jsonPrimitive?.intOrNull
                ?: throw IllegalArgumentException("Time trigger requires hour")
            val minute = payload["minute"]?.jsonPrimitive?.intOrNull
                ?: throw IllegalArgumentException("Time trigger requires minute")
            require(hour in 0..23) { "Hour must be 0-23" }
            require(minute in 0..59) { "Minute must be 0-59" }
            return TimeTrigger(hour, minute)
        }
    }
}
