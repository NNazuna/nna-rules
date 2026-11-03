package io.github.nnazuna.rules.core.trigger

import kotlinx.serialization.json.JsonObject

interface TriggerPlugin {
    val typeId: String

    fun encode(trigger: Trigger): JsonObject

    fun decode(payload: JsonObject): Trigger
}
