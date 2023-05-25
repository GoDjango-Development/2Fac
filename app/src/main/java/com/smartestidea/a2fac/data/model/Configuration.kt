package com.smartestidea.a2fac.data.model

import com.nerox.client.Tfprotocol
import com.smartestidea.a2fac.core.Callback
import com.smartestidea.a2fac.core.TYPE

//ipServer,portServer,publicKey,hash,len,protocol,protoHandler
abstract class Configuration(
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

//fun Configuration.toProtocol() = Tfprotocol(ipServer,portServe,("""-----BEGIN PUBLIC KEY-----
//MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAw4xpa6MpKze3tp+uNQ+Z
//JmP+Pzj4TLXXCQ/hWrAMYx60IxTsRRvTRpg8bIgVYHWLwh/ixDQ4sYcWaOhx+QjD
//RNVBFLziHs918y8up82DvdLU5qhFfJT4YbYq3YCPAYkHVzD545W+WmE1+GQr36P1
//222P+owl7bM58ZDL5pXwbwJPOH9cz5N/uLAvCaU9WkCICGE9ZtSDCRf7AouuPaGa
//fL6sIb8hBMYRUDJfemeh/AdoEyYh11EhnsdnyBHHKfW8qlm2FA7xPvoMotVlO3gN
//8T45zI9j89SzGG2YBmnj6RB43ZiGBvkFkUqLArNnMbD8kuSAnk0b1NJhrzI89Ife
//qwIDAQAB
//-----END PUBLIC KEY-----""").trim(),hash,64,protocol,Callback())

fun Configuration.toProtocol() = Tfprotocol(ipServer,portServe,(publicKey).trim(),hash,64,protocol,Callback())