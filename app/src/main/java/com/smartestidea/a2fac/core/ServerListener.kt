package com.smartestidea.a2fac.core

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.os.AsyncTask
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.nerox.client.Tfprotocol
import com.smartestidea.a2fac.core.gsm.SmsSender
import com.smartestidea.a2fac.data.model.Configuration
import com.smartestidea.a2fac.data.model.ConfigurationReceiver
import com.smartestidea.a2fac.data.model.toProtocol
import com.smartestidea.a2fac.ui.viewmodel.settings.TFPViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*
import javax.inject.Singleton



class ServerListener: AsyncTask<List<Any>, Void, Boolean>() {
    private var protocols = emptyList<ConfigurationReceiver>()
    private var timers= mutableListOf<Long>()
    override fun onCancelled() {
        protocols = emptyList()
        super.onCancelled()
    }

    fun setProtocols(newProtocols:List<ConfigurationReceiver>) {
        protocols = newProtocols
        val timeStart = Calendar.getInstance().timeInMillis
        timers = MutableList(protocols.size){timeStart}
    }
    override fun doInBackground(vararg p0: List<Any>?): Boolean? {
        protocols = p0[0]?.get(0) as List<ConfigurationReceiver>
//        val viewModel = p0[0]?.get(1) as TFPViewModel
        val timeStart = Calendar.getInstance().timeInMillis
//        val mutex = Mutex()
        val mutexFolder = Mutex()
        val mutexFile = Mutex()
        timers.fill(timeStart)
        val coroutineExceptionHandler = CoroutineExceptionHandler{_, throwable ->
            Log.e("COROUTINE_ERROR",throwable.message.toString())
        }
        if(protocols.isNotEmpty())
            while(true) {
                protocols.forEach {config->
                    CoroutineScope(Dispatchers.IO+coroutineExceptionHandler).launch {
                    try {

                        if ((Calendar.getInstance().timeInMillis - timers[protocols.indexOf(config)]!!) >= (config.interval * 1000)) {
//                            Log.i("RUNNING_TFP",config.name)

                            timers[protocols.indexOf(config)] = Calendar.getInstance().timeInMillis

                            mutexFolder.withLock {

                                config.safeFolders.forEach { safeFolder ->
                                    Log.i("SAFE_FOLDER_${config.name}",safeFolder)
                                    val tfp = config.toProtocol()
                                    tfp.tcpTimeOut.connectTimeout = 5000
                                    tfp.tcpTimeOut.connectRetry = 3
                                    tfp.tcpTimeOut.dnsResolutionTimeout = 3000
                                    tfp.connect()

                                    val lsvFileName =
                                        "lsv2Response-${safeFolder}-${config.name}.txt"
                                    tfp.connect()

                                    tfp.lsv2Command(
                                        safeFolder,
                                        "$safeFolder/$lsvFileName"
                                    )

                                    val lsv2Content = getContent(
                                        "$safeFolder/$lsvFileName",
                                        tfp
                                    )
                                    val filesToSend = lsv2Content.split("$safeFolder/").filter {
//                                        !config.alreadyDownloads.contains(it) &&
                                                !it.startsWith("lsv2Response") && it.isNotEmpty() && it != " "
                                    }
                                    mutexFile.withLock {
                                        filesToSend.forEach { fileName ->

                                            val fileContent = getContent(
                                                "$safeFolder/$fileName",
                                                tfp
                                            )

                                            if (fileName.isNotEmpty() && fileContent.isNotEmpty())
                                                SmsSender(fileName, fileContent)

                                            tfp.delCommand("$safeFolder/$fileName")

//                                            config.alreadyDownloads.add(fileName)
//                                            mutex.lock()
//                                            viewModel.update(config) {
//                                                mutex.unlock()
//                                                this@ServerListener.cancel(true)
//                                            }

                                        }
                                    }
                                    tfp.delCommand("$safeFolder/$lsvFileName")
                                    tfp.disconnect()
                                }
                            }
                        }
                    }
                    catch (e:Exception){
                        Log.e("ServerListenerError",e.message.toString())
                    }
                }
            }
        }
        return true
    }

    private fun getContent(serverPath: String, tfp: Tfprotocol):String{
        if(!(tfp.isConnect)){
            tfp.connect()
        }
        var content = ""
        //taxifi
        tfp.sdownCommand(serverPath, object : OutputStream() {
            override fun write(p0: Int) {
                TODO("Not yet implemented")
            }

            @SuppressLint("NewApi")
            @Throws(IOException::class)
            override fun write(b: ByteArray) {
                content =  b.toString(Charsets.UTF_8)
            }
        }, 3)

        return content
    }
}