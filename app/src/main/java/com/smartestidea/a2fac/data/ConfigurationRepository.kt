package com.smartestidea.a2fac.data

import android.util.Log
import com.smartestidea.a2fac.core.TYPE
import com.smartestidea.a2fac.data.database.ConfigurationDao
import com.smartestidea.a2fac.data.database.ConfigurationReceiverEntity
import com.smartestidea.a2fac.data.database.ConfigurationSenderEntity
import com.smartestidea.a2fac.data.database.toDomain
import com.smartestidea.a2fac.data.model.Configuration
import com.smartestidea.a2fac.data.model.ConfigurationReceiver
import com.smartestidea.a2fac.data.model.ConfigurationSender
import com.smartestidea.a2fac.data.model.toDomain
import javax.inject.Inject

class ConfigurationRepository @Inject constructor(
    private val dao:ConfigurationDao
) {
    suspend fun getAllConfigurations():List<Configuration> {
        val configurations= mutableListOf<Configuration>()

        configurations.addAll(dao.getAllSenders().map { it.toDomain() })
        configurations.addAll(dao.getAllReceivers().map { it.toDomain() })
        configurations.sortBy { it.pos }
        return configurations
    }
    suspend fun deleteConfiguration(id:Int,type: TYPE) {
       if(type == TYPE.RECEIVER) dao.deleteReceiver(id)
        else dao.deleteSender(id)
    }
    suspend fun insertConfiguration(configuration: Configuration) {
        Log.i("id-type-insert", configuration.name)
        when (configuration.getType()){
            TYPE.RECEIVER -> {
                dao.insertConfigurationReceiver((configuration as ConfigurationReceiver).toDomain())
            }
            TYPE.SENDER-> dao.insertConfigurationSender((configuration as ConfigurationSender).toDomain())
        }

    }
    suspend fun updateConfiguration(configuration: Configuration) {
        if(configuration is ConfigurationReceiver) dao.updateConfigurationReceiver(configuration.toDomain())
        else dao.updateConfigurationSender((configuration as ConfigurationSender).toDomain())
    }
    suspend fun getAllKeys():List<String> = dao.getAllKeys()
    suspend fun updatePos(id: Int,pos:Int, type: TYPE) {
        if(type == TYPE.RECEIVER) dao.updatePosReceiver(id, pos)
        else dao.updatePosSender(id,pos)
    }
    suspend fun setState(isOn:Boolean, id:Int,type: TYPE) {
        if(type == TYPE.RECEIVER) dao.setStateReceiver(isOn, id)
        else dao.setStateSender(isOn,id)
    }
}