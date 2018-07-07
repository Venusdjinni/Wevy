package com.venus.app.wevy;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import com.venus.app.Utils.Terminating;
import com.venus.app.services.DailyNotificationService;
import com.venus.app.services.DailyPinnedInfosService;
import com.venus.app.services.FbDbLSBroadcastReceiver;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements Terminating {
    public static SharedPreferences preferences;
    public static final String PREF_NAME = "name";
    public static final String PREF_NAME_DEF = "Wevy";
    public static final String PREF_CLASSE = "classe";
    public static final String PREF_CLASSE_DEF = "0-MSP A0";
    public static final String PREF_EMAIL = "email";
    public static final String PREF_EMAIL_DEF = "wevy@wevy.com";
    public static final String PREF_IS_ADMIN = "isAdmin";
    public static final String PREF_IP = "PREF_IP";
    public static final String PREF_TOKEN = "token";
    public static final String PREF_NEW_USER = "pref_new_user";
    public static String PREF_IP_VALUE = "";
    public static String PREF_URL_VALUE = "";
    private static final int PINTENT1 = 10;
    private static final int PINTENT2 = 20;

    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialisation des variables globales
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        PREF_IP_VALUE = preferences.getString(PREF_IP, "192.168.8.150");
        PREF_URL_VALUE = "http://" + PREF_IP_VALUE + "/Wevy/Scripts/";
        PREF_URL_VALUE = getString(R.string.servername);

        if (preferences.getBoolean(PREF_NEW_USER, true)) {
            setContentView(R.layout.activity_main);

            // on cache la statusbar
            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN);
            } else {
                View decorView = getWindow().getDecorView();
                // Hide the status bar.
                int uiOptions = 0;
                uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
                decorView.setSystemUiVisibility(uiOptions);
            }
            getSupportActionBar().hide();

            viewPager = (ViewPager) findViewById(R.id.main_viewpager);
            ((TabLayout) findViewById(R.id.main_tablayout)).setupWithViewPager(viewPager);
            viewPager.setAdapter(new MainPagerAdapter(getSupportFragmentManager()));
        } else {// on va directement à la HomeActivity
            MainActivity.setAlarms(this);
            //registerReceiver(new FbDbLSBroadcastReceiver(), new IntentFilter("android.net.ConnectivityManager.CONNECTIVITY_ACTION"));
            startActivity(new Intent(MainActivity.this, HomeActivity.class));
            finish();
        }
    }

    public void swipeFirst() {
        viewPager.setCurrentItem(1);
    }

    static void setAlarms(Context context) {
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // daily notif
        PendingIntent pintent1 = PendingIntent.getService(context, PINTENT1, new Intent(context, DailyNotificationService.class), PendingIntent.FLAG_CANCEL_CURRENT);
        Calendar time = Calendar.getInstance();
        time.setTimeInMillis(System.currentTimeMillis());
        String stime = preferences.getString("PREF_NOTIF_HOUR", "19:00");
        time.set(Calendar.HOUR_OF_DAY, Integer.parseInt(stime.split(":")[0]));
        time.set(Calendar.MINUTE, Integer.parseInt(stime.split(":")[1]));

        // daily pin
        PendingIntent pintent2 = PendingIntent.getService(context, PINTENT2, new Intent(context, DailyPinnedInfosService.class), PendingIntent.FLAG_CANCEL_CURRENT);
        Calendar time2 = Calendar.getInstance();
        time2.setTimeInMillis(System.currentTimeMillis());
        time2.set(Calendar.HOUR_OF_DAY, 0);
        time2.set(Calendar.MINUTE, 30);

        // on annule d'abord les alarmes
        manager.cancel(pintent1);
        manager.cancel(pintent2);

        // puis on les recree
        manager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                time.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY,
                pintent1);

        manager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                time2.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY,
                pintent2);
    }

    static void stopAlarms(Context context) {
        PendingIntent pintent1 = PendingIntent.getService(context, PINTENT1, new Intent(context, DailyNotificationService.class), PendingIntent.FLAG_CANCEL_CURRENT);
        PendingIntent pintent2 = PendingIntent.getService(context, PINTENT2, new Intent(context, DailyPinnedInfosService.class), PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        manager.cancel(pintent1);
        manager.cancel(pintent2);
    }

    @Override
    public void terminer() {
        startActivity(new Intent(MainActivity.this, HomeActivity.class));
        finish();
    }

    private class MainPagerAdapter extends FragmentStatePagerAdapter {

        public MainPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return MainFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            return 3;
        }
    }
}
