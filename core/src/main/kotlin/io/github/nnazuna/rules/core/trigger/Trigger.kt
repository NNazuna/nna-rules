package io.github.nnazuna.rules.core.trigger

import io.github.nnazuna.rules.core.event.RuleEvent

sealed interface Trigger {
    val type: String

    fun matches(event: RuleEvent): Boolean
}
