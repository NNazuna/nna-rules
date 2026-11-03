package io.github.nnazuna.rules.core.action

import io.github.nnazuna.rules.core.event.RuleEvent
import io.github.nnazuna.rules.core.model.RuleDefinition
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

data class CsvAppendAction(
    val filePath: String,
    val extractionPattern: String? = null,
    val columns: List<String> = emptyList(),
) : Action {
    override val type: String get() = TYPE

    override fun execute(event: RuleEvent, rule: RuleDefinition, context: ActionContext): ActionReport {
        val extractedValue = if (extractionPattern != null) {
            val regex = Regex(extractionPattern)
            regex.find(event.toString())?.groupValues?.getOrNull(1) 
                ?: regex.find(event.toString())?.value
        } else {
            event.toString()
        }

        if (extractedValue.isNullOrBlank()) return ActionReport()

        context.appendToCsv(filePath, columns, extractedValue)
        return ActionReport()
    }

    companion object : ActionPlugin {
        const val TYPE = "CSV_APPEND"
        override val typeId: String get() = TYPE

        override fun encode(action: Action): JsonObject {
            require(action is CsvAppendAction)
            return buildJsonObject {
                put("filePath", action.filePath)
                action.extractionPattern?.let { put("extractionPattern", it) }
                if (action.columns.isNotEmpty()) {
                    put("columns", action.columns.toString())
                }
            }
        }

        override fun decode(payload: JsonObject): Action {
            val filePath = payload["filePath"]?.jsonPrimitive?.contentOrNull
                ?: throw IllegalArgumentException("CSV action requires filePath")
            val extractionPattern = payload["extractionPattern"]?.jsonPrimitive?.contentOrNull
            val columnsStr = payload["columns"]?.jsonPrimitive?.contentOrNull
            val columns = columnsStr?.removeSurrounding("[", "]")
                ?.split(", ")
                ?.map { it.trim() } ?: emptyList()
            return CsvAppendAction(filePath, extractionPattern, columns)
        }
    }
}
