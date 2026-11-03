package io.github.nnazuna.rules.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import io.github.nnazuna.rules.core.codec.RuleCodec
import io.github.nnazuna.rules.core.codec.RulePayload
import io.github.nnazuna.rules.core.model.RuleDefinition
import kotlinx.serialization.Serializable

@Entity(tableName = "rules")
data class RuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "app_label") val appLabel: String,
    val enabled: Boolean = true,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "trigger_type") val triggerType: String,
    @ColumnInfo(name = "trigger_payload") val triggerPayload: String,
    @ColumnInfo(name = "conditions_payload") val conditionsPayload: String,
    @ColumnInfo(name = "actions_payload") val actionsPayload: String,
)

fun RuleEntity.toDefinition(codec: RuleCodec): RuleDefinition? = codec.decode(
    id = id,
    appLabel = appLabel,
    enabled = enabled,
    createdAt = createdAt,
    payload = RulePayload(
        triggerType = triggerType,
        triggerPayload = triggerPayload,
        conditionsPayload = conditionsPayload,
        actionsPayload = actionsPayload,
    ),
)

fun RuleDefinition.toEntity(codec: RuleCodec): RuleEntity {
    val payload = codec.encode(this)
    return RuleEntity(
        id = id,
        appLabel = appLabel,
        enabled = enabled,
        createdAt = createdAt,
        triggerType = payload.triggerType,
        triggerPayload = payload.triggerPayload,
        conditionsPayload = payload.conditionsPayload,
        actionsPayload = payload.actionsPayload,
    )
}

@Serializable
data class ExportedRule(
    val appLabel: String,
    val enabled: Boolean = true,
    val triggerType: String,
    val triggerPayload: String,
    val conditionsPayload: String,
    val actionsPayload: String,
)
