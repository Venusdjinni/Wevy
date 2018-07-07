package com.venus.app.Preferences;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.preference.DialogPreference;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.widget.TimePicker;

/**
 * Created by arnold on 24/09/17.
 */
public class TimeFragmentPreference extends PreferenceDialogFragmentCompat implements TimePickerDialog.OnTimeSetListener {
    private static final String ARG_KEY = "key";
    private String key;
    private int h, m;

    public static TimeFragmentPreference newInstance(String key) {

        Bundle args = new Bundle();
        args.putString(ARG_KEY, key);
        TimeFragmentPreference fragment = new TimeFragmentPreference();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        key = getArguments().getString(ARG_KEY);
        String time = ((TimePreference) getPreference()).getTime();
        h = Integer.parseInt(time.split(":")[0]);
        m = Integer.parseInt(time.split(":")[1]);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), this, h, m, true);
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {

    }

    @Override
    public void onTimeSet(TimePicker timePicker, int hours, int minutes) {
        h = hours;
        m = minutes;

        String time = ((h < 10) ? "0" + h : h) + ":" + ((m < 10) ? "0" + m : m);
        getPreference().setSummary(time);
        DialogPreference pref = getPreference();
        if (pref instanceof TimePreference)
            if (pref.callChangeListener(time))
                ((TimePreference) pref).setTime(time);
    }
}
