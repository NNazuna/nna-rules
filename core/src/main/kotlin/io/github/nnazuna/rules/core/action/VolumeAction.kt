package io.github.nnazuna.rules.core.action

import io.github.nnazuna.rules.core.event.RuleEvent
import io.github.nnazuna.rules.core.model.RuleDefinition
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

data class VolumeAction(val level: Int, val streamType: String = "RING") : Action {
    override val type: String get() = TYPE

    override fun execute(event: RuleEvent, rule: RuleDefinition, context: ActionContext): ActionReport {
        context.setVolume(level, streamType)
        return ActionReport()
    }

    companion object : ActionPlugin {
        const val TYPE = "VOLUME"
        override val typeId: String get() = TYPE

        override fun encode(action: Action): JsonObject {
            require(action is VolumeAction)
            return buildJsonObject {
                put("level", action.level)
                put("streamType", action.streamType)
            }
        }

        override fun decode(payload: JsonObject): Action {
            val level = payload["level"]?.jsonPrimitive?.intOrNull
                ?: throw IllegalArgumentException("Volume action requires level")
            val streamType = payload["streamType"]?.jsonPrimitive?.contentOrNull ?: "RING"
            require(level in 0..100) { "Volume level must be 0-100" }
            return VolumeAction(level, streamType)
        }
    }
}
