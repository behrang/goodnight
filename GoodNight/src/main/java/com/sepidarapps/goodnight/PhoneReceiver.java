package com.sepidarapps.goodnight;

import android.content.*;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.util.*;

public class PhoneReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        Bundle extras = intent.getExtras();
        if (extras != null) {
            String state = extras.getString(TelephonyManager.EXTRA_STATE);
            if (TelephonyManager.EXTRA_STATE_RINGING.equals(state)) {
                String phoneNumber = extras.getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
                if (isNumberAllowed(context, phoneNumber) || isRepeatedCall(context, phoneNumber)) {
                    setRingerModeChangedReceiverState(context, false);
                    audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                    setRingerModeChangedReceiverState(context, true);
                }
                savePhoneNumber(context, phoneNumber);
            } else if (TelephonyManager.EXTRA_STATE_IDLE.equals(state)) {
                audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
            }
        }
    }

    private void setRingerModeChangedReceiverState(Context context, boolean enabled) {
        int flag = enabled ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
        ComponentName ringerModeChangedReceiver = new ComponentName(context, RingerModeChangedReceiver.class);
        PackageManager packageManager = context.getPackageManager();
        if (packageManager != null) {
            packageManager.setComponentEnabledSetting(ringerModeChangedReceiver, flag, PackageManager.DONT_KILL_APP);
        }
    }

    private void savePhoneNumber(Context context, String phoneNumber) {
        Map<String, Long> map = getRecentCalls(context);
        map.put(phoneNumber, new Date().getTime());
        saveRecentCalls(context, map);
    }

    private Map<String, Long> getRecentCalls(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String recentCallsString = preferences.getString("recentCalls", "");
        Map<String, Long> recentCallsMap = parseRecentCalls(recentCallsString);
        return filterRecentCalls(recentCallsMap);
    }

    private Map<String, Long> parseRecentCalls(String recentCalls) {
        Map<String, Long> map = new HashMap<String, Long>();
        String[] calls = recentCalls.split("&");
        for (String call : calls) {
            String[] pair = call.split("@");
            if (pair.length == 2) {
                map.put(pair[0], Long.valueOf(pair[1]));
            }
        }
        return map;
    }

    private Map<String, Long> filterRecentCalls(Map<String, Long> recentCallsMap) {
        Map<String, Long> result = new HashMap<String, Long>();
        for (Map.Entry<String, Long> call : recentCallsMap.entrySet()) {
            if (new Date().getTime() - call.getValue() < 3 * 60 * 1000) {
                result.put(call.getKey(), call.getValue());
            }
        }
        return result;
    }

    private void saveRecentCalls(Context context, Map<String, Long> map) {
        List<String> recentCallsList = new ArrayList<String>();
        for (Map.Entry<String, Long> call : map.entrySet()) {
            recentCallsList.add(call.getKey() + "@" + String.valueOf(call.getValue()));
        }
        String recentCallsString = TextUtils.join("&", recentCallsList);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("recentCalls", recentCallsString);
        editor.commit();
    }

    private boolean isNumberAllowed(Context context, String phoneNumber) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String allow = preferences.getString("allow", context.getString(R.string.allow_calls_from));
        //noinspection ConstantConditions
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        String[] projection = new String[]{ContactsContract.PhoneLookup._ID, ContactsContract.PhoneLookup.STARRED};
        if ("-1".equals(allow)) {
            return false;
        } else if ("-2".equals(allow)) {
            //noinspection ConstantConditions
            Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    if (cursor.getInt(cursor.getColumnIndex(ContactsContract.PhoneLookup.STARRED)) == 1) {
                        return true;
                    }
                }
            }
            return false;
        } else if ("-3".equals(allow)) {
            //noinspection ConstantConditions
            Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
            return cursor != null && cursor.getCount() > 0;
        } else if ("-4".equals(allow)) {
            return true;
        }
        return false;
    }

    private boolean isRepeatedCall(Context context, String phoneNumber) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean repeated = preferences.getBoolean("repeated", context.getResources().getBoolean(R.bool.repeated));
        if (repeated) {

            Set<Integer> ids = new HashSet<Integer>();
            //noinspection ConstantConditions
            Cursor callers = context.getContentResolver().query(
                    Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber)),
                    new String[]{ContactsContract.PhoneLookup._ID},
                    null,
                    null,
                    null
            );
            if (callers != null) {
                while (callers.moveToNext()) {
                    ids.add(callers.getInt(callers.getColumnIndex(ContactsContract.PhoneLookup._ID)));
                }
            }

            Map<String, Long> recentCallsMap = getRecentCalls(context);
            if (recentCallsMap.containsKey(phoneNumber)) {
                return true;
            }

            for (String recentNumber : recentCallsMap.keySet()) {
                //noinspection ConstantConditions
                Cursor contacts = context.getContentResolver().query(
                        Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(recentNumber)),
                        new String[]{ContactsContract.PhoneLookup._ID},
                        null,
                        null,
                        null
                );
                if (contacts != null) {
                    while (contacts.moveToNext()) {
                        int id = contacts.getInt(contacts.getColumnIndex(ContactsContract.PhoneLookup._ID));
                        if (ids.contains(id)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
}
