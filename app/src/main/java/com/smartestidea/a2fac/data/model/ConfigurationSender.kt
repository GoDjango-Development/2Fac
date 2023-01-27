package com.smartestidea.a2fac.data.model

import com.smartestidea.a2fac.core.TYPE
import com.smartestidea.a2fac.data.database.ConfigurationSenderEntity

class ConfigurationSender (
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
    val keyword:String,
    val safeFolder:String,
        ): Configuration(id,
 name,
 ipServer,
 portServe,
publicKey,
 hash,
 protocol,
 pos,
 interval,
 isOn) {
    override fun copy(): Configuration = ConfigurationSender(id, name, ipServer, portServe, publicKey, hash, protocol, pos, interval, isOn, keyword, safeFolder)
    override fun getType(): TYPE = TYPE.SENDER

}

fun ConfigurationSenderEntity.toDomain() = ConfigurationSender(id, name, ipServer, portServe, publicKey, hash, protocol, pos, interval, isOn, keyword, safeFolder)