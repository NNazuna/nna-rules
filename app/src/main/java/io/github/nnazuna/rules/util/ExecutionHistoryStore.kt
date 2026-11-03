package io.github.nnazuna.rules.util

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

@Serializable
data class ExecutionEvent(
    val timestamp: Long = System.currentTimeMillis(),
    val ruleLabel: String,
    val detail: String,
)

object ExecutionHistoryStore {
    private const val PREFS_NAME = "nna_rules_execution_history"
    private const val KEY_HISTORY = "history_json"
    private const val MAX_ENTRIES = 30

    private val json = Json { ignoreUnknownKeys = true }
    private val _events = MutableStateFlow<List<ExecutionEvent>>(emptyList())
    val events: StateFlow<List<ExecutionEvent>> = _events.asStateFlow()

    private var initialized = false

    fun init(context: Context) {
        if (initialized) return
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_HISTORY, null)
        if (!raw.isNullOrBlank()) {
            val list = runCatching {
                json.decodeFromString(ListSerializer(ExecutionEvent.serializer()), raw)
            }.getOrDefault(emptyList())
            _events.value = list
        }
        initialized = true
    }

    fun record(context: Context, ruleLabel: String, detail: String) {
        init(context)
        val newEvent = ExecutionEvent(ruleLabel = ruleLabel, detail = detail)
        val updated = (listOf(newEvent) + _events.value).take(MAX_ENTRIES)
        _events.value = updated

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val encoded = json.encodeToString(ListSerializer(ExecutionEvent.serializer()), updated)
        prefs.edit().putString(KEY_HISTORY, encoded).apply()
    }

    fun clear(context: Context) {
        _events.value = emptyList()
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_HISTORY).apply()
    }
}
