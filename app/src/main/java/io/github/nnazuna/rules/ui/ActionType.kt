package io.github.nnazuna.rules.ui

import io.github.nnazuna.rules.R
import io.github.nnazuna.rules.core.action.Action
import io.github.nnazuna.rules.core.action.ClipboardAction
import io.github.nnazuna.rules.core.action.NotifyAction
import io.github.nnazuna.rules.core.action.SoundAction

enum class ActionType {
    NOTIFY,
    SOUND,
    CLIPBOARD;

    fun labelRes(): Int = when (this) {
        NOTIFY -> R.string.action_notify
        SOUND -> R.string.action_sound
        CLIPBOARD -> R.string.action_clipboard
    }

    fun descriptionRes(): Int = when (this) {
        NOTIFY -> R.string.action_notify_description
        SOUND -> R.string.action_sound_description
        CLIPBOARD -> R.string.action_clipboard_description
    }
}

fun Action.toActionType(): ActionType? = when (this) {
    is NotifyAction -> ActionType.NOTIFY
    is SoundAction -> ActionType.SOUND
    is ClipboardAction -> ActionType.CLIPBOARD
    else -> null
}
