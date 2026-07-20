package com.atakolstudio.universalremote.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.atakolstudio.universalremote.data.local.dao.DeviceDao
import com.atakolstudio.universalremote.data.local.dao.FavoriteDao
import com.atakolstudio.universalremote.data.local.dao.IrCodeDao
import com.atakolstudio.universalremote.data.local.dao.MacroDao
import com.atakolstudio.universalremote.data.local.entity.DeviceEntity
import com.atakolstudio.universalremote.data.local.entity.FavoriteEntity
import com.atakolstudio.universalremote.data.local.entity.IrCodeEntity
import com.atakolstudio.universalremote.data.local.entity.MacroEntity
import com.atakolstudio.universalremote.data.local.entity.MacroStepEntity

@Database(
    entities = [
        DeviceEntity::class,
        IrCodeEntity::class,
        MacroEntity::class,
        MacroStepEntity::class,
        FavoriteEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun deviceDao(): DeviceDao
    abstract fun irCodeDao(): IrCodeDao
    abstract fun macroDao(): MacroDao
    abstract fun favoriteDao(): FavoriteDao

    companion object {
        const val DATABASE_NAME = "universal_remote.db"
    }
}
