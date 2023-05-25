package com.smartestidea.a2fac.data.database

import androidx.room.*
import com.smartestidea.a2fac.data.model.ConfigurationReceiver

@Dao
interface ConfigurationDao {
        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insertConfigurationReceiver(receiver: ConfigurationReceiverEntity)

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insertConfigurationSender(sender: ConfigurationSenderEntity)

        @Update
        suspend fun updateConfigurationReceiver(receiver: ConfigurationReceiverEntity)

        @Update
        suspend fun updateConfigurationSender(sender: ConfigurationSenderEntity)

        @Query("UPDATE receivers SET pos = :pos WHERE name = :id")
        suspend fun updatePosReceiver(id:String,pos:Int)

        @Query("UPDATE senders SET pos = :pos WHERE name = :id")
        suspend fun updatePosSender(id:String,pos:Int)

        @Query("DELETE FROM receivers WHERE name = :id")
        suspend fun deleteReceiver(id:String)

        @Query("DELETE FROM senders WHERE name = :id")
        suspend fun deleteSender(id:String)

        @Query("SELECT * FROM senders ORDER BY pos")
        suspend fun getAllSenders():List<ConfigurationSenderEntity>

        @Query("SELECT * FROM receivers ORDER BY pos")
        suspend fun getAllReceivers():List<ConfigurationReceiverEntity>

        @Query("SELECT senders.publicKey FROM receivers,senders ")
        suspend fun getAllKeys():List<String>

        @Query("update receivers set isOn=:isOn where name=:id")
        suspend fun setStateReceiver(isOn:Boolean,id:String)

        @Query("update senders set isOn=:isOn where name=:id")
        suspend fun setStateSender(isOn:Boolean,id:String)
}