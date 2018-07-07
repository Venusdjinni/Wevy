package com.venus.app.services;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.venus.app.IO.Asyncable;
import com.venus.app.IO.SendToServerAsc;
import com.venus.app.wevy.MainActivity;
import com.venus.app.wevy.R;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by arnold on 25/10/17.
 */
public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService implements Asyncable {
    private static final String FOA_SEND_TOKEN = "send token";

    @Override
    public void onTokenRefresh() {
        // on envoie le token de l'utilisateur au serveur
        super.onTokenRefresh();
        System.out.println("\n\non token refresh\n\n");
        String token = FirebaseInstanceId.getInstance().getToken();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (!preferences.getBoolean(MainActivity.PREF_NEW_USER, true)) {
            String url = "http://" + preferences.getString("PREF_IP", "http://wevy.sitimmo.cm/") + "/Wevy/Scripts/";
            url = getString(R.string.servername);
            String data = "";
            try {
                data = "email=" + URLEncoder.encode(preferences.getString(MainActivity.PREF_EMAIL, MainActivity.PREF_EMAIL_DEF), "UTF-8") +
                        "&" + "token=" + URLEncoder.encode(token, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            new SendToServerAsc(this, url + "setToken.php", FOA_SEND_TOKEN).execute(data);
        }
    }

    @Override
    public void fetchOnlineResult(Object result, String code) {
        System.out.println("token result = " + result);
        if (code.equals(FOA_SEND_TOKEN) && result != null && !result.toString().isEmpty()) {
            if (result.toString().startsWith("1")) System.out.println("Token mis à jour!");
        }
    }
}
