package com.atakolstudio.universalremote.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "macros")
data class MacroEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val iconKey: String = "movie",
    val createdAtMillis: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "macro_steps",
    foreignKeys = [
        ForeignKey(
            entity = MacroEntity::class,
            parentColumns = ["id"],
            childColumns = ["macroId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = DeviceEntity::class,
            parentColumns = ["id"],
            childColumns = ["deviceId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class MacroStepEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val macroId: Long,
    val deviceId: Long,
    val function: RemoteFunction,
    val orderIndex: Int,
    /** Delay to wait after sending this step's command, before the next one fires. */
    val delayAfterMillis: Long = 300
)
