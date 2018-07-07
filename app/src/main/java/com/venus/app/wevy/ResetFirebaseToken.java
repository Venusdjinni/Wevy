package com.venus.app.wevy;

import android.os.AsyncTask;
import com.google.firebase.iid.FirebaseInstanceId;

import java.io.IOException;

/**
 * Created by arnold on 31/10/17.
 */
public class ResetFirebaseToken extends AsyncTask<Void, Void, Void> {
    @Override
    protected Void doInBackground(Void... voids) {
        try {
            // On supprime le token
            FirebaseInstanceId.getInstance().deleteInstanceId();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        // On cree un nouveau token
        FirebaseInstanceId.getInstance().getToken();
    }
}
