package io.github.nnazuna.rules.core.action

import io.github.nnazuna.rules.core.event.RuleEvent
import io.github.nnazuna.rules.core.model.RuleDefinition

sealed interface Action {
    val type: String

    fun execute(
        event: RuleEvent,
        rule: RuleDefinition,
        context: ActionContext,
    ): ActionReport
}
