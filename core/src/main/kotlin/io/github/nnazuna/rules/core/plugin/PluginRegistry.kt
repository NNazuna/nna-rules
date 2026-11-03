package io.github.nnazuna.rules.core.plugin

import io.github.nnazuna.rules.core.action.ActionPlugin
import io.github.nnazuna.rules.core.condition.ConditionPlugin
import io.github.nnazuna.rules.core.trigger.TriggerPlugin

class PluginRegistry(
    triggers: List<TriggerPlugin> = emptyList(),
    conditions: List<ConditionPlugin> = emptyList(),
    actions: List<ActionPlugin> = emptyList(),
) {
    private val triggerPlugins = index(triggers) { it.typeId }
    private val conditionPlugins = index(conditions) { it.typeId }
    private val actionPlugins = index(actions) { it.typeId }

    fun trigger(type: String): TriggerPlugin? = triggerPlugins[type]

    fun condition(type: String): ConditionPlugin? = conditionPlugins[type]

    fun action(type: String): ActionPlugin? = actionPlugins[type]

    private fun <T> index(items: List<T>, typeId: (T) -> String): Map<String, T> {
        val result = LinkedHashMap<String, T>(items.size)
        items.forEach { item ->
            val id = typeId(item)
            check(result.put(id, item) == null) { "Duplicate plugin type: $id" }
        }
        return result
    }
}
