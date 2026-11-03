package io.github.nnazuna.rules.core.legacy

import io.github.nnazuna.rules.core.action.ClipboardAction
import io.github.nnazuna.rules.core.action.NotifyAction
import io.github.nnazuna.rules.core.action.SoundAction
import io.github.nnazuna.rules.core.condition.KeywordCondition
import io.github.nnazuna.rules.core.extraction.ClipboardMode
import io.github.nnazuna.rules.core.extraction.ExtractionMode
import io.github.nnazuna.rules.core.trigger.NotificationTrigger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class LegacyRuleMapperTest {
    private fun snapshot(
        keyword: String? = null,
        action: String = "NOTIFY",
        clipboardMode: String = "FULL",
        extractionMode: String = "OTP",
        customRegex: String? = null,
        clearClipboardSeconds: Int = 0,
    ) = LegacyRuleSnapshot(
        id = 7L,
        packageName = "com.chat",
        appLabel = "Chat",
        keyword = keyword,
        action = action,
        clipboardMode = clipboardMode,
        extractionMode = extractionMode,
        customRegex = customRegex,
        clearClipboardSeconds = clearClipboardSeconds,
        enabled = false,
        createdAt = 1234L,
    )

    @Test
    fun mapsIdentityFields() {
        val definition = LegacyRuleMapper.toDefinition(snapshot(keyword = "otp"))

        assertEquals(7L, definition.id)
        assertEquals("Chat", definition.appLabel)
        assertEquals(false, definition.enabled)
        assertEquals(1234L, definition.createdAt)
        assertEquals(NotificationTrigger("com.chat"), definition.trigger)
    }

    @Test
    fun mapsKeywordAndNotifyAction() {
        val definition = LegacyRuleMapper.toDefinition(snapshot(keyword = "  otp  "))

        assertEquals(listOf(KeywordCondition("otp")), definition.conditions)
        assertEquals(listOf(NotifyAction), definition.actions)
    }

    @Test
    fun blankKeywordBecomesNoCondition() {
        val definition = LegacyRuleMapper.toDefinition(snapshot(keyword = "   "))

        assertTrue(definition.conditions.isEmpty())
    }

    @Test
    fun mapsSoundAction() {
        val definition = LegacyRuleMapper.toDefinition(snapshot(action = "SOUND"))

        assertEquals(listOf(SoundAction), definition.actions)
    }

    @Test
    fun mapsClipboardConfig() {
        val definition = LegacyRuleMapper.toDefinition(
            snapshot(
                action = "CLIPBOARD",
                clipboardMode = "EXTRACTED",
                extractionMode = "CUSTOM_REGEX",
                customRegex = "code: (\\d+)",
                clearClipboardSeconds = 60,
            ),
        )

        assertEquals(
            listOf(
                ClipboardAction(
                    mode = ClipboardMode.EXTRACTED,
                    extractionMode = ExtractionMode.CUSTOM_REGEX,
                    customRegex = "code: (\\d+)",
                    clearClipboardSeconds = 60,
                ),
            ),
            definition.actions,
        )
    }

    @Test
    fun unknownEnumValuesFallBackToDefaults() {
        val definition = LegacyRuleMapper.toDefinition(
            snapshot(
                action = "CLIPBOARD",
                clipboardMode = "BROKEN",
                extractionMode = "BROKEN",
            ),
        )

        assertEquals(
            listOf(ClipboardAction(mode = ClipboardMode.FULL, extractionMode = ExtractionMode.OTP)),
            definition.actions,
        )
    }

    @Test
    fun unknownActionFailsLoudly() {
        assertThrows(IllegalStateException::class.java) {
            LegacyRuleMapper.toDefinition(snapshot(action = "UNKNOWN"))
        }
    }
}
