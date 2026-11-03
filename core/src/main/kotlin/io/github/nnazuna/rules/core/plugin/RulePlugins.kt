package io.github.nnazuna.rules.core.plugin

import io.github.nnazuna.rules.core.action.BrightnessAction
import io.github.nnazuna.rules.core.action.ClipboardAction
import io.github.nnazuna.rules.core.action.CsvAppendAction
import io.github.nnazuna.rules.core.action.DndAction
import io.github.nnazuna.rules.core.action.NotifyActionPlugin
import io.github.nnazuna.rules.core.action.SoundActionPlugin
import io.github.nnazuna.rules.core.action.VolumeAction
import io.github.nnazuna.rules.core.condition.KeywordCondition
import io.github.nnazuna.rules.core.trigger.NotificationTrigger
import io.github.nnazuna.rules.core.trigger.PowerTrigger
import io.github.nnazuna.rules.core.trigger.TimeTrigger
import io.github.nnazuna.rules.core.trigger.WifiTrigger

object RulePlugins {
    val registry: PluginRegistry = PluginRegistry(
        triggers = listOf(
            NotificationTrigger.Companion,
            TimeTrigger.Companion,
            WifiTrigger.Companion,
            PowerTrigger.Companion,
        ),
        conditions = listOf(KeywordCondition.Companion),
        actions = listOf(
            NotifyActionPlugin,
            SoundActionPlugin,
            ClipboardAction.Companion,
            VolumeAction.Companion,
            BrightnessAction.Companion,
            DndAction.Companion,
            CsvAppendAction.Companion,
        ),
    )
}
