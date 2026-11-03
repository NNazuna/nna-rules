package io.github.nnazuna.rules.core.plugin

import io.github.nnazuna.rules.core.action.ClipboardAction
import io.github.nnazuna.rules.core.action.NotifyAction
import io.github.nnazuna.rules.core.action.NotifyActionPlugin
import io.github.nnazuna.rules.core.condition.KeywordCondition
import io.github.nnazuna.rules.core.trigger.NotificationTrigger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class PluginRegistryTest {
    @Test
    fun resolvesRegisteredPluginsByType() {
        val registry = RulePlugins.registry

        assertEquals(NotificationTrigger.Companion, registry.trigger(NotificationTrigger.TYPE))
        assertEquals(KeywordCondition.Companion, registry.condition(KeywordCondition.TYPE))
        assertEquals(NotifyActionPlugin, registry.action(NotifyAction.TYPE))
        assertEquals(ClipboardAction.Companion, registry.action(ClipboardAction.TYPE))
        assertNull(registry.action("UNKNOWN"))
    }

    @Test
    fun duplicateTriggerTypeFailsFast() {
        assertThrows(IllegalStateException::class.java) {
            PluginRegistry(
                triggers = listOf(
                    NotificationTrigger.Companion,
                    NotificationTrigger.Companion,
                ),
            )
        }
    }

    @Test
    fun duplicateActionTypeFailsFast() {
        assertThrows(IllegalStateException::class.java) {
            PluginRegistry(
                actions = listOf(NotifyActionPlugin, NotifyActionPlugin),
            )
        }
    }
}
