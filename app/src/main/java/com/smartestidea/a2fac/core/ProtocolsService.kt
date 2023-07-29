package com.smartestidea.a2fac.core

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.smartestidea.a2fac.R
import kotlin.system.exitProcess

class ProtocolsService:Service() {

    override fun onBind(p0: Intent?): IBinder? = null
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action){
            Actions.START.toString() -> start()
            Actions.STOP.toString() -> {
                stopSelf()
                exitProcess(0)
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }
    private fun start(){
        val stopIntent = Intent(this,this::class.java).also {
            it.action = Actions.STOP.toString()
        }
        val pendingStopIntent = PendingIntent.getService(this,1,stopIntent,PendingIntent.FLAG_UPDATE_CURRENT)
        val stopAction = NotificationCompat.Action.Builder(android.R.drawable.ic_lock_power_off,getString(android.R.string.cancel),pendingStopIntent).build()
        val notification = NotificationCompat.Builder(this,"protocols_channel")
            .setSmallIcon(R.mipmap.ic_launcher_foreground)
            .setContentTitle(getString(R.string.running))
            .addAction(stopAction)
            .build()
        startForeground(1,notification)
    }
}

enum class Actions {
    START,
    STOP
}