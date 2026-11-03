package io.github.nnazuna.rules.core.engine

import io.github.nnazuna.rules.core.event.RuleEvent
import io.github.nnazuna.rules.core.model.RuleDefinition

data class RuleExecution(
    val rule: RuleDefinition,
    val event: RuleEvent,
)
