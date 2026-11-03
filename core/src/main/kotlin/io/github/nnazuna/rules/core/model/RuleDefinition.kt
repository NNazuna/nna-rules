package io.github.nnazuna.rules.core.model

import io.github.nnazuna.rules.core.action.Action
import io.github.nnazuna.rules.core.condition.Condition
import io.github.nnazuna.rules.core.trigger.Trigger

data class RuleDefinition(
    val id: Long = 0L,
    val appLabel: String,
    val enabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val trigger: Trigger,
    val conditions: List<Condition> = emptyList(),
    val actions: List<Action> = emptyList(),
)
