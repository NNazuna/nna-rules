package io.github.nnazuna.rules.core.plugin

import io.github.nnazuna.rules.core.action.ClipboardAction
import io.github.nnazuna.rules.core.action.NotifyActionPlugin
import io.github.nnazuna.rules.core.action.SoundActionPlugin
import io.github.nnazuna.rules.core.condition.KeywordCondition
import io.github.nnazuna.rules.core.trigger.NotificationTrigger

object RulePlugins {
    val registry: PluginRegistry = PluginRegistry(
        triggers = listOf(NotificationTrigger.Companion),
        conditions = listOf(KeywordCondition.Companion),
        actions = listOf(
            NotifyActionPlugin,
            SoundActionPlugin,
            ClipboardAction.Companion,
        ),
    )
}
