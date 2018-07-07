package com.venus.app.wevy;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import com.google.firebase.database.*;
import com.venus.app.Base.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.Normalizer;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by arnold on 23/07/17.
 */
public abstract class Utilities {
    public static final String[] DAYS = new String[] {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche"};

    public static void justifyListViewHeightBasedOnChildren (ListView listView) {

        ListAdapter adapter = listView.getAdapter();

        if (adapter == null) {
            return;
        }
        ViewGroup vg = listView;
        int totalHeight = 0;
        for (int i = 0; i < adapter.getCount(); i++) {
            View listItem = adapter.getView(i, null, vg);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams par = listView.getLayoutParams();
        par.height = totalHeight + (listView.getDividerHeight() * (adapter.getCount() - 1));
        listView.setLayoutParams(par);
        listView.requestLayout();
    }

    public static String parseCalendar(Calendar c){
        String add1 = "", add2 = "";
        if (c.get(Calendar.MONTH) + 1 < 10)
            add1 = "0";
        if (c.get(Calendar.DAY_OF_MONTH) < 10)
            add2 = "0";
        return c.get(Calendar.YEAR) + "-" + add1 + (c.get(Calendar.MONTH) + 1) + "-" + add2 + c.get(Calendar.DAY_OF_MONTH);
    }

    public static Calendar parseCalendar(String s) {
        Calendar c = new GregorianCalendar(Calendar.getInstance().getTimeZone());
        c.set(Calendar.DAY_OF_MONTH, Integer.parseInt(s.substring(8, 10)));
        c.set(Calendar.MONTH, Integer.parseInt(s.substring(5, 7)) - 1);
        c.set(Calendar.YEAR, Integer.parseInt(s.substring(0, 4)));

        return c;
    }

    public static String invertDate(String s) {
        if (s.charAt(2) == '-') // format DD-MM-YYYY
            return s.substring(6, 10) + "-" + s.substring(3, 5) + "-" + s.substring(0, 2);
        else // format YYYY-MM-DD
            return s.substring(8, 10) + "-" + s.substring(5, 7) + "-" + s.substring(0, 4);
    }

    public static int daysBetween(Calendar startDate, Calendar endDate) {
        long end = endDate.getTimeInMillis();
        long start = startDate.getTimeInMillis();
        return (int) TimeUnit.MILLISECONDS.toDays(start - end);
    }

    public static boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static String dayToAbrev(Calendar c) {
        String abrev;
        switch (c.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.MONDAY: abrev = "Lun."; break;
            case Calendar.TUESDAY: abrev = "Mar."; break;
            case Calendar.WEDNESDAY: abrev = "Mer."; break;
            case Calendar.THURSDAY: abrev = "Jeu."; break;
            case Calendar.FRIDAY: abrev = "Ven."; break;
            case Calendar.SATURDAY: abrev = "Sam."; break;
            case Calendar.SUNDAY: abrev = "Dim."; break;
            default: abrev = ""; break;
        }
        return abrev;
    }

    private static class MyComparator implements Comparator<String> {
        @Override
        public int compare(String s, String t1) {
            if (s.compareTo(parseCalendar(Calendar.getInstance())) < 0 && t1.compareTo(parseCalendar(Calendar.getInstance())) < 0)
                return - s.compareTo(t1);
            else if ((s.compareTo(parseCalendar(Calendar.getInstance())) < 0 && t1.compareTo(parseCalendar(Calendar.getInstance())) >= 0) ||
                    (s.compareTo(parseCalendar(Calendar.getInstance())) >= 0 && t1.compareTo(parseCalendar(Calendar.getInstance())) < 0))
                return -s.compareTo(t1);
            else return s.compareTo(t1);
        }
    }

    public static MyComparator InfosComparator() {
        return new MyComparator();
    }

    public static Cours[] convertToCours(JSONArray jsonArray) {
        Cours[] cours = new Cours[jsonArray.length()];
        JSONObject json;
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                json = jsonArray.getJSONObject(i);
                Cours c = new Cours(
                        json.getInt("idCours"),
                        json.getString("nomCours"),
                        json.getString("nomProf"),
                        json.getString("heureD").substring(0, 5),
                        json.getString("heureF").substring(0, 5),
                        TypeCours.buildFromAbbr(json.getString("typeCours")),
                        json.getInt("jour")
                );
                cours[i] = c;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return cours;
    }

    public static Information[] convertToInfos(JSONArray jsonArray) {
        Information[] infos = new Information[jsonArray.length()];
        JSONObject json;
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                json = jsonArray.getJSONObject(i);
                Information in = new Information(
                        json.getString("titre"),
                        json.getString("dateEnreg"),
                        json.getString("echeance"),
                        json.getString("description"),
                        json.getInt("idInfo"),
                        json.getInt("isValide") == 1,
                        json.getString("nomEtudiant"),
                        TypeInformation.buildFromNom(json.getString("typeInformation"))
                );
                infos[i] = in;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return infos;
    }

    public static Discussion[] convertToDiscs(JSONArray jsonArray) {
        Discussion[] discs = new Discussion[jsonArray.length()];
        JSONObject json;
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                json = jsonArray.getJSONObject(i);
                Discussion in = new Discussion(
                        json.getInt("idDiscussion"),
                        json.getInt("idInfo"),
                        json.getString("titre"),
                        json.getString("description"),
                        json.getInt("messageCount")
                );
                in.setEmailAuteur(json.getString("email"));
                in.setAuteur(json.getString("nomEtudiant"));
                discs[i] = in;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return discs;
    }

    public static Object[] updateAbsInfos(Object[] a1, Object[] a2) {
        // actualise la liste a1 par la liste a2
        ArrayList<Object> a = new ArrayList<>();
        Collections.addAll(a, a1.length != 0 ? a1 : a2);

        // on actualise les elements qui ont le meme id, sinon on ajoute une entree
        for (Object o : a2) {
            Information i = (Information) o;
            for (int c = 0; c < a1.length; c++)
            //for (Object i1 : a1)
                 if (a1[c] instanceof Information && ((Information) a1[c]).getIdInformation() == i.getIdInformation()) {
                    // s'ils ont le meme id
                    a.set(a.indexOf(a1[c]), i);
                    break;
                } else if(c == a1.length - 1) a.add(i); // sinon on l'ajoute à la liste
        }

        return a.toArray(new Object[]{});
    }

    public static String stripAccents(String s) {
        s = Normalizer.normalize(s, Normalizer.Form.NFD);
        s = s.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        return s;
    }

    public static ProgressDialog newLoadingDialog(Context context) {
        ProgressDialog dialog = new ProgressDialog(context);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setIndeterminate(true);
        dialog.setMessage("Chargement...");
        return dialog;
    }

    public static NotificationCompat.Builder getDefaultNotification(Context context) {
        return new NotificationCompat.Builder(context/*, channelId*/)
                .setContentTitle(context.getString(R.string.app_name))
                .setSmallIcon(R.drawable.icon_notif)
                .setColor(context.getResources().getColor(R.color.colorPrimaryDark))
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true);
    }

    public static boolean isMyServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i ("isMyServiceRunning?", true+"");
                return true;
            }
        }
        Log.i ("isMyServiceRunning?", false+"");
        return false;
    }

    public static boolean hasConnection(Context context) {
        System.out.println("connect action:" + "CONNECTIVITY_ACTION");
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null && ni.isConnectedOrConnecting();
    }

    public static void moveFirebaseRecord(final String fromPath) {
        final DatabaseReference fRef = FirebaseDatabase.getInstance().getReference("Active").child(fromPath);

        fRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                FirebaseDatabase.getInstance().getReference().child("Inactive")
                        .child(fromPath)
                        .setValue(dataSnapshot.getValue(), new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if (databaseError != null) System.out.println("Copy failed");
                        else {
                            fRef.removeValue();
                            System.out.println("Success");
                        }
                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("Copy failed");
            }
        });
    }

}
