package io.github.nnazuna.rules.core.condition

import io.github.nnazuna.rules.core.event.NotificationEvent
import io.github.nnazuna.rules.core.event.RuleEvent
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

data class KeywordCondition(val keyword: String) : Condition {
    override val type: String get() = TYPE

    override fun matches(event: RuleEvent): Boolean {
        if (event !is NotificationEvent) return false
        val needle = keyword.trim()
        if (needle.isEmpty()) return true
        return event.searchableText.contains(needle, ignoreCase = true)
    }

    companion object : ConditionPlugin {
        const val TYPE = "KEYWORD"

        override val typeId: String get() = TYPE

        override fun encode(condition: Condition): JsonObject {
            require(condition is KeywordCondition) {
                "Expected KeywordCondition, got ${condition::class.simpleName}"
            }
            return buildJsonObject {
                put("keyword", condition.keyword)
            }
        }

        override fun decode(payload: JsonObject): Condition {
            val keyword = payload["keyword"]?.jsonPrimitive?.contentOrNull
            require(keyword != null) { "Keyword condition requires a keyword" }
            return KeywordCondition(keyword)
        }
    }
}
