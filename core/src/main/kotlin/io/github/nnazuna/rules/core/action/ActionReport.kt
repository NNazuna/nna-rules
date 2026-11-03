package io.github.nnazuna.rules.core.action

import io.github.nnazuna.rules.core.extraction.ClipboardOutcome

data class ActionReport(
    val clipboardOutcome: ClipboardOutcome? = null,
)
