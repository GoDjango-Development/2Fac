package com.smartestidea.a2fac.core.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.smartestidea.a2fac.data.database.ConfigurationDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class RoomModule {
    @Singleton
    @Provides
    fun getRoomDatabase(@ApplicationContext context: Context) =
        Room.databaseBuilder(context,ConfigurationDatabase::class.java,"configuration_db").build()

    @Singleton
    @Provides
    fun getConfigurationDao(db:ConfigurationDatabase) =
        db.getConfigurationDao()
}