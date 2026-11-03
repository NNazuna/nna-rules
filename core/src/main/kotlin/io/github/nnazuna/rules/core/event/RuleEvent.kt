package io.github.nnazuna.rules.core.event

sealed interface RuleEvent

data class NotificationEvent(
    val packageName: String,
    val appLabel: String,
    val title: String,
    val text: String,
    val postedAt: Long = System.currentTimeMillis(),
) : RuleEvent {
    val searchableText: String
        get() = buildList {
            if (title.isNotBlank()) add(title)
            if (text.isNotBlank()) add(text)
        }.joinToString("\n")
}
