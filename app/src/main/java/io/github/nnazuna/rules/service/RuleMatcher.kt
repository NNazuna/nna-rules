package io.github.nnazuna.rules.service

import io.github.nnazuna.rules.data.Rule

data class NotificationContent(
    val title: String,
    val text: String,
) {
    val searchableText: String
        get() = buildList {
            if (title.isNotBlank()) add(title)
            if (text.isNotBlank()) add(text)
        }.joinToString("\n")
}

object RuleMatcher {
    fun matches(rule: Rule, packageName: String, content: NotificationContent): Boolean {
        if (!rule.enabled) return false
        if (rule.packageName != packageName) return false

        val keyword = rule.keyword?.trim().orEmpty()
        if (keyword.isEmpty()) return true

        return content.searchableText.contains(keyword, ignoreCase = true)
    }
}
