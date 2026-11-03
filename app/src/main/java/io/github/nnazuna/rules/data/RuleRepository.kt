package io.github.nnazuna.rules.data

import kotlinx.coroutines.flow.Flow

class RuleRepository(private val dao: RuleDao) {
    fun observeRules(): Flow<List<Rule>> = dao.observeAll()

    suspend fun getEnabledRules(): List<Rule> = dao.getEnabled()

    suspend fun getRule(id: Long): Rule? = dao.getById(id)

    suspend fun save(rule: Rule): Long =
        if (rule.id == 0L) {
            dao.insert(rule)
        } else {
            dao.update(rule)
            rule.id
        }

    suspend fun setEnabled(rule: Rule, enabled: Boolean) {
        dao.update(rule.copy(enabled = enabled))
    }

    suspend fun delete(rule: Rule) {
        dao.delete(rule)
    }
}
