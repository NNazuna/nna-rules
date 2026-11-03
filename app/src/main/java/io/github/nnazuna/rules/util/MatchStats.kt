package io.github.nnazuna.rules.util

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class RuleMatchStat(
    val count: Int,
    val lastMatchedAt: Long?,
) {
    companion object {
        val Empty = RuleMatchStat(count = 0, lastMatchedAt = null)
    }
}

object MatchStats {
    private const val PREFS_NAME = "nna_rules_match_stats"
    private const val KEY_COUNT_PREFIX = "count_"
    private const val KEY_LAST_PREFIX = "last_"

    private val _revision = MutableStateFlow(0L)
    val revision: StateFlow<Long> = _revision.asStateFlow()

    fun record(context: Context, ruleIds: Collection<Long>) {
        if (ruleIds.isEmpty()) return
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val now = System.currentTimeMillis()
        val editor = prefs.edit()
        ruleIds.forEach { ruleId ->
            val key = KEY_COUNT_PREFIX + ruleId
            editor.putInt(key, prefs.getInt(key, 0) + 1)
            editor.putLong(KEY_LAST_PREFIX + ruleId, now)
        }
        editor.apply()
        _revision.value = now
    }

    fun statFor(context: Context, ruleId: Long): RuleMatchStat {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val count = prefs.getInt(KEY_COUNT_PREFIX + ruleId, 0)
        val last = prefs.getLong(KEY_LAST_PREFIX + ruleId, 0L).takeIf { it > 0L }
        return RuleMatchStat(count = count, lastMatchedAt = last)
    }
}
