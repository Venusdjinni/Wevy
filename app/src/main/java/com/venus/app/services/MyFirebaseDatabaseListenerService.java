package com.venus.app.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.*;
import com.venus.app.DAO.DAODiscussion;
import com.venus.app.IO.Asyncable;
import com.venus.app.IO.FetchOnlineAsc;
import com.venus.app.wevy.CompteActivity;
import com.venus.app.wevy.MainActivity;
import com.venus.app.wevy.R;
import com.venus.app.wevy.Utilities;
import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;

import java.util.HashMap;

/**
 * Created by arnold on 28/11/17.
 * Ce service a pour but de surveiller la FbDb en attendant qu'un nouveau message soit posté
 * Il notifie l'utilisateur de ce dernier et enregistre le nombre de messages non lus pour chaque discussion
 */
public class MyFirebaseDatabaseListenerService extends Service
    implements ChildEventListener, Asyncable {
    public static final String ARG_NAME = "FirebaseDatabaseListenerService";
    public String URL;
    public static final String FOA_DISC = "discussions";
    private static final int NOTIF_ID = 30;
    private IBinder mBinder = new LocalBinder();
    private SharedPreferences preferences;
    private HashMap<String, DAODiscussion.DiscItem> discussions;
    private HashMap<String, ChildEventListener> listeners;
    private ChildEventListener unBindListener;
    private DatabaseReference activeReference;
    private DAODiscussion dao;
    private String boundDiscId = "";

    @Override
    public void onCreate() {
        super.onCreate();
        URL = getString(R.string.servername);
        FirebaseApp.initializeApp(this);
        System.out.println("MFDLService on create");

        activeReference = FirebaseDatabase.getInstance().getReference("Active");
        discussions = new HashMap<>();
        listeners = new HashMap<>();
        dao = new DAODiscussion(this);
        dao.open();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        FirebaseApp.initializeApp(this);

        System.out.println("MFDLService on start command");
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                // On recupere toutes les discussions de la classe
                preferences = PreferenceManager.getDefaultSharedPreferences(MyFirebaseDatabaseListenerService.this);
                // On recupere d'abord les non-lus de la derniere session
                for (DAODiscussion.DiscItem d : dao.getAllDiscs())
                    discussions.put(d.getNode(), d);

                new FetchOnlineAsc(MyFirebaseDatabaseListenerService.this, URL + "getDiscussions.php", FOA_DISC)
                        .execute("nomClasse=" + preferences.getString(MainActivity.PREF_CLASSE, MainActivity.PREF_CLASSE_DEF));
            }
        });

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (CompteActivity.deconnexion)
            super.onDestroy();
        else {
            // On sauvegarde les données
            System.out.println("MFDLService onDestroy");
            for (String id : listeners.keySet()) {
                activeReference.child(id).removeEventListener(listeners.get(id));
                activeReference.child(id).removeEventListener(boundedDMListener);
            }
            saveDatas();
            dao.close();
            super.onDestroy();
            // et on lance le broadcast
            sendBroadcast(new Intent(ARG_NAME));
        }
    }

    @Override
    public void fetchOnlineResult(Object result, String code) {
        if (code.equals(FOA_DISC) && result != null) {
            // On connecte chaque noeud de discussion à un listener
            for (DAODiscussion.DiscItem d : DAODiscussion.setExtraDatas(this, Utilities.convertToDiscs((JSONArray) result))) {
                discussions.put(d.getNode(), d);
                listeners.put(d.getNode(), activeReference
                        .child(d.getNode())
                        .addChildEventListener(discMessageListener));
                activeReference.child(d.getNode()).addListenerForSingleValueEvent(notifListener);
            }
            System.out.println("MFDLService fetchOnlineResult");

            // On connecte un listener sur la racine, au cas où une nouvelle discussion est crée
            activeReference
                    .addChildEventListener(this);
        } else {
            if (Utilities.hasConnection(this))
                new FetchOnlineAsc(this, URL + "getDiscussions.php", FOA_DISC)
                    .execute("nomClasse=" + preferences.getString(MainActivity.PREF_CLASSE, MainActivity.PREF_CLASSE_DEF));
        }
    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        // Une nouvelle discussion a été crée
        // on cree un listener pour la discussion
        System.out.println("MFDLService onDiscussionAdded");
        boolean isActive = dataSnapshot.getRef().getParent().getKey().equals("Active");
        if (!discussions.containsKey(dataSnapshot.getKey()) && isActive) {
            System.out.println("Ajout d'une discussion: " + dataSnapshot.getKey());
            listeners.put(dataSnapshot.getKey(), dataSnapshot.getRef().addChildEventListener(discMessageListener));
            discussions.put(dataSnapshot.getKey(), new DAODiscussion.DiscItem(dataSnapshot.getKey(), 0, null));
            dataSnapshot.getRef().addListenerForSingleValueEvent(notifListener);
        }
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {

    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private void sendNotification() {
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // On construit les textes
        String bigText = "";
        int nbDis = 0, nbMes = 0;
        for (DAODiscussion.DiscItem d : discussions.values())
            if (d.getNonLus() > 0) {
                nbDis++;
                nbMes += d.getNonLus();
                bigText += "\n" + d.getTitre() == null ? "" : d.getTitre() + ": " + d.getNonLus() + " message(s) non lu(s)";
            }
        if (nbMes == 0) {
            notificationManager.cancel(NOTIF_ID);
            return;
        }
        String contentText = nbMes + " message(s) de " + nbDis + " discussion(s)";
        bigText = bigText.substring(1);

        // String channelId = getString(R.string.default_notification_channel_id);
        NotificationCompat.Builder notificationBuilder = Utilities.getDefaultNotification(this)
                        .setContentText(contentText)
                        .setStyle(new NotificationCompat.BigTextStyle().setBigContentTitle(getString(R.string.app_name)).bigText(bigText))
                        .setContentIntent(pendingIntent);

        notificationManager.notify(NOTIF_ID, notificationBuilder.build());
    }

    public void saveDatas() {
        // sauvegarde les donnees de messages non-lus
        if (dao.getDb().isOpen())
            for (HashMap.Entry<String, DAODiscussion.DiscItem> e : discussions.entrySet())
                dao.addDiscussion(e.getKey(), e.getValue().getNonLus(), e.getValue().getLastMessageID());
    }

    public void resetDiscNonlus(String id) {
        // reinitialise le compteur d'une discussion
        try {
            discussions.get(id).setNonLus(0);
        } catch (Exception e) {e.printStackTrace();}
    }

    public void discActivityBounded(String id) {
        try {
            System.out.println("Disc Activity bounded: " + id);
            if (discussions.containsKey(id)) {
                discussions.get(id).resetNonLus();
                boundDiscId = id;
                EventBus.getDefault().post(new HAMessageEvent(discussions.get(id).getIdInfo(), true));
                //activeReference.child(id).removeEventListener(listeners.get(id));
            } else discussions.put(id, new DAODiscussion.DiscItem(id, 0, null));
            //unBindListener = activeReference.child(id).addChildEventListener(boundedDMListener);
        } catch (Exception e) {e.printStackTrace();}
        sendNotification();
    }

    public void discActivityUnbounded(String id) {
        try {
            System.out.println("Disc Activity Unbounded: " + id);
            boundDiscId = "";
            //activeReference.child(id).removeEventListener(unBindListener);
            //listeners.put(id, activeReference.child(id).addChildEventListener(discMessageListener));
        } catch (Exception e) {e.printStackTrace();}
    }

    private ChildEventListener discMessageListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            // TODO: Gerer le cas de suppression des discussions: On supprime bien la discussion dans
            // TODO: la bd wevy, mais pas dans FbDb, donc c'est succeptible de creer des appels de listener

            System.out.println("MFDLService on Message Unbound Added");
            // On incrémente le compteur de messages non lus
            String parent = dataSnapshot.getRef().getParent().getKey();
            if (discussions.get(parent).getLastMessageID() == null || discussions.get(parent).notify) {
                System.out.println("disc : parent = " + parent + ", last = " + discussions.get(parent).getLastMessageID() + ", key = " + dataSnapshot.getKey());
                DAODiscussion.DiscItem d = discussions.get(parent);
                if (boundDiscId.isEmpty()) {
                    // pas de discussion affichée
                    d.upvoteNonLus();
                    EventBus.getDefault().post(new DIAMessageEvent(d.getIdDiscussion()));
                    EventBus.getDefault().post(new HAMessageEvent(d.getIdInfo()));
                    d.setLastMessageID(dataSnapshot.getKey());
                    if (dataSnapshot.getKey().equals(discussions.get(parent).getLastMessageID()))
                        discussions.get(parent).notify = true;
                } else d.setLastMessageID(dataSnapshot.getKey());
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private ChildEventListener boundedDMListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            System.out.println("MFDLService onMessage Bounded Added");
            String parent = dataSnapshot.getRef().getParent().getKey();
            DAODiscussion.DiscItem d = discussions.get(parent);
            d.setLastMessageID(dataSnapshot.getKey());
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private ValueEventListener notifListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            // on ne sauvegarde et ne notifie que lorsque tout est chargé
            saveDatas();
            sendNotification();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
        }
    };

    public class LocalBinder extends Binder {
        public MyFirebaseDatabaseListenerService getService() {
            // Return this instance of LocalService so clients can call public methods
            return MyFirebaseDatabaseListenerService.this;
        }
    }

    public static class DIAMessageEvent {
        public int idToUp;

        public DIAMessageEvent(int idToUp) {
            this.idToUp = idToUp;
        }
    }

    public static class HAMessageEvent {
        public int idToUp;
        public boolean reset = false;

        public HAMessageEvent(int idToUp) {
            this.idToUp = idToUp;
        }

        public HAMessageEvent(int idToUp, boolean reset) {
            this.idToUp = idToUp;
            this.reset = reset;
        }
    }
}
