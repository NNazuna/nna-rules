package io.github.nnazuna.rules.core.action

import io.github.nnazuna.rules.core.event.NotificationEvent
import io.github.nnazuna.rules.core.event.RuleEvent
import io.github.nnazuna.rules.core.extraction.ClipboardMode
import io.github.nnazuna.rules.core.extraction.ExtractionMode
import io.github.nnazuna.rules.core.extraction.TextExtractor
import io.github.nnazuna.rules.core.model.RuleDefinition
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

data class ClipboardAction(
    val mode: ClipboardMode = ClipboardMode.FULL,
    val extractionMode: ExtractionMode = ExtractionMode.OTP,
    val customRegex: String? = null,
    val clearClipboardSeconds: Int = 0,
) : Action {
    override val type: String get() = TYPE

    override fun execute(
        event: RuleEvent,
        rule: RuleDefinition,
        context: ActionContext,
    ): ActionReport {
        val notification = event as? NotificationEvent ?: return ActionReport()
        val resolved = TextExtractor.resolveForClipboard(
            text = notification.searchableText,
            mode = mode,
            extractionMode = extractionMode,
            customRegex = customRegex,
        )
        if (resolved.text.isBlank()) return ActionReport()
        context.copyToClipboard(resolved.text, clearClipboardSeconds)
        return ActionReport(resolved.outcome)
    }

    companion object : ActionPlugin {
        const val TYPE = "CLIPBOARD"

        override val typeId: String get() = TYPE

        override fun encode(action: Action): JsonObject {
            require(action is ClipboardAction) {
                "Expected ClipboardAction, got ${action::class.simpleName}"
            }
            return buildJsonObject {
                put("mode", action.mode.name)
                put("extractionMode", action.extractionMode.name)
                action.customRegex?.let { put("customRegex", it) }
                put("clearClipboardSeconds", action.clearClipboardSeconds)
            }
        }

        override fun decode(payload: JsonObject): Action {
            val mode = enumValue<ClipboardMode>(payload, "mode", ClipboardMode.FULL)
            val extractionMode = enumValue<ExtractionMode>(
                payload,
                "extractionMode",
                ExtractionMode.OTP,
            )
            val customRegex = payload["customRegex"]?.jsonPrimitive?.contentOrNull
            val clearClipboardSeconds = payload["clearClipboardSeconds"]
                ?.jsonPrimitive
                ?.intOrNull
                ?: 0
            return ClipboardAction(
                mode = mode,
                extractionMode = extractionMode,
                customRegex = customRegex,
                clearClipboardSeconds = clearClipboardSeconds.coerceAtLeast(0),
            )
        }

        private inline fun <reified T : Enum<T>> enumValue(
            payload: JsonObject,
            key: String,
            default: T,
        ): T {
            val raw = payload[key]?.jsonPrimitive?.contentOrNull ?: return default
            return enumValues<T>().firstOrNull { it.name == raw } ?: default
        }
    }
}
