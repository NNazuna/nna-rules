package io.github.nnazuna.rules.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RuleDao {
    @Query("SELECT * FROM rules ORDER BY created_at DESC")
    fun observeAll(): Flow<List<Rule>>

    @Query("SELECT * FROM rules WHERE enabled = 1")
    suspend fun getEnabled(): List<Rule>

    @Query("SELECT * FROM rules WHERE id = :id")
    suspend fun getById(id: Long): Rule?

    @Insert
    suspend fun insert(rule: Rule): Long

    @Update
    suspend fun update(rule: Rule)

    @Delete
    suspend fun delete(rule: Rule)
}
