package com.venus.app.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.venus.app.Base.AbstractInformation;
import com.venus.app.DAO.DAODiscussion;
import com.venus.app.DAO.DAOInformation;
import com.venus.app.wevy.MainActivity;
import com.venus.app.wevy.R;
import com.venus.app.wevy.Utilities;

import java.util.Collections;

/**
 * Created by arnold on 25/10/17.
 */
public class MyFireBaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseMsgService";
    private static final int NOTIF_ID_IN = 20, NOTIF_ID_IA = 21, NOTIF_ID_ND = 22;
    public static int countIN = 0, countIA = 0, countND = 0;
    private static String discContentTexts = "";

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a notification payload.
        if (remoteMessage.getData() != null) {
            //Log.d(TAG, "Notification Body: " + remoteMessage.getNotification().getBody());
            sendNotification(remoteMessage.getData().get("title"), remoteMessage.getData().get("body"));
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
    // [END receive_message]

    /**
     * Schedule a job using FirebaseJobDispatcher.
     */
    /*private void scheduleJob() {
        // [START dispatch_job]
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(this));
        Job myJob = dispatcher.newJobBuilder()
                .setService(MyJobService.class)
                .setTag("my-job-tag")
                .build();
        dispatcher.schedule(myJob);
        // [END dispatch_job]
    }*/

    /**
     * Handle time allotted to BroadcastReceivers.
     */
    /*private void handleNow() {
        Log.d(TAG, "Short lived task is done.");
    }*/

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param title FCM message body received.
     */

    private void sendNotification(String title, String body) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // String channelId = getString(R.string.default_notification_channel_id);
        // Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = Utilities.getDefaultNotification(this)
                        .setContentIntent(pendingIntent);
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        switch (title) {
            case "FCM New Info Notification":
                countIN++;
                String text = countIN > 1 ? countIN + " nouvelles informations" : "Une nouvelle information";
                notificationBuilder.setContentText(text)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .setBigContentTitle(getString(R.string.app_name)).bigText(text));
                notificationManager.notify(NOTIF_ID_IN, notificationBuilder.build());
                break;
            case "FCM New Info Admin Notification":
                countIA++;
                String text2 = "Administrateur, il y a " + (countIA > 1 ? countIA + " nouvelles informations" : "une nouvelle information") + " à contrôler";
                notificationBuilder.setContentText(text2)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .setBigContentTitle(getString(R.string.app_name)).bigText(text2));
                notificationManager.notify(NOTIF_ID_IA, notificationBuilder.build());
                break;
            case "FCM New Disc Notification":
                countND++;
                String text3 = countND > 1 ? countND + " nouvelles discussions" : "Une nouvelle discussion";

                // On récupère les data
                if (body != null) discContentTexts += "\n" + body;
                notificationBuilder.setContentText(text3)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .setBigContentTitle(text3).bigText(discContentTexts.substring(1)));
                notificationManager.notify(NOTIF_ID_ND, notificationBuilder.build());
                break;
            case "FCM Del Information":
                // On supprime les discussions
                DAODiscussion daod = new DAODiscussion(this);
                daod.open();
                daod.removeAllDiscs(Integer.parseInt(body));
                daod.close();
                // puis l'information
                DAOInformation daoi = new DAOInformation(this);
                daoi.open();
                PinnedInformationsService.startService(this, null, Collections.singletonList((AbstractInformation) daoi.getInfo(Integer.parseInt(body))));
                daoi.removeInfo(Integer.parseInt(body));
                daoi.close();
                // TODO: est ce que l'information va juste disparaitre comme ca?
                // TODO: Non. Creer un adapter "gris" qui va dire "info supprimée" avec un bouton "OK" pour confirmer la suppression
                break;
            default:
                Log.e(TAG, "No notification case matching");
                break;
        }
    }
}
