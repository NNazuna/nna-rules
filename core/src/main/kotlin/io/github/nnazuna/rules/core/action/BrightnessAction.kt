package io.github.nnazuna.rules.core.action

import io.github.nnazuna.rules.core.event.RuleEvent
import io.github.nnazuna.rules.core.model.RuleDefinition
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

data class BrightnessAction(val level: Int) : Action {
    override val type: String get() = TYPE

    override fun execute(event: RuleEvent, rule: RuleDefinition, context: ActionContext): ActionReport {
        context.setBrightness(level)
        return ActionReport()
    }

    companion object : ActionPlugin {
        const val TYPE = "BRIGHTNESS"
        override val typeId: String get() = TYPE

        override fun encode(action: Action): JsonObject {
            require(action is BrightnessAction)
            return buildJsonObject {
                put("level", action.level)
            }
        }

        override fun decode(payload: JsonObject): Action {
            val level = payload["level"]?.jsonPrimitive?.intOrNull
                ?: throw IllegalArgumentException("Brightness action requires level")
            require(level in 0..255) { "Brightness level must be 0-255" }
            return BrightnessAction(level)
        }
    }
}
