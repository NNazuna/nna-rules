package io.github.nnazuna.rules.ui

import io.github.nnazuna.rules.R
import io.github.nnazuna.rules.core.action.Action
import io.github.nnazuna.rules.core.action.BrightnessAction
import io.github.nnazuna.rules.core.action.ClipboardAction
import io.github.nnazuna.rules.core.action.CsvAppendAction
import io.github.nnazuna.rules.core.action.DigestAction
import io.github.nnazuna.rules.core.action.DndAction
import io.github.nnazuna.rules.core.action.NotifyAction
import io.github.nnazuna.rules.core.action.SoundAction
import io.github.nnazuna.rules.core.action.VolumeAction

enum class ActionType {
    NOTIFY,
    SOUND,
    CLIPBOARD,
    VOLUME,
    BRIGHTNESS,
    DND,
    CSV,
    DIGEST;

    fun labelRes(): Int = when (this) {
        NOTIFY -> R.string.action_notify
        SOUND -> R.string.action_sound
        CLIPBOARD -> R.string.action_clipboard
        VOLUME -> R.string.action_volume
        BRIGHTNESS -> R.string.action_brightness
        DND -> R.string.action_dnd
        CSV -> R.string.action_csv
        DIGEST -> R.string.action_digest
    }

    fun descriptionRes(): Int = when (this) {
        NOTIFY -> R.string.action_notify_description
        SOUND -> R.string.action_sound_description
        CLIPBOARD -> R.string.action_clipboard_description
        VOLUME -> R.string.action_volume_description
        BRIGHTNESS -> R.string.action_brightness_description
        DND -> R.string.action_dnd_description
        CSV -> R.string.action_csv_description
        DIGEST -> R.string.action_digest_description
    }
}

fun Action.toActionType(): ActionType? = when (this) {
    is NotifyAction -> ActionType.NOTIFY
    is SoundAction -> ActionType.SOUND
    is ClipboardAction -> ActionType.CLIPBOARD
    is VolumeAction -> ActionType.VOLUME
    is BrightnessAction -> ActionType.BRIGHTNESS
    is DndAction -> ActionType.DND
    is CsvAppendAction -> ActionType.CSV
    is DigestAction -> ActionType.DIGEST
    else -> null
}
