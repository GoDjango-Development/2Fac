package com.smartestidea.a2fac.core.gsm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.telephony.SmsMessage
import android.util.Log
import com.smartestidea.a2fac.R
import com.smartestidea.a2fac.core.di.RoomModule
import com.smartestidea.a2fac.data.model.toDomain
import com.smartestidea.a2fac.data.model.toProtocol
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.nio.charset.StandardCharsets
import java.util.*


class SmsListener : BroadcastReceiver() {
    private val preferences: SharedPreferences? = null
    override fun onReceive(context: Context?, intent: Intent) {
        // TODO Auto-generated method stub
        if (intent.action == "android.provider.Telephony.SMS_RECEIVED") {
            val bundle = intent.extras //---get the SMS message passed in---
            var msgs: Array<SmsMessage?>? = null
            var msgFrom: String
            if (bundle != null) {
                //---retrieve the SMS message received---
                try {
                    val pdus = bundle["pdus"] as Array<*>?
                    msgs = arrayOfNulls(pdus!!.size)
                    for (i in msgs.indices) {
                        msgs[i] = SmsMessage.createFromPdu(pdus[i] as ByteArray)
                        msgFrom = msgs[i]?.originatingAddress ?:""
                        val msgBody: String = msgs[i]?.messageBody ?:""
                        CoroutineScope(Dispatchers.IO).launch {
                            send(msgBody,msgFrom, context)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("Exception caught",e.message?:"");
                }
            }
        }
    }

    private suspend fun send(message: String,from:String,context: Context?) {
        val room= RoomModule()
        val database = context?.let { room.getRoomDatabase(it) }
            ?.let { room.getConfigurationDao(it) }

        val configurations =(database?.getAllSenders()?.map { it.toDomain() })?.filter { message.contains(it.keyword) && it.isOn}

            val stream = ("$message\n").byteInputStream(StandardCharsets.UTF_8)
            configurations?.forEach {
            val tfp = it.toProtocol()
            CoroutineScope(Dispatchers.IO).launch {
                tfp.connect()
                tfp.fstatCommand(it.safeFolder)
                Log.i("Send",message)
                val fileName = "${it.safeFolder}/${from}__${Calendar.getInstance().timeInMillis}.txt"
                Log.i("FileName",fileName)
                tfp.supCommand(
                    fileName,
                    stream,
                    0
                )
            }
        }
    }

}