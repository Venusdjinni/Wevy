package com.venus.app.Preferences;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.preference.DialogPreference;
import android.util.AttributeSet;

/**
 * Created by arnold on 24/09/17.
 */
public class TimePreference extends DialogPreference {
    private String time = null;

    public TimePreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public TimePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    void setTime(String time) {
        this.time = time;
        persistString(time);
    }

    String getTime() {
        return time;
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return  a.getString(index);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        setTime(restorePersistedValue ? getPersistedString(time) : defaultValue.toString());
    }

    @Override
    public CharSequence getSummary() {
        return time;
    }
}
