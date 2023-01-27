package com.smartestidea.a2fac.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters

@Database(entities = [ConfigurationReceiverEntity::class,ConfigurationSenderEntity::class], version = 1)
@TypeConverters(ConverterListFiles::class)
abstract class ConfigurationDatabase:RoomDatabase() {
  abstract fun getConfigurationDao():ConfigurationDao

}