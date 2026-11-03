package io.github.nnazuna.rules.data

import android.util.Log
import io.github.nnazuna.rules.core.codec.RuleCodec
import io.github.nnazuna.rules.core.plugin.RulePlugins
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RuleRepository(
    private val dao: RuleDao,
    private val codec: RuleCodec = RuleCodec(RulePlugins.registry),
) {
    fun observeRules(): Flow<List<Rule>> = dao.observeAll().map { entities ->
        entities.mapNotNull { it.toLegacyRule() }
    }

    suspend fun getEnabledRules(): List<Rule> = dao.getEnabled().mapNotNull { it.toLegacyRule() }

    suspend fun getRule(id: Long): Rule? = dao.getById(id)?.toLegacyRule()

    suspend fun save(rule: Rule): Long =
        if (rule.id == 0L) {
            dao.insert(rule.toDefinition().toEntity(codec))
        } else {
            dao.update(rule.toDefinition().toEntity(codec))
            rule.id
        }

    suspend fun setEnabled(rule: Rule, enabled: Boolean) {
        dao.update(rule.copy(enabled = enabled).toDefinition().toEntity(codec))
    }

    suspend fun delete(rule: Rule) {
        dao.delete(rule.toDefinition().toEntity(codec))
    }

    private fun RuleEntity.toLegacyRule(): Rule? {
        val definition = toDefinition(codec)
        if (definition == null) {
            Log.w(TAG, "Skipping rule $id: unsupported trigger or action payload")
            return null
        }
        return definition.toLegacyRule()
    }

    private companion object {
        const val TAG = "RuleRepository"
    }
}
