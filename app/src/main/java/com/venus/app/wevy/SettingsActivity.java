package com.venus.app.wevy;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.Preference;
import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompat;
import com.venus.app.Preferences.TimeFragmentPreference;
import com.venus.app.Preferences.TimePreference;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();
    }

    @Override
    protected void onStop() {
        HomeActivity.toRecreate = true;
        // on recree les alarmes
        MainActivity.setAlarms(this);
        super.onStop();
    }

    public static class MyPreferenceFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
            getPreferenceScreen().setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    //SharedPreferences sp = getPreferenceScreen().getSharedPreferences();
                    switch (preference.getKey()) {
                        case "PREF_NOTIF_HOUR":
                            TimePreference timePreference = (TimePreference) preference;
                            timePreference
                                    .setSummary(newValue.toString());
                            break;
                    }
                    System.out.println("pref change");
                    return true;
                }
            });

            // on gere les preferences
            findPreference("PREF_ABOUT").setEnabled(false);

            SharedPreferences sp = getPreferenceScreen().getSharedPreferences();
        }

        @Override
        public void onDisplayPreferenceDialog(Preference preference) {
            DialogFragment fragment;
            if (preference instanceof TimePreference) {
                fragment = TimeFragmentPreference.newInstance(preference.getKey());
                fragment.setTargetFragment(this, 0);
                fragment.show(getActivity().getSupportFragmentManager(), "time_notif");
            } else super.onDisplayPreferenceDialog(preference);
        }

        @Override
        public void onCreatePreferencesFix(@Nullable Bundle savedInstanceState, String rootKey) {

        }
    }
}
