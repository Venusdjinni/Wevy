package com.venus.app.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by arnold on 29/11/17.
 */
public class FbDbLSBroadcastReceiver extends BroadcastReceiver {
    public static final String CONNECTIVITY_ACTION = ConnectivityManager.CONNECTIVITY_ACTION;



    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case MyFirebaseDatabaseListenerService.ARG_NAME:
                // On relance immediatement le service
                context.startService(new Intent(context, MyFirebaseDatabaseListenerService.class));
                break;
            case CONNECTIVITY_ACTION:
                System.out.println("connect action:" + CONNECTIVITY_ACTION);
                ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo ni = cm.getActiveNetworkInfo();
                if (ni != null && ni.isConnectedOrConnecting())
                    context.startService(new Intent(context, MyFirebaseDatabaseListenerService.class));
                break;
        }
    }
}
