package io.github.nnazuna.rules.data

import io.github.nnazuna.rules.core.action.ClipboardAction
import io.github.nnazuna.rules.core.action.NotifyAction
import io.github.nnazuna.rules.core.action.SoundAction
import io.github.nnazuna.rules.core.condition.KeywordCondition
import io.github.nnazuna.rules.core.extraction.ClipboardMode
import io.github.nnazuna.rules.core.extraction.ExtractionMode
import io.github.nnazuna.rules.core.model.RuleDefinition
import io.github.nnazuna.rules.core.trigger.NotificationTrigger

fun Rule.toDefinition(): RuleDefinition {
    val keyword = keyword?.trim()?.takeIf { it.isNotEmpty() }
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
        conditions = keyword?.let { listOf(KeywordCondition(it)) } ?: emptyList(),
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
