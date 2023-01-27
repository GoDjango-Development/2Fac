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

        @Query("UPDATE receivers SET pos = :pos WHERE id = :id")
        suspend fun updatePosReceiver(id:Int,pos:Int)

        @Query("UPDATE senders SET pos = :pos WHERE id = :id")
        suspend fun updatePosSender(id:Int,pos:Int)

        @Query("DELETE FROM receivers WHERE id = :id")
        suspend fun deleteReceiver(id:Int)

        @Query("DELETE FROM senders WHERE id = :id")
        suspend fun deleteSender(id:Int)

        @Query("SELECT * FROM senders ORDER BY pos")
        suspend fun getAllSenders():List<ConfigurationSenderEntity>

        @Query("SELECT * FROM receivers ORDER BY pos")
        suspend fun getAllReceivers():List<ConfigurationReceiverEntity>

        @Query("SELECT senders.publicKey FROM receivers,senders ")
        suspend fun getAllKeys():List<String>

        @Query("update receivers set isOn=:isOn where id=:id")
        suspend fun setStateReceiver(isOn:Boolean,id:Int)

        @Query("update senders set isOn=:isOn where id=:id")
        suspend fun setStateSender(isOn:Boolean,id:Int)
}