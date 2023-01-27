package com.smartestidea.a2fac.data.model

import com.smartestidea.a2fac.core.TYPE
import com.smartestidea.a2fac.data.database.ConfigurationReceiverEntity

class ConfigurationReceiver(
    id:Int,
    name:String,
    ipServer:String,
    portServe:Int,
    publicKey:String,
    hash:String,
    protocol:String,
    pos:Int,
    interval:Int,
    isOn:Boolean,
    val safeFolders:MutableList<String>,
    val alreadyDownloads:MutableList<String>
):Configuration(
    id,
    name,
    ipServer,
    portServe,
    publicKey,
    hash,
    protocol,
    pos,
    interval,
    isOn
) {
    override fun copy(): Configuration = ConfigurationReceiver(id, name, ipServer, portServe, publicKey, hash, protocol, pos, interval, isOn, safeFolders, alreadyDownloads)
    override fun getType(): TYPE = TYPE.RECEIVER

}

fun ConfigurationReceiverEntity.toDomain() = ConfigurationReceiver(id, name, ipServer, portServe, publicKey, hash, protocol, pos, interval, isOn, safeFolders, alreadyDownloads)