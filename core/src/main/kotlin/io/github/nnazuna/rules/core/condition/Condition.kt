package io.github.nnazuna.rules.core.condition

import io.github.nnazuna.rules.core.event.RuleEvent

sealed interface Condition {
    val type: String

    fun matches(event: RuleEvent): Boolean
}
