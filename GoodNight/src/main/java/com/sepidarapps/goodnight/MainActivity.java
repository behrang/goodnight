package com.sepidarapps.goodnight;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

@SuppressWarnings("deprecation")
public class MainActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceManager.setDefaultValues(this, R.xml.preference, true);

        callQuietModeReceiver();

        addPreferencesFromResource(R.xml.preference);

        updateAllowCallsFromSummary();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //noinspection ConstantConditions
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //noinspection ConstantConditions
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if ("quiet".equals(key)) {
            callQuietModeReceiver();
        } else if ("scheduled".equals(key) || "scheduleFrom".equals(key) || "scheduleTo".equals(key)) {
            callScheduleReceiver();
        } else if ("allow".equals(key)) {
            updateAllowCallsFromSummary();
        }
    }

    private void callQuietModeReceiver() {
        Intent intent = new Intent(this, QuietModeReceiver.class);
        sendBroadcast(intent);
    }

    private void callScheduleReceiver() {
        Intent intent = new Intent(this, ScheduleReceiver.class);
        sendBroadcast(intent);
    }

    private void updateAllowCallsFromSummary() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String allow = preferences.getString("allow", getString(R.string.allow_calls_from));
        Preference allowCallsFromPreference = findPreference("allow");
        if (allowCallsFromPreference != null) {
            if ("-1".equals(allow)) {
                allowCallsFromPreference.setSummary(getString(R.string.allow_calls_from_no_one));
            } else if ("-2".equals(allow)) {
                allowCallsFromPreference.setSummary(getString(R.string.allow_calls_from_favorites));
            } else if ("-3".equals(allow)) {
                allowCallsFromPreference.setSummary(getString(R.string.allow_calls_from_all_contacts));
            } else if ("-4".equals(allow)) {
                allowCallsFromPreference.setSummary(getString(R.string.allow_calls_from_everyone));
            }
        }
    }
}
