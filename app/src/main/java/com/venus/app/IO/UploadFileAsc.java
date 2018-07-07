package com.venus.app.IO;

import android.os.AsyncTask;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by arnold on 28/03/17.
 */
public class UploadFileAsc extends AsyncTask<String, Integer, Boolean> {
    private Asyncable mSource;
    private String mUrl = null;
    private String code;

    public UploadFileAsc(Asyncable asyncable, String url, String code){
        mSource = asyncable;
        mUrl = url;
        this.code = code;
    }

    @Override
    protected Boolean doInBackground(String... strings) {
        // strings[0] est le chemin de l'image du lieu
        try {
            String sourceFileUri;
            if (strings.length != 0) sourceFileUri = strings[0]; else return false;
            System.out.println("uploadfileasc: image = " + strings[0]);


            HttpURLConnection conn = null;
            DataOutputStream dos = null;
            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "*****";
            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 1024 * 1024;
            File sourceFile = new File(sourceFileUri);

            if (sourceFile.isFile()) {

                try {
                    // open a URL connection to the Servlet
                    FileInputStream fileInputStream = new FileInputStream(sourceFile);
                    URL url = new URL(mUrl);

                    // Open a HTTP connection to the URL
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setDoInput(true); // Allow Inputs
                    conn.setDoOutput(true); // Allow Outputs
                    conn.setUseCaches(false); // Don't use a Cached Copy
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Connection", "Keep-Alive");
                    conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                    conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                    conn.setRequestProperty("bill", sourceFileUri);

                    // puis on traite le fichier
                    dos = new DataOutputStream(conn.getOutputStream());

                    dos.writeBytes(twoHyphens + boundary + lineEnd);
                    dos.writeBytes("Content-Disposition: form-data; name=\"image\";filename=\""
                            + "image" + sourceFileUri.substring(sourceFileUri.lastIndexOf('.')) + "\"" + lineEnd);
                    // l'image se nomme "image.ext"
                    dos.writeBytes(lineEnd);

                    // create a buffer of maximum size
                    bytesAvailable = fileInputStream.available();

                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    buffer = new byte[bufferSize];

                    // read file and write it into form...
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                    while (bytesRead > 0) {
                        dos.write(buffer, 0, bufferSize);
                        bytesAvailable = fileInputStream.available();
                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                    }
                    // send multipart form data necesssary after file
                    // data...
                    dos.writeBytes(lineEnd);
                    dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                    // on lit le resultat
                    String response = JSONfunctions.getData(conn).replace("\n", "");
                    System.out.println("response = " + response);
                    if (response.charAt(0) != '1') return false;

                    // close the streams //
                    fileInputStream.close();
                    dos.flush();
                    dos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            } // End else block
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    protected void onCancelled() {
        onPostExecute(false);
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        mSource.fetchOnlineResult(aBoolean, code);
    }
}
