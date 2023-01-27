package com.smartestidea.a2fac.data.model

import com.nerox.client.Tfprotocol
import com.smartestidea.a2fac.core.Callback
import com.smartestidea.a2fac.core.TYPE

//ipServer,portServer,publicKey,hash,len,protocol,protoHandler
abstract class Configuration(
    val id:Int,
    val name:String,
    val ipServer:String,
    val portServe:Int,
    val publicKey:String,
    val hash:String,
    val protocol:String,
    val pos:Int,
    val interval:Int,
    var isOn:Boolean
) {
    abstract fun copy(): Configuration
    abstract fun getType():TYPE
}

fun Configuration.toProtocol() = Tfprotocol(ipServer,portServe,publicKey.substringAfter(" :: "),hash,64,protocol,Callback())