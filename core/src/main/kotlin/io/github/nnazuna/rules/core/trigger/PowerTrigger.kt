package io.github.nnazuna.rules.core.trigger

import io.github.nnazuna.rules.core.event.PowerEvent
import io.github.nnazuna.rules.core.event.RuleEvent
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

data class PowerTrigger(val connected: Boolean) : Trigger {
    override val type: String get() = TYPE

    override fun matches(event: RuleEvent): Boolean =
        event is PowerEvent && event.connected == connected

    companion object : TriggerPlugin {
        const val TYPE = "POWER"

        override val typeId: String get() = TYPE

        override fun encode(trigger: Trigger): JsonObject {
            require(trigger is PowerTrigger) {
                "Expected PowerTrigger, got ${trigger::class.simpleName}"
            }
            return buildJsonObject {
                put("connected", trigger.connected)
            }
        }

        override fun decode(payload: JsonObject): Trigger {
            val raw = payload["connected"]?.jsonPrimitive?.contentOrNull
                ?: throw IllegalArgumentException("Power trigger requires connected boolean")
            val connected = raw.toBooleanStrictOrNull()
                ?: throw IllegalArgumentException("Power trigger connected must be boolean string")
            return PowerTrigger(connected)
        }
    }
}
