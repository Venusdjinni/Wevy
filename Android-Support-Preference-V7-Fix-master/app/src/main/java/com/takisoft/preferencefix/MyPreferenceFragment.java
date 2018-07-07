package com.takisoft.preferencefix;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.preference.EditTextPreferenceFix;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompatDividers;

/**
 * A placeholder fragment containing a simple view.
 */
public class MyPreferenceFragment extends PreferenceFragmentCompatDividers {

    @Override
    public void onCreatePreferencesFix(@Nullable Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings, rootKey);

        testDynamicPrefs();

        EditTextPreferenceFix etPref = (EditTextPreferenceFix) findPreference("edit_text_fix_test");
        if (etPref != null) {
            int inputType = etPref.getEditText().getInputType();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, @Nullable Bundle savedInstanceState) {
        try {
            return super.onCreateView(inflater, container, savedInstanceState);
        } finally {
            // Uncomment this if you want to change the dividers' style
            // setDividerPreferences(DIVIDER_PADDING_CHILD | DIVIDER_CATEGORY_AFTER_LAST | DIVIDER_CATEGORY_BETWEEN);
        }
    }

    private void testDynamicPrefs() {
        final Context ctx = getPreferenceManager().getContext(); // this is the material styled context

        final PreferenceCategory dynamicCategory = (PreferenceCategory) findPreference("pref_categ");

        Preference prefAdd = findPreference("pref_add");
        if (prefAdd != null) {
            prefAdd.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                private int n = 0;

                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Preference newPreference = new Preference(ctx);

                    newPreference.setTitle("New preference " + n++);
                    newPreference.setSummary(Long.toString(System.currentTimeMillis()));

                    if (dynamicCategory != null) {
                        dynamicCategory.addPreference(newPreference);
                    }
                    return true;
                }
            });
        }
    }
}
