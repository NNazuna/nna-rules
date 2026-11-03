package io.github.nnazuna.rules.core.codec

import io.github.nnazuna.rules.core.action.Action
import io.github.nnazuna.rules.core.condition.Condition
import io.github.nnazuna.rules.core.model.RuleDefinition
import io.github.nnazuna.rules.core.plugin.PluginRegistry
import io.github.nnazuna.rules.core.trigger.Trigger
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

@Serializable
data class PluginEnvelope(
    val type: String,
    val payload: JsonObject,
)

data class RulePayload(
    val triggerType: String,
    val triggerPayload: String,
    val conditionsPayload: String,
    val actionsPayload: String,
)

class RuleCodec(
    private val plugins: PluginRegistry,
    private val json: Json = Json { ignoreUnknownKeys = true },
) {
    fun encode(rule: RuleDefinition): RulePayload = RulePayload(
        triggerType = rule.trigger.type,
        triggerPayload = encodeTrigger(rule.trigger),
        conditionsPayload = encodeConditions(rule.conditions),
        actionsPayload = encodeActions(rule.actions),
    )

    fun decode(
        id: Long,
        appLabel: String,
        enabled: Boolean,
        createdAt: Long,
        payload: RulePayload,
    ): RuleDefinition? {
        val triggerPlugin = plugins.trigger(payload.triggerType) ?: return null
        val trigger = triggerPlugin.decode(
            json.parseToJsonElement(payload.triggerPayload).jsonObject,
        )

        val conditionEnvelopes = decodeEnvelopes(payload.conditionsPayload)
        val conditions = ArrayList<Condition>(conditionEnvelopes.size)
        for (envelope in conditionEnvelopes) {
            val plugin = plugins.condition(envelope.type) ?: return null
            conditions += plugin.decode(envelope.payload)
        }

        val actionEnvelopes = decodeEnvelopes(payload.actionsPayload)
        val actions = ArrayList<Action>(actionEnvelopes.size)
        for (envelope in actionEnvelopes) {
            val plugin = plugins.action(envelope.type) ?: return null
            actions += plugin.decode(envelope.payload)
        }

        return RuleDefinition(
            id = id,
            appLabel = appLabel,
            enabled = enabled,
            createdAt = createdAt,
            trigger = trigger,
            conditions = conditions,
            actions = actions,
        )
    }

    private fun encodeTrigger(trigger: Trigger): String {
        val plugin = plugins.trigger(trigger.type)
            ?: error("No trigger plugin registered for ${trigger.type}")
        return json.encodeToString(JsonObject.serializer(), plugin.encode(trigger))
    }

    private fun encodeConditions(conditions: List<Condition>): String {
        val envelopes = conditions.map { condition ->
            val plugin = plugins.condition(condition.type)
                ?: error("No condition plugin registered for ${condition.type}")
            PluginEnvelope(type = condition.type, payload = plugin.encode(condition))
        }
        return encodeEnvelopes(envelopes)
    }

    private fun encodeActions(actions: List<Action>): String {
        val envelopes = actions.map { action ->
            val plugin = plugins.action(action.type)
                ?: error("No action plugin registered for ${action.type}")
            PluginEnvelope(type = action.type, payload = plugin.encode(action))
        }
        return encodeEnvelopes(envelopes)
    }

    private fun encodeEnvelopes(envelopes: List<PluginEnvelope>): String =
        json.encodeToString(ListSerializer(PluginEnvelope.serializer()), envelopes)

    private fun decodeEnvelopes(payload: String): List<PluginEnvelope> =
        json.decodeFromString(ListSerializer(PluginEnvelope.serializer()), payload)
}
