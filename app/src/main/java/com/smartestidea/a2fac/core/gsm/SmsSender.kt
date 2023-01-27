package com.smartestidea.a2fac.core.gsm

import android.telephony.SmsManager
import android.util.Log


class SmsSender(number: String, msg: String) {

    init{
        val smsManager = SmsManager.getDefault()
        smsManager.sendTextMessage(number, null, msg.substringBefore("__"), null, null)
    }


}
