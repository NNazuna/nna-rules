package io.github.nnazuna.rules.core.engine

import io.github.nnazuna.rules.core.event.RuleEvent
import io.github.nnazuna.rules.core.model.RuleDefinition

class RuleEngine {
    fun evaluate(
        rules: List<RuleDefinition>,
        event: RuleEvent,
    ): List<RuleExecution> = rules.asSequence()
        .filter { it.enabled }
        .filter { rule -> rule.trigger.matches(event) }
        .filter { rule -> rule.conditions.all { condition -> condition.matches(event) } }
        .map { rule -> RuleExecution(rule = rule, event = event) }
        .toList()
}
