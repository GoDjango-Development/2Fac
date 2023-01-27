package com.smartestidea.a2fac.ui.viewmodel.settings

import android.content.Context
import android.util.Log
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.material.snackbar.Snackbar
import com.smartestidea.a2fac.R
import com.smartestidea.a2fac.core.ServerListener
import com.smartestidea.a2fac.core.TYPE
import com.smartestidea.a2fac.data.model.Configuration
import com.smartestidea.a2fac.data.model.ConfigurationReceiver
import com.smartestidea.a2fac.data.model.ConfigurationSender
import com.smartestidea.a2fac.data.model.toProtocol
import com.smartestidea.a2fac.domain.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import java.security.spec.InvalidKeySpecException
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

    fun onCreate(){
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
    fun update(configuration: Configuration){
        viewModelScope.launch {
            startLoading()
            updateUseCase(configuration)
            getAll()
            stopLoading()
        }
    }
    fun delete(id:Int,coordinator: CoordinatorLayout,context: Context){
        viewModelScope.launch{
            startLoading()
            val configuration: Configuration? =
                configurations.value?.filter { it.id == id }?.get(0)
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
            stopLoading()
        }
    }
    private suspend fun getAll(){
        val configs = getUseCase()
        Log.i("id-type-vm", configs.joinToString { it.name+"->"})
        if(configs.isNotEmpty())
            configurations.postValue(configs)
        else
            configurations.postValue(emptyList())
    }
    fun updatePos(id:Int,pos:Int){
        val configuration: Configuration? =
            configurations.value?.filter { it.id == id }?.get(0)
        viewModelScope.launch{
            updatePosUseCase(id,pos,if(configuration is ConfigurationReceiver) TYPE.RECEIVER else TYPE.SENDER)
            getAll()
        }
    }

    fun connect(id:Int, coordinator: CoordinatorLayout, context: Context, type: TYPE, setChecked:(Boolean)->Unit){
        viewModelScope.launch(Dispatchers.IO) {
            startLoading()
            val configuration: Configuration? =
                configurations.value?.filter { it.id == id && type == it.getType() }?.get(0)
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
                        connect(id,coordinator,context,type, setChecked)
                    }
                    setActionTextColor(ContextCompat.getColor(context,R.color.white))
                    setTextColor(ContextCompat.getColor(context,R.color.white))
                    show()
                }
                Log.e("TFP_ERROR", e.message.toString())
                configuration?.getType()?.let { of(id, it) }
                withContext(Dispatchers.Main){ setChecked(false) }
            }
            stopLoading()
        }
       }
    fun run() {
        viewModelScope.launch{
            ServerListener().execute(Pair((configurations.value?: emptyList()).filter { it.isOn && it.getType() == TYPE.RECEIVER},this@TFPViewModel) as Pair<List<ConfigurationReceiver>, TFPViewModel>?)
        }
    }
    fun of(id: Int,type: TYPE){
        viewModelScope.launch{
            startLoading()
            ServerListener().cancel(true)
            updateStateUseCase(false,id,type)
            stopLoading()
        }
    }
    fun on(id: Int,type: TYPE){
        viewModelScope.launch{
            startLoading()
            ServerListener().cancel(true)
            updateStateUseCase(true,id,type)
            stopLoading()
        }
    }

    private fun startLoading() = loading.postValue(true)
    private fun stopLoading() = loading.postValue(false)
}