package io.github.nnazuna.rules.core.legacy

import io.github.nnazuna.rules.core.action.ClipboardAction
import io.github.nnazuna.rules.core.action.NotifyAction
import io.github.nnazuna.rules.core.action.SoundAction
import io.github.nnazuna.rules.core.condition.KeywordCondition
import io.github.nnazuna.rules.core.extraction.ClipboardMode
import io.github.nnazuna.rules.core.extraction.ExtractionMode
import io.github.nnazuna.rules.core.model.RuleDefinition
import io.github.nnazuna.rules.core.trigger.NotificationTrigger

data class LegacyRuleSnapshot(
    val id: Long,
    val packageName: String,
    val appLabel: String,
    val keyword: String?,
    val action: String,
    val clipboardMode: String,
    val extractionMode: String,
    val customRegex: String?,
    val clearClipboardSeconds: Int,
    val enabled: Boolean,
    val createdAt: Long,
)

object LegacyRuleMapper {
    fun toDefinition(snapshot: LegacyRuleSnapshot): RuleDefinition {
        val keyword = snapshot.keyword?.trim().orEmpty()
        val conditions = if (keyword.isEmpty()) {
            emptyList()
        } else {
            listOf(KeywordCondition(keyword))
        }
        val action = when (snapshot.action) {
            "NOTIFY" -> NotifyAction
            "SOUND" -> SoundAction
            "CLIPBOARD" -> ClipboardAction(
                mode = enumOrDefault(snapshot.clipboardMode, ClipboardMode.FULL),
                extractionMode = enumOrDefault(snapshot.extractionMode, ExtractionMode.OTP),
                customRegex = snapshot.customRegex,
                clearClipboardSeconds = snapshot.clearClipboardSeconds.coerceAtLeast(0),
            )
            else -> error("Unknown legacy action: ${snapshot.action}")
        }
        return RuleDefinition(
            id = snapshot.id,
            appLabel = snapshot.appLabel,
            enabled = snapshot.enabled,
            createdAt = snapshot.createdAt,
            trigger = NotificationTrigger(snapshot.packageName),
            conditions = conditions,
            actions = listOf(action),
        )
    }

    private inline fun <reified T : Enum<T>> enumOrDefault(value: String, default: T): T =
        enumValues<T>().firstOrNull { it.name == value } ?: default
}
