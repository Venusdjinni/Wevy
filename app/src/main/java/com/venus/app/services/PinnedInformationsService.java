package com.venus.app.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcelable;
import android.support.v4.app.NotificationCompat;
import com.venus.app.Base.AbstractInformation;
import com.venus.app.wevy.PinActivity;
import com.venus.app.wevy.Utilities;

import java.util.*;

public class PinnedInformationsService extends Service {
    private static final String ARG_INTENT = "intent";
    public static final int NOTIF_ID = 50;
    private IBinder mBinder = new LocalBinder();
    private static HashSet<AbstractInformation> informations = new HashSet<>();

    public static void startService(Context context, AbstractInformation ai) {
        startService(context, Collections.singletonList(ai));
    }

    public static void startService(Context context, Collection<AbstractInformation> ai) {
        informations.addAll(ai);
        context.startService(new Intent(context, PinnedInformationsService.class).putParcelableArrayListExtra(ARG_INTENT, new ArrayList<Parcelable>(informations)));
    }

    public static void startService(Context context, Collection<AbstractInformation> ai, Collection<AbstractInformation> rai) {
        if (ai != null) informations.addAll(ai);
        if (rai != null) informations.removeAll(rai);
        context.startService(new Intent(context, PinnedInformationsService.class).putParcelableArrayListExtra(ARG_INTENT, new ArrayList<Parcelable>(informations)));
    }

    public static List<AbstractInformation> getInformations() { return new ArrayList<>(informations); }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (informations.isEmpty())
            for (Parcelable p : intent.getParcelableArrayListExtra(ARG_INTENT))
                informations.add((AbstractInformation) p);
        /*new Handler().post(new Runnable() {
            @Override
            public void run() {
                initService();
            }
        });
        return START_REDELIVER_INTENT;*/
        return initService();
    }

    private int initService() {
        if (informations.size() > 0) {
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */,
                    new Intent(this, PinActivity.class).putParcelableArrayListExtra(PinActivity.ARG_INTENT_DATA, new ArrayList<Parcelable>(informations)),
                    PendingIntent.FLAG_UPDATE_CURRENT);

            String btext = "";
            for (AbstractInformation i : informations)
                btext += "\n" + i.getTitre() + ": " + i.getDescription();

            NotificationCompat.Builder nb = Utilities.getDefaultNotification(this);
            nb.setContentText(informations.size() > 1 ? informations.size() + " informations épinglées" : "une information épinglée");
            //nb.setStyle(new NotificationCompat.BigTextStyle().setBigContentTitle(getString(R.string.app_name)).bigText(btext.substring(1)));
            nb.setContentIntent(pendingIntent);
            nb.setDefaults(Notification.DEFAULT_LIGHTS);

            startForeground(NOTIF_ID, nb.build());
            return START_REDELIVER_INTENT;
        } else {
            stopForeground(true);
            return START_NOT_STICKY;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class LocalBinder extends Binder {
        public PinnedInformationsService getService() {
            // Return this instance of LocalService so clients can call public methods
            return PinnedInformationsService.this;
        }
    }
}
