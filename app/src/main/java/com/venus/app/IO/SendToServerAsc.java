package com.venus.app.IO;

import android.os.AsyncTask;
import android.util.Log;

import java.net.URL;
import java.net.URLConnection;

/**
 * Created by arnold on 28/10/16.
 */
public class SendToServerAsc extends AsyncTask<String, Void, String> {

    private Asyncable mSource;
    private String murl = null;
    private String code;

    public SendToServerAsc(Asyncable asyncable, String url, String code){
        mSource = asyncable;
        murl = url;
        this.code = code;
    }

    @Override
    protected String doInBackground(String... strings) {
        String reponse = null;
        if (!isCancelled()) {
            try {
                URL url = new URL(murl);
                URLConnection connection = url.openConnection();
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(10000);
                connection.setDoOutput(true);
                // on ecrit les donnees
                JSONfunctions.sendData(connection, strings[0]);

                // et on lit la reponse du serveur
                reponse = JSONfunctions.getData(connection);
            } catch (Exception e) {
                Log.e("log_cat", e.getClass().getSimpleName() + ": " + e.getMessage());
                cancel(true);
            }
        }
        System.out.println("data = " + strings[0]);
        System.out.println("reponse = " + reponse);
        return reponse;
    }

    @Override
    protected void onCancelled(){
        // on n'a pas pu se connecter au serveur
        onPostExecute(null);
    }

    @Override
    protected void onPostExecute(String result){
        mSource.fetchOnlineResult(result, code);
    }
}
