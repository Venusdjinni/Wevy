package com.venus.app.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import com.venus.app.DAO.DAOInformation;
import com.venus.app.IO.Asyncable;
import com.venus.app.IO.FetchOnlineAsc;
import com.venus.app.IO.SendToServerAsc;
import com.venus.app.wevy.MainActivity;
import com.venus.app.wevy.R;
import com.venus.app.wevy.Utilities;
import org.json.JSONArray;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Ce service a pour but d'aller constamment verifier si de nouvelles informations sont
 * disponibles sur le serveur.
 * Pour cela, il envoie à la page "checkLastId.php" le plus grand ID d'info qu'il possède.
 * Ce dernier va vérifier s'il est égal au plus grand de la bd. Si c'est le cas, le service va télécharger
 * les dernières infos et les enregistrer.
 */

public class CheckInformationsService extends IntentService implements Asyncable {
    public static final String FOA_LAST_ID = "last id";
    public static final String FOA_INFOS = "infos";
    private static final String ARG_NAME = "CheckInformationsService";
    private static final int NOTIF_ID = 20;
    private String url;
    private int lastId, countNewInfo;

    public CheckInformationsService() {
        super(ARG_NAME);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // on verifie si on a deja la derniere info enregistrée
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        url = "http://" + preferences.getString(MainActivity.PREF_IP, "192.168.1.150") + "/Wevy/Scripts/";

        // On recupere le lastId
        DAOInformation dao = new DAOInformation(this);
        Cursor cursor = dao.rawQuery("select max(" + DAOInformation.INFO_ID + ") from " + DAOInformation.S_TABLE_NAME(), new String[]{});
        lastId = cursor.getInt(0);
        cursor.close();

        String data = "";
        try {
            data = "classe=" + URLEncoder.encode(preferences.getString(MainActivity.PREF_CLASSE, MainActivity.PREF_CLASSE_DEF), "UTF-8") +
                "&" + "id=" + lastId;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        new SendToServerAsc(this, url + "checkLastId.php", FOA_LAST_ID).execute(data);
    }

    @Override
    public void fetchOnlineResult(Object result, String code) {
        if (code.equals(FOA_LAST_ID)) {
            if (result != null && result instanceof Integer)
                countNewInfo = Integer.parseInt(result.toString());
                if (countNewInfo > 0) { // Il y a des infos au dessus de la derniere enregistrée dans la bd interne
                    // on part chercher les nouvelles informations
                    System.out.println("intent: idUser = " + countNewInfo + ", idServer = " + Integer.parseInt(result.toString()));
                    System.out.println("On part chercher les nouvelles donnees");
                    new FetchOnlineAsc(this, url + "getInformations.php", FOA_INFOS).execute();
                }
        } else if (code.equals(FOA_INFOS)) {
            System.out.println("recup ok");
            DAOInformation.asyncSave(this, Utilities.convertToInfos((JSONArray) result));

            // On affiche la notification
            NotificationCompat.Builder nb = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText(countNewInfo + " nouvelles informations ont été publiées");

            Intent resultIntent = new Intent(this, MainActivity.class);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            // Adds the back stack for the Intent (but not the Intent itself)
            stackBuilder.addParentStack(MainActivity.class);
            // Adds the Intent that starts the Activity to the top of the stack
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            nb.setContentIntent(resultPendingIntent);

            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            nm.notify(NOTIF_ID, nb.build());
        }
    }
}