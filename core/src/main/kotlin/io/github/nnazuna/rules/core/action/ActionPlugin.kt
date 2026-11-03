package io.github.nnazuna.rules.core.action

import kotlinx.serialization.json.JsonObject

interface ActionPlugin {
    val typeId: String

    fun encode(action: Action): JsonObject

    fun decode(payload: JsonObject): Action
}
