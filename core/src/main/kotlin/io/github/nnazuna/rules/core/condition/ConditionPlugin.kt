package io.github.nnazuna.rules.core.condition

import kotlinx.serialization.json.JsonObject

interface ConditionPlugin {
    val typeId: String

    fun encode(condition: Condition): JsonObject

    fun decode(payload: JsonObject): Condition
}
