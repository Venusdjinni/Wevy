package com.venus.app.IO;

import android.os.AsyncTask;
import android.util.Log;
import org.json.JSONArray;

import java.net.URL;
import java.net.URLConnection;

/**
 * Created by arnold on 16/09/16.
 */
public class FetchOnlineAsc extends AsyncTask<String, Integer, JSONArray> {

    private Asyncable mSource = null;
    private String mUrl = null;
    private String code;

    public FetchOnlineAsc(Asyncable source, String url, String code){
        mSource = source;
        mUrl = url;
        this.code = code;
    }

    public Asyncable getSource() {
        return mSource;
    }

    @Override
    protected JSONArray doInBackground(String... strings) {
        JSONArray jsonArray = null;
        String reponse = null;
        if(!isCancelled()){
            try {
                URL url = new URL(mUrl);
                URLConnection conn =  url.openConnection();
                conn.setConnectTimeout(5000); // temps de recherche de la connexion
                conn.setReadTimeout(10000); // temps de lecture des donnees
                conn.setDoOutput(true);
                // on envoie les donnees
                if (strings.length != 0)
                    JSONfunctions.sendData(conn, strings[0]);
                // read the response
                reponse = JSONfunctions.getData(conn);
                jsonArray = new JSONArray(reponse);
            } catch (Exception e) {
                Log.e("log_cat", e.getClass().getSimpleName() + ": " + e.getMessage());
            }
            System.out.println("entree = " + mUrl + ", " + (strings.length != 0 ? strings[0] : ""));
            System.out.println("reponse = " + reponse);
        }
        return jsonArray;
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {

    }

    @Override
    protected void onCancelled(){
        onPostExecute(null);
    }

    @Override
    protected void onPostExecute(JSONArray result){
        mSource.fetchOnlineResult(result, code);
    }
}
