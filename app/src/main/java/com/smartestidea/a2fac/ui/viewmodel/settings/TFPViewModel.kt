package com.smartestidea.a2fac.ui.viewmodel.settings

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.material.snackbar.Snackbar
import com.nerox.client.Tfprotocol
import com.smartestidea.a2fac.R
import com.smartestidea.a2fac.core.ServerManager.mutexs
import com.smartestidea.a2fac.core.ServerManager.myExecutor
import com.smartestidea.a2fac.core.ServerManager.protocols
import com.smartestidea.a2fac.core.ServerManager.protocolsRunning
import com.smartestidea.a2fac.core.ServerManager.timeStart
import com.smartestidea.a2fac.core.ServerManager.timers
import com.smartestidea.a2fac.core.TYPE
import com.smartestidea.a2fac.core.gsm.SmsSender
import com.smartestidea.a2fac.data.model.Configuration
import com.smartestidea.a2fac.data.model.ConfigurationReceiver
import com.smartestidea.a2fac.data.model.ConfigurationSender
import com.smartestidea.a2fac.data.model.toProtocol
import com.smartestidea.a2fac.domain.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.IOException
import java.io.OutputStream
import java.security.spec.InvalidKeySpecException
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class TFPViewModel @Inject constructor(
    private val getUseCase: GetAllConfigurationsUseCase,
    private val insertOrUpdateUseCase: InsertConfigurationUseCase,
    private val deleteUseCase: DeleteConfigurationUseCase,
    private val updateUseCase: UpdateConfigurationUseCase,
    private val keysUseCase: GetAllKeysUseCase,
    private val updatePosUseCase: UpdatePosUseCase,
    private val updateStateUseCase: UpdateStateUseCase
) :ViewModel() {
    val loading = MutableLiveData<Boolean>()
    val configurations = MutableLiveData<List<Configuration>>()
    val keys = MutableLiveData<List<String>>()
    fun onCreate() {
        viewModelScope.launch {
            startLoading()
            getAll()
            val keysUC = keysUseCase()
            if(keysUC.isNotEmpty())
                keys.postValue(keysUC)
            stopLoading()
        }
    }
    fun insert(configuration: Configuration){
        viewModelScope.launch {
            startLoading()
            insertOrUpdateUseCase(configuration)
            getAll()
            stopLoading()
        }
    }
    fun update(configuration: Configuration,onFinish:()->Unit = {}){
        viewModelScope.launch {
            updateUseCase(configuration)
            getAll()
            onFinish()
        }
    }
    fun delete(id:String,coordinator: CoordinatorLayout,context: Context){
        Log.i("delete", id)
        configurations.value?.joinToString { "${it.name} -" }?.let { Log.i("configs", it) }
        viewModelScope.launch{
//            startLoading()

            val configuration: Configuration? =
                configurations.value?.filter { it.name == id }?.get(0)
            val copy = configuration?.copy()
            configuration?.let {  deleteUseCase(id, if(it is ConfigurationReceiver) TYPE.RECEIVER else TYPE.SENDER)}
            Snackbar.make(coordinator,context.resources.getString(R.string.deleted), Snackbar.LENGTH_SHORT).apply {
                setBackgroundTint(ContextCompat.getColor(context,R.color.red_error))
                setTextColor(ContextCompat.getColor(context,R.color.white))
                setAction(context.resources.getString(R.string.undo)) {
                    if (copy != null) {
                        insert(copy)
                    }
                }
                setActionTextColor(ContextCompat.getColor(context,R.color.white))
                show()
            }
            getAll()
            stopLoading()
        }
    }
    private suspend fun getAll(){
        val configs = getUseCase()
        configurations.postValue(configs)
    }
    fun updatePos(id:String,pos:Int){
        startLoading()
        viewModelScope.launch{
            getAll()
            val configuration: Configuration? =
            configurations.value?.filter { it.name == id }?.get(0)
            updatePosUseCase(id,pos,if(configuration is ConfigurationReceiver) TYPE.RECEIVER else TYPE.SENDER)
            getAll()
        }
        stopLoading()
    }

    fun connect(id:String, coordinator: CoordinatorLayout, context: Context, type: TYPE, setChecked:(Boolean)->Unit){
        viewModelScope.launch(Dispatchers.IO) {
            startLoading()
            val configuration: Configuration? =
                configurations.value?.filter { it.name == id && type == it.getType() }?.get(0)
            try {
                configuration?.apply {
                    val tfp = this.toProtocol()
                    tfp.connect()
                    tfp.echoCommand("testConnection")
                    if(this is ConfigurationReceiver){
                        safeFolders.forEach{folder->
                            tfp.mkdirCommand(folder)
                        }
                    }else{
                        tfp.mkdirCommand((this as ConfigurationSender).safeFolder)
                        tfp.fstatCommand(this.safeFolder)
                    }
                    on(id, configuration.getType())
                    withContext(Dispatchers.Main){ setChecked(true) }
                    Snackbar.make(coordinator,context.resources.getString(R.string.success), Snackbar.LENGTH_SHORT).apply {
                        setBackgroundTint(ContextCompat.getColor(context,R.color.green_success))
                        setTextColor(ContextCompat.getColor(context,R.color.dark_blue))
                        show()
                    }
                }
            }catch (invalidKey: InvalidKeySpecException){
                Snackbar.make(coordinator,context.resources.getString(R.string.invalid_key), Snackbar.LENGTH_SHORT).show()
                configuration?.getType()?.let { of(id, it) }
                withContext(Dispatchers.Main){ setChecked(false) }
            }
            catch (e:Exception){
                Snackbar.make(coordinator,context.resources.getString(R.string.error), Snackbar.LENGTH_SHORT).apply {
                    setBackgroundTint(ContextCompat.getColor(context,R.color.red_error))
                    setAction(R.string.retry){
                        connect(id,coordinator,context,type ,setChecked)
                    }
                    setActionTextColor(ContextCompat.getColor(context,R.color.white))
                    setTextColor(ContextCompat.getColor(context,R.color.white))
                    show()
                }
                Log.e("TFP_ERROR", e.message.toString()+'\n'+e.stackTraceToString())
                configuration?.getType()?.let { of(id, it) }
                withContext(Dispatchers.Main){ setChecked(false) }
            }
            getAll()
            stopLoading()
        }
       }
    fun run() {
//        viewModelScope.launch{
//            serverListener.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
//            if (serverListener.status != AsyncTask.Status.RUNNING || serverListener.isCancelled)
//                serverListener.execute(listOf( (configurations.value ?: emptyList()).filter { it.isOn && it.getType() == TYPE.RECEIVER }, this@TFPViewModel))
//            else
//                serverListener.setProtocols((configurations.value ?: emptyList()).filter { it.isOn && it.getType() == TYPE.RECEIVER } as List<ConfigurationReceiver>)
//        }
        if (!myExecutor.isShutdown) {
            myExecutor.shutdownNow()
            myExecutor = Executors.newCachedThreadPool()
        }
        protocols = (configurations.value ?: emptyList()).filter { it.isOn && it.getType() == TYPE.RECEIVER } as List<ConfigurationReceiver>
        protocolsRunning.clear()
        timeStart = Calendar.getInstance().timeInMillis
        timers= MutableList(protocols.size){ timeStart}
        mutexs = MutableList(protocols.size){ Mutex() }
        autoRun()
    }
    fun of(id: String,type: TYPE){
        viewModelScope.launch{
            startLoading()
            updateStateUseCase(false,id,type)
            getAll()
            run()
            stopLoading()
        }
    }
    fun on(id: String,type: TYPE){
        viewModelScope.launch{
            startLoading()
            updateStateUseCase(true,id,type)
            getAll()
            run()
            stopLoading()
        }
    }

    private fun startLoading() = loading.postValue(true)
    private fun stopLoading() = loading.postValue(false)

}

