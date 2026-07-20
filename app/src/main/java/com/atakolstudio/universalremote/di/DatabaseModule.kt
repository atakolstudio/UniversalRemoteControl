package com.atakolstudio.universalremote.di

import android.content.Context
import androidx.room.Room
import com.atakolstudio.universalremote.data.local.AppDatabase
import com.atakolstudio.universalremote.data.local.dao.DeviceDao
import com.atakolstudio.universalremote.data.local.dao.FavoriteDao
import com.atakolstudio.universalremote.data.local.dao.IrCodeDao
import com.atakolstudio.universalremote.data.local.dao.MacroDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideDeviceDao(db: AppDatabase): DeviceDao = db.deviceDao()

    @Provides
    fun provideIrCodeDao(db: AppDatabase): IrCodeDao = db.irCodeDao()

    @Provides
    fun provideMacroDao(db: AppDatabase): MacroDao = db.macroDao()

    @Provides
    fun provideFavoriteDao(db: AppDatabase): FavoriteDao = db.favoriteDao()
}
