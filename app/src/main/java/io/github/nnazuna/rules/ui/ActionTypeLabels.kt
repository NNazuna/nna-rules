package io.github.nnazuna.rules.ui

import io.github.nnazuna.rules.R
import io.github.nnazuna.rules.data.ActionType

fun ActionType.labelRes(): Int = when (this) {
    ActionType.NOTIFY -> R.string.action_notify
    ActionType.SOUND -> R.string.action_sound
    ActionType.CLIPBOARD -> R.string.action_clipboard
}

fun ActionType.descriptionRes(): Int = when (this) {
    ActionType.NOTIFY -> R.string.action_notify_description
    ActionType.SOUND -> R.string.action_sound_description
    ActionType.CLIPBOARD -> R.string.action_clipboard_description
}
