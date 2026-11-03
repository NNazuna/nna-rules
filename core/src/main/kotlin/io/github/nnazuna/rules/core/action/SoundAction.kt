package io.github.nnazuna.rules.core.action

import io.github.nnazuna.rules.core.event.NotificationEvent
import io.github.nnazuna.rules.core.event.RuleEvent
import io.github.nnazuna.rules.core.model.RuleDefinition
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject

data object SoundAction : Action {
    const val TYPE = "SOUND"

    override val type: String get() = TYPE

    override fun execute(
        event: RuleEvent,
        rule: RuleDefinition,
        context: ActionContext,
    ): ActionReport {
        val notification = event as? NotificationEvent ?: return ActionReport()
        context.postNotification(
            appLabel = rule.appLabel,
            text = notification.searchableText,
            withSound = true,
        )
        return ActionReport()
    }
}

object SoundActionPlugin : ActionPlugin {
    override val typeId: String get() = SoundAction.TYPE

    override fun encode(action: Action): JsonObject = buildJsonObject { }

    override fun decode(payload: JsonObject): Action = SoundAction
}
