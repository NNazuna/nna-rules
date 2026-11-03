package io.github.nnazuna.rules.data

import android.util.Log
import io.github.nnazuna.rules.core.codec.RuleCodec
import io.github.nnazuna.rules.core.model.RuleDefinition
import io.github.nnazuna.rules.core.plugin.RulePlugins
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RuleRepository(
    private val dao: RuleDao,
    private val codec: RuleCodec = RuleCodec(RulePlugins.registry),
) {
    fun observeDefinitions(): Flow<List<RuleDefinition>> = dao.observeAll().map { entities ->
        entities.mapNotNull { it.toDefinition(codec) }
    }

    suspend fun getEnabledDefinitions(): List<RuleDefinition> =
        dao.getEnabled().mapNotNull { it.toDefinition(codec) }

    suspend fun getDefinition(id: Long): RuleDefinition? = dao.getById(id)?.toDefinition(codec)

    suspend fun save(definition: RuleDefinition): Long =
        if (definition.id == 0L) {
            dao.insert(definition.toEntity(codec))
        } else {
            dao.update(definition.toEntity(codec))
            definition.id
        }

    suspend fun setEnabled(definition: RuleDefinition, enabled: Boolean) {
        dao.update(definition.copy(enabled = enabled).toEntity(codec))
    }

    suspend fun delete(definition: RuleDefinition) {
        dao.delete(definition.toEntity(codec))
    }

    private companion object {
        const val TAG = "RuleRepository"
    }
}
