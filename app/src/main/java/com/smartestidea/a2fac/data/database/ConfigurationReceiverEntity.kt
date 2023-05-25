package com.smartestidea.a2fac.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.smartestidea.a2fac.data.model.Configuration
import com.smartestidea.a2fac.data.model.ConfigurationReceiver

@Entity(tableName = "receivers")
data class ConfigurationReceiverEntity(
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
    val safeFolders:MutableList<String>,
    val alreadyDownloads:MutableList<String>
)
fun ConfigurationReceiver.toDomain() = ConfigurationReceiverEntity( name, ipServer, portServe, publicKey, hash, protocol, pos, interval, isOn, safeFolders, alreadyDownloads)