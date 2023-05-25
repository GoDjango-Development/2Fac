package com.smartestidea.a2fac.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.smartestidea.a2fac.data.model.ConfigurationSender

@Entity(tableName = "senders")
data class ConfigurationSenderEntity (
    @PrimaryKey(autoGenerate = false)
    val name:String,
    val ipServer:String,
    val portServe:Int,
    val publicKey:String,
    val hash:String,
    val protocol:String,
    val pos:Int,
    val interval:Int,
    val isOn:Boolean,
    val keyword:String,
    val safeFolder:String,
)
fun ConfigurationSender.toDomain() = ConfigurationSenderEntity(name, ipServer, portServe, publicKey, hash, protocol, pos, interval, isOn, keyword, safeFolder)