fun autoRun(){

    myExecutor.execute {
        val coroutineExceptionHandler = CoroutineExceptionHandler{_, throwable ->
            Log.e("COROUTINE_ERROR",throwable.message.toString())
        }
        if(protocols.isNotEmpty())
            while(true) {
                val setProtocols = hashSetOf<ConfigurationReceiver>()
                protocols.forEach {config->
                    val protocolIndex = protocols.indexOf(config)
                    try {
                    if (
                        protocolIndex != -1 &&
                        protocolIndex<timers.size &&
                        ((Calendar.getInstance().timeInMillis - timers[protocolIndex]) >= (config.interval * 1000))) {
                        timers[protocolIndex] = Calendar.getInstance().timeInMillis
                        setProtocols.add(config)
                    }}catch (e:Exception){
                        Log.e("ERROR", e.stackTraceToString())
                    }
                }
                setProtocols.forEach {config->
                    val protocolIndex = protocols.indexOf(config)
                    CoroutineScope(Dispatchers.IO+coroutineExceptionHandler).launch {
//                            protocolsRunning.add(config)
                        mutexs[protocolIndex].withLock {
                            try {
                                if(protocols.find{it.name == config.name}?.isOn == true)
                                    config.safeFolders.forEach { safeFolder ->
                                        Log.i("SAFE_FOLDER_${config.name}", safeFolder)
                                        val tfp = config.toProtocol()
                                        tfp.tcpTimeOut.connectTimeout = 5000
                                        tfp.tcpTimeOut.connectRetry = 3
                                        tfp.tcpTimeOut.dnsResolutionTimeout = 3000
                                        tfp.connect()
                                        Log.d("PUBLIC_KEY", config.publicKey)
                                        Log.d("PUBLIC_TFP_KEY", tfp.publicKey)

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
                                        val filesToSend =
                                            lsv2Content.split("$safeFolder/").filter {
                                                !it.startsWith("lsv2Response") && it.isNotEmpty() && it != " "
                                            }
                                        filesToSend.forEach { fileName ->

                                            val fileContent = getContent(
                                                "$safeFolder/$fileName",
                                                tfp
                                            )

                                            if (fileName.isNotEmpty() && fileContent.isNotEmpty())
                                                SmsSender(fileName, fileContent)

                                            tfp.delCommand("$safeFolder/$fileName")
                                        }
                                        tfp.delCommand("$safeFolder/$lsvFileName")
                                        tfp.disconnect()
                                    }
                            } catch (e: Exception) {
                                Log.e(
                                    "ServerListenerError",
                                    e.message.toString() + '\n' + e.stackTraceToString()
                                )
                            }
                        }
//                            protocolsRunning.remove(config)
                    }
                }
            }
    }
}
fun getContent(serverPath: String, tfp: Tfprotocol):String{
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
