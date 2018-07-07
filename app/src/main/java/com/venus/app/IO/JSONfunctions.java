package com.venus.app.IO;

import android.util.Log;

import java.io.*;
import java.net.URLConnection;

/**
 * Created by arnold on 14/09/16.
 */

public abstract class JSONfunctions {
    public static void sendData(URLConnection connection, String data) {
        try {
            OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
            writer.write(data);
            writer.flush();
        } catch (IOException e) {
            Log.e("log_cat", "IOException: " + e.getMessage());
        }
    }

    public static String getData(URLConnection connection) {
        String response = "";
        try {
            InputStream in = new BufferedInputStream(connection.getInputStream());
            response = convertStreamToString(in);
        } catch (Exception e) {
            Log.e("log_cat", e.getClass().getSimpleName() + ": " + e.getMessage());
        }
        return response;
    }

    public static String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
}
