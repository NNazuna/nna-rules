package io.github.nnazuna.rules.core.codec

import io.github.nnazuna.rules.core.action.ClipboardAction
import io.github.nnazuna.rules.core.action.NotifyAction
import io.github.nnazuna.rules.core.action.SoundAction
import io.github.nnazuna.rules.core.condition.KeywordCondition
import io.github.nnazuna.rules.core.extraction.ClipboardMode
import io.github.nnazuna.rules.core.extraction.ExtractionMode
import io.github.nnazuna.rules.core.model.RuleDefinition
import io.github.nnazuna.rules.core.plugin.RulePlugins
import io.github.nnazuna.rules.core.trigger.NotificationTrigger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class RuleCodecTest {
    private val codec = RuleCodec(RulePlugins.registry)

    private fun decode(payload: RulePayload): RuleDefinition? = codec.decode(
        id = 9L,
        appLabel = "Chat",
        enabled = false,
        createdAt = 1234L,
        payload = payload,
    )

    @Test
    fun roundTripsNotificationRuleWithoutConditions() {
        val rule = RuleDefinition(
            id = 9L,
            appLabel = "Chat",
            enabled = false,
            createdAt = 1234L,
            trigger = NotificationTrigger("com.chat"),
            actions = listOf(NotifyAction),
        )

        val decoded = decode(codec.encode(rule))

        assertEquals(rule, decoded)
    }

    @Test
    fun roundTripsKeywordAndSound() {
        val rule = RuleDefinition(
            id = 9L,
            appLabel = "Chat",
            enabled = false,
            createdAt = 1234L,
            trigger = NotificationTrigger("com.chat"),
            conditions = listOf(KeywordCondition("urgent")),
            actions = listOf(SoundAction),
        )

        assertEquals(rule, decode(codec.encode(rule)))
    }

    @Test
    fun roundTripsClipboardExtractionConfig() {
        val rule = RuleDefinition(
            id = 9L,
            appLabel = "Chat",
            enabled = false,
            createdAt = 1234L,
            trigger = NotificationTrigger("com.chat"),
            conditions = listOf(KeywordCondition("otp")),
            actions = listOf(
                ClipboardAction(
                    mode = ClipboardMode.EXTRACTED,
                    extractionMode = ExtractionMode.CUSTOM_REGEX,
                    customRegex = "code: (\\d+)",
                    clearClipboardSeconds = 60,
                ),
            ),
        )

        assertEquals(rule, decode(codec.encode(rule)))
    }

    @Test
    fun roundTripsClipboardDefaults() {
        val rule = RuleDefinition(
            id = 9L,
            appLabel = "Chat",
            enabled = false,
            createdAt = 1234L,
            trigger = NotificationTrigger("com.chat"),
            actions = listOf(ClipboardAction()),
        )

        assertEquals(rule, decode(codec.encode(rule)))
    }

    @Test
    fun unknownTriggerTypeReturnsNull() {
        val payload = codec.encode(
            RuleDefinition(
                appLabel = "Chat",
                trigger = NotificationTrigger("com.chat"),
                actions = listOf(NotifyAction),
            ),
        ).copy(triggerType = "TIME")

        assertNull(decode(payload))
    }

    @Test
    fun unknownConditionTypeReturnsNull() {
        val payload = codec.encode(
            RuleDefinition(
                appLabel = "Chat",
                trigger = NotificationTrigger("com.chat"),
                conditions = listOf(KeywordCondition("x")),
                actions = listOf(NotifyAction),
            ),
        ).copy(conditionsPayload = """[{"type":"UNKNOWN","payload":{}}]""")

        assertNull(decode(payload))
    }

    @Test
    fun unknownActionTypeReturnsNull() {
        val payload = codec.encode(
            RuleDefinition(
                appLabel = "Chat",
                trigger = NotificationTrigger("com.chat"),
                actions = listOf(NotifyAction),
            ),
        ).copy(actionsPayload = """[{"type":"UNKNOWN","payload":{}}]""")

        assertNull(decode(payload))
    }

    @Test
    fun decodesMissingOptionalClipboardFieldsWithDefaults() {
        val payload = codec.encode(
            RuleDefinition(
                appLabel = "Chat",
                trigger = NotificationTrigger("com.chat"),
                actions = listOf(ClipboardAction()),
            ),
        ).copy(
            actionsPayload = """[{"type":"CLIPBOARD","payload":{}}]""",
        )

        val decoded = decode(payload)

        assertNotNull(decoded)
        assertEquals(
            ClipboardAction(),
            decoded?.actions?.single(),
        )
    }
}
