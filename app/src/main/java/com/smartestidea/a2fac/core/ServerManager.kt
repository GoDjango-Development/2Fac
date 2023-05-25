package com.smartestidea.a2fac.core

import com.nerox.client.Tfprotocol
import com.smartestidea.a2fac.data.model.ConfigurationReceiver
import kotlinx.coroutines.sync.Mutex
import java.util.concurrent.Executors

object ServerManager {
    var serverListener = ServerListener()
    var protocols = emptyList<ConfigurationReceiver>()
    var protocolsRunning = mutableListOf<ConfigurationReceiver>()
    var myExecutor = Executors.newCachedThreadPool()
    var timers = mutableListOf<Long>()
    var timeStart:Long = 0L
    var mutexs = mutableListOf<Mutex>()

}