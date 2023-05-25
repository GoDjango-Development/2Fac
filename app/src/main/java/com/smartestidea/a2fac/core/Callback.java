package com.smartestidea.a2fac.core;

import android.util.Log;

import com.nerox.client.Tfprotocol;
import com.nerox.client.callbacks.ITfprotocolCallback;
import com.nerox.client.connection.Easyreum;
import com.nerox.client.constants.TfprotocolConsts;
import com.nerox.client.misc.FileStat;
import com.nerox.client.misc.StatusInfo;
import com.nerox.client.misc.StatusServer;

import java.util.Date;

public class Callback implements ITfprotocolCallback{

     @Override
     public void echoCallback(String value) {
         Log.e("echo",value);
     }

     @Override
     public void responseServerCallback(StatusInfo si) {
         System.out.println("response"); //To change body of generated methods, choose Tools | Templates.
     }

     @Override
     public void instanceTfProtocol(Tfprotocol t) {
         System.out.println("instance"); //To change body of generated methods, choose Tools | Templates.
     }


     @Override
     public void statusServer(StatusInfo status) {
         Log.i("statusServer", status.getStatus().toString());
     }

     @Override
     public void supCallback(StatusInfo sup) {
         Log.d("sup",sup.getMessage());
     }

     @Override
     public void fstatlsCommand(byte b, TfprotocolConsts.FSTYPE value, long wrap, long wrap1, long wrap2) {
         Log.d("fstatls",value.name());
     }

     @Override
     public void fstatCallback(FileStat filestat, StatusInfo status) {
         Log.d("fstat",status.getStatus()+" " + status.getCode());
     }

     @Override
     public void sdownCallback(StatusInfo status) {
         Log.d("sdownCallback",status.getStatus()+" " + status.getCode());
     }

     @Override
     public void lsv2Callback(StatusInfo buildStatusInfo) {
         Log.i("LSV2",buildStatusInfo.getMessage()+" "+buildStatusInfo.getStatus().name()+" "+buildStatusInfo.getCode());
     }

     @Override
     public void mkdirCallback(StatusInfo status) {
         Log.i("MKDIR",status.getMessage());
     }

    @Override
    public void delCallback(StatusInfo status) {
        Log.i("DEL", status.getMessage());
    }
}


