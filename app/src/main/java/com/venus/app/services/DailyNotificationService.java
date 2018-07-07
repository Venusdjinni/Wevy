package com.venus.app.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.app.NotificationCompat;
import android.support.v7.preference.PreferenceManager;
import com.venus.app.DAO.DAOInformation;
import com.venus.app.DAO.DAONote;
import com.venus.app.wevy.MainActivity;
import com.venus.app.wevy.Utilities;

import java.util.Calendar;

/**
 * Ce service affiche la notification quotidienne, qui rappelle à l'utilisateur ses devoirs
 * à préparer pour le lendemain
 */
public class DailyNotificationService extends IntentService {
    private static final String ARG_NAME = "DailyNotificationService";
    private static final int NOTIF_ID = 10;

    public DailyNotificationService() {
        super(ARG_NAME);
    }

    @Override
    protected void onHandleIntent(Intent intent) {


        // on verifie s'il y a des informations/note d'écheance le lendemain
        Calendar demain = Calendar.getInstance();
        demain.add(Calendar.DAY_OF_MONTH, 1);
        DAOInformation daoi = new DAOInformation(this);
        daoi.open();
        Cursor cursor = daoi.rawQuery("select * from " +
                        "(select count(*) from " + DAOInformation.S_TABLE_NAME() + " where " + DAOInformation.INFO_ECHEANCE + " = ?) as c1, " +
                        "(select count(*) from " + DAONote.S_TABLE_NAME() + " where " + DAONote.NOTE_ECHEANCE + " = ?) as c2",
                new String[]{Utilities.parseCalendar(demain), Utilities.parseCalendar(demain)});
        cursor.moveToNext();
        int count = cursor.getInt(0) + cursor.getInt(1);
        cursor.close();
        daoi.close();

        if (!PreferenceManager.getDefaultSharedPreferences(this).getBoolean("PREF_NOTIF_ANY", false)) {
            // on cree la notification
            String message = count > 1 ? "Vous avez " + count + " activités prévues pour demain" :
                    (count == 1 ? "Vous avez une activité prévue pour demain" : "Il n'y a aucune activité prévue pour demain");
            // Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder nb = Utilities.getDefaultNotification(this)
                    .setContentText(message)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(message));

            Intent resultIntent = new Intent(this, MainActivity.class);
            resultIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            nb.setContentIntent(resultPendingIntent);

            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            nm.notify(NOTIF_ID, nb.build());
        }
    }
}
