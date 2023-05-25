package com.smartestidea.a2fac.core.gsm

import android.telephony.SmsManager
import android.util.Log


class SmsSender(number: String, msg: String) {

    init{
        Log.i("SMS_SENDER","$number :: $msg")
        val smsManager = SmsManager.getDefault()
        smsManager.sendTextMessage(number.substringBefore("__"), null, msg, null, null)
    }


}
