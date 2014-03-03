package com.sepidarapps.goodnight;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Calendar;

public class SwitchQuietModeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String scheduleFrom = preferences.getString("scheduleFrom", "22:00");
        String scheduleTo = preferences.getString("scheduleTo", "07:00");

        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("quiet", isInsideQuietTimeSpan(scheduleFrom, scheduleTo));
        editor.commit();

        callQuietModeReceiver(context);
        callScheduleReceiver(context);
    }

    private boolean isInsideQuietTimeSpan(String scheduleFrom, String scheduleTo) {
        Calendar now = Calendar.getInstance();
        now.set(Calendar.MILLISECOND, 0);
        now.set(Calendar.SECOND, 0);

        Calendar from = (Calendar) now.clone();
        from.set(Calendar.MINUTE, TimePreference.getMinute(scheduleFrom));
        from.set(Calendar.HOUR_OF_DAY, TimePreference.getHour(scheduleFrom));

        Calendar to = (Calendar) now.clone();
        to.set(Calendar.MINUTE, TimePreference.getMinute(scheduleTo));
        to.set(Calendar.HOUR_OF_DAY, TimePreference.getHour(scheduleTo));

        return (to.after(from) && now.before(to) && now.compareTo(from) >= 0) || (from.after(to) && (now.before(to) || now.compareTo(from) >= 0));
    }

    private void callQuietModeReceiver(Context context) {
        Intent intent = new Intent(context, QuietModeReceiver.class);
        context.sendBroadcast(intent);
    }

    private void callScheduleReceiver(Context context) {
        Intent intent = new Intent(context, ScheduleReceiver.class);
        context.sendBroadcast(intent);
    }
}
