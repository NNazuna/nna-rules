package io.github.nnazuna.rules.core.trigger

import io.github.nnazuna.rules.core.event.RuleEvent
import io.github.nnazuna.rules.core.event.WifiEvent
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

data class WifiTrigger(val ssid: String) : Trigger {
    override val type: String get() = TYPE

    override fun matches(event: RuleEvent): Boolean =
        event is WifiEvent && event.ssid == ssid

    companion object : TriggerPlugin {
        const val TYPE = "WIFI"

        override val typeId: String get() = TYPE

        override fun encode(trigger: Trigger): JsonObject {
            require(trigger is WifiTrigger) {
                "Expected WifiTrigger, got ${trigger::class.simpleName}"
            }
            return buildJsonObject {
                put("ssid", trigger.ssid)
            }
        }

        override fun decode(payload: JsonObject): Trigger {
            val ssid = payload["ssid"]?.jsonPrimitive?.contentOrNull
                ?: throw IllegalArgumentException("Wifi trigger requires ssid")
            return WifiTrigger(ssid)
        }
    }
}
