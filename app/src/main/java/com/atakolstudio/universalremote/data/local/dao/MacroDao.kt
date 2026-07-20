package com.atakolstudio.universalremote.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.atakolstudio.universalremote.data.local.entity.MacroEntity
import com.atakolstudio.universalremote.data.local.entity.MacroStepEntity
import kotlinx.coroutines.flow.Flow

data class MacroWithSteps(
    val macro: MacroEntity,
    val steps: List<MacroStepEntity>
)

@Dao
interface MacroDao {
    @Query("SELECT * FROM macros ORDER BY createdAtMillis DESC")
    fun observeAll(): Flow<List<MacroEntity>>

    @Query("SELECT * FROM macro_steps WHERE macroId = :macroId ORDER BY orderIndex")
    suspend fun getSteps(macroId: Long): List<MacroStepEntity>

    @Query("SELECT * FROM macro_steps WHERE macroId = :macroId ORDER BY orderIndex")
    fun observeSteps(macroId: Long): Flow<List<MacroStepEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMacro(macro: MacroEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSteps(steps: List<MacroStepEntity>)

    @Delete
    suspend fun deleteMacro(macro: MacroEntity)

    @Transaction
    suspend fun createWithSteps(macro: MacroEntity, steps: List<MacroStepEntity>): Long {
        val macroId = insertMacro(macro)
        insertSteps(steps.map { it.copy(macroId = macroId) })
        return macroId
    }
}
