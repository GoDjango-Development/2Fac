package com.smartestidea.a2fac.core

import android.os.AsyncTask
import android.os.Environment
import android.util.Log
import com.smartestidea.a2fac.core.gsm.SmsSender
import com.smartestidea.a2fac.data.model.Configuration
import com.smartestidea.a2fac.data.model.ConfigurationReceiver
import com.smartestidea.a2fac.data.model.toProtocol
import com.smartestidea.a2fac.ui.viewmodel.settings.TFPViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.*
import javax.inject.Singleton


@Singleton
class ServerListener: AsyncTask<Pair<List<ConfigurationReceiver>, TFPViewModel>, Void, Boolean>() {
    override fun doInBackground(vararg p0: Pair<List<ConfigurationReceiver>,TFPViewModel>?): Boolean? {
        val protocols: List<ConfigurationReceiver> = p0[0]?.first ?: emptyList()
        val viewModel = p0[0]?.second
        val timeStart = Calendar.getInstance().timeInMillis
        val timers:Array<Long?> = arrayOfNulls(protocols.size)
        timers.fill(timeStart)
        if(protocols.isNotEmpty())
            while(true) {
                protocols.forEach { CoroutineScope(Dispatchers.IO).launch {

                    try {
                        if ((Calendar.getInstance().timeInMillis - timers[protocols.indexOf(it)]!!) >= it.interval) {

                            val interval =
                                (Calendar.getInstance().timeInMillis - timers[protocols.indexOf(it)]!!).toString() + " :: " + it.interval.toString()
                            Log.i("interval", interval+" "+it.name)


                            timers[protocols.indexOf(it)] = Calendar.getInstance().timeInMillis

                            val tfp = it.toProtocol()
                            it.safeFolders.forEach { safeFolder->

                            val lsvFileName =
                                "lsv2Response-${safeFolder}-${it.name}.txt"

                            tfp.connect()
                            tfp.lsv2Command(safeFolder, "$safeFolder/$lsvFileName")

                            var root =
                                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                            root = File(root, "2FAC" + it.name + (Calendar.getInstance().timeInMillis) + ".txt")

                            val fOut = FileOutputStream(root, false)

                            tfp.sdownCommand("$safeFolder/$lsvFileName", fOut, 0)
                            fOut.close()

                            val s = Scanner(root)
                            val builder = StringBuilder()

                            while (s.hasNextLine()) builder.append(s.nextLine())

                            val files = builder.split("$safeFolder/")
                            val proto = it
                            val filesToSend = files.filter {!proto.alreadyDownloads.contains(it) && it != lsvFileName}

                            filesToSend.forEach { fileName ->
                                Log.i("SMS_FILE_TO_SEND", fileName)
                                var rootForOne =
                                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                                rootForOne =
                                    File(rootForOne, "2FAC" + proto.name + "ForSent" + ".txt")
                                val fOutForOne = FileOutputStream(rootForOne, false)
                                tfp.sdownCommand("$safeFolder/$fileName", fOutForOne, 0)
                                val sForOne = Scanner(rootForOne)
                                val builderForOne = StringBuilder()
                                while (sForOne.hasNextLine()) builderForOne.append(sForOne.nextLine())
                                if (fileName.isNotEmpty() && builderForOne.toString().isNotEmpty())
                                    Log.i("SMS_SEND", "$builderForOne $fileName")
                                    //SmsSender(fileName, builderForOne.toString())
                                fOutForOne.close()
                                it.alreadyDownloads.add(fileName)
                                viewModel?.update(it)
                                delay(1000)
                            }

                        }

                        }
                    }
                    catch (e:Exception){
                        Log.e("ServerListener",e.message?:"")
                    }
                }
            }
        }
        return true
    }
}