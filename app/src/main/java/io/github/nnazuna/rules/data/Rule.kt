package io.github.nnazuna.rules.data

import io.github.nnazuna.rules.core.action.ClipboardAction
import io.github.nnazuna.rules.core.action.NotifyAction
import io.github.nnazuna.rules.core.action.SoundAction
import io.github.nnazuna.rules.core.condition.KeywordCondition
import io.github.nnazuna.rules.core.extraction.ClipboardMode
import io.github.nnazuna.rules.core.extraction.ExtractionMode
import io.github.nnazuna.rules.core.model.RuleDefinition
import io.github.nnazuna.rules.core.trigger.NotificationTrigger

enum class ActionType {
    NOTIFY,
    SOUND,
    CLIPBOARD,
}

data class Rule(
    val id: Long = 0,
    val packageName: String,
    val appLabel: String,
    val keyword: String? = null,
    val action: ActionType,
    val clipboardMode: ClipboardMode = ClipboardMode.FULL,
    val extractionMode: ExtractionMode = ExtractionMode.OTP,
    val customRegex: String? = null,
    val clearClipboardSeconds: Int = 0,
    val enabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
)

fun Rule.toDefinition(): RuleDefinition {
    val actions = when (action) {
        ActionType.NOTIFY -> listOf(NotifyAction)
        ActionType.SOUND -> listOf(SoundAction)
        ActionType.CLIPBOARD -> listOf(
            ClipboardAction(
                mode = clipboardMode,
                extractionMode = extractionMode,
                customRegex = customRegex,
                clearClipboardSeconds = clearClipboardSeconds,
            ),
        )
    }
    return RuleDefinition(
        id = id,
        appLabel = appLabel,
        enabled = enabled,
        createdAt = createdAt,
        trigger = NotificationTrigger(packageName),
        conditions = keyword?.trim()?.takeIf { it.isNotEmpty() }
            ?.let { listOf(KeywordCondition(it)) } ?: emptyList(),
        actions = actions,
    )
}

fun RuleDefinition.toLegacyRule(): Rule? {
    val notification = trigger as? NotificationTrigger ?: return null
    val primary = actions.firstOrNull() ?: return null
    val actionType = when (primary) {
        NotifyAction -> ActionType.NOTIFY
        SoundAction -> ActionType.SOUND
        is ClipboardAction -> ActionType.CLIPBOARD
        else -> return null
    }
    val clipboard = primary as? ClipboardAction
    return Rule(
        id = id,
        packageName = notification.packageName,
        appLabel = appLabel,
        keyword = conditions.filterIsInstance<KeywordCondition>().firstOrNull()?.keyword,
        action = actionType,
        clipboardMode = clipboard?.mode ?: ClipboardMode.FULL,
        extractionMode = clipboard?.extractionMode ?: ExtractionMode.OTP,
        customRegex = clipboard?.customRegex,
        clearClipboardSeconds = clipboard?.clearClipboardSeconds ?: 0,
        enabled = enabled,
        createdAt = createdAt,
    )
}
