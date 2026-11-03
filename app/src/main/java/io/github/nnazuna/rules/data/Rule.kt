package io.github.nnazuna.rules.data

import io.github.nnazuna.rules.core.extraction.ClipboardMode
import io.github.nnazuna.rules.core.extraction.ExtractionMode

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
