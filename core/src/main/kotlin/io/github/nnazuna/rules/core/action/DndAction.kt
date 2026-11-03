package io.github.nnazuna.rules.core.action

import io.github.nnazuna.rules.core.event.RuleEvent
import io.github.nnazuna.rules.core.model.RuleDefinition
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

data class DndAction(val mode: String) : Action {
    override val type: String get() = TYPE

    override fun execute(event: RuleEvent, rule: RuleDefinition, context: ActionContext): ActionReport {
        context.setDndMode(mode)
        return ActionReport()
    }

    companion object : ActionPlugin {
        const val TYPE = "DND"
        override val typeId: String get() = TYPE

        override fun encode(action: Action): JsonObject {
            require(action is DndAction)
            return buildJsonObject {
                put("mode", action.mode)
            }
        }

        override fun decode(payload: JsonObject): Action {
            val mode = payload["mode"]?.jsonPrimitive?.contentOrNull
                ?: throw IllegalArgumentException("DND action requires mode")
            require(mode in listOf("OFF", "PRIORITY", "SILENT", "ALARMS")) {
                "Invalid DND mode"
            }
            return DndAction(mode)
        }
    }
}
