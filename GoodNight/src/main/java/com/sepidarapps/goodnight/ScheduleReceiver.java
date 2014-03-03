package com.sepidarapps.goodnight;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;

import java.util.Calendar;

public class ScheduleReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean scheduled = preferences.getBoolean("scheduled", false);
        String scheduleFrom = preferences.getString("scheduleFrom", "22:00");
        String scheduleTo = preferences.getString("scheduleTo", "07:00");

        if (scheduled && !isSame(scheduleFrom, scheduleTo)) {
            Long next = getNextSchedule(scheduleFrom, scheduleTo);
            scheduleAlarm(context, next);
        } else {
            cancelAlarm(context);
        }
    }

    private boolean isSame(String scheduleFrom, String scheduleTo) {
        int fromHour = TimePreference.getHour(scheduleFrom);
        int fromMinute = TimePreference.getMinute(scheduleFrom);
        int toHour = TimePreference.getHour(scheduleTo);
        int toMinute = TimePreference.getMinute(scheduleTo);
        return fromHour == toHour && fromMinute == toMinute;
    }

    private Long getNextSchedule(String scheduleFrom, String scheduleTo) {
        Calendar now = Calendar.getInstance();
        now.set(Calendar.MILLISECOND, 0);
        now.set(Calendar.SECOND, 0);

        Calendar from = (Calendar) now.clone();
        from.set(Calendar.MINUTE, TimePreference.getMinute(scheduleFrom));
        from.set(Calendar.HOUR_OF_DAY, TimePreference.getHour(scheduleFrom));
        if (from.compareTo(now) <= 0) {
            from.add(Calendar.DAY_OF_MONTH, 1);
        }

        Calendar to = (Calendar) now.clone();
        to.set(Calendar.MINUTE, TimePreference.getMinute(scheduleTo));
        to.set(Calendar.HOUR_OF_DAY, TimePreference.getHour(scheduleTo));
        if (to.compareTo(now) <= 0) {
            to.add(Calendar.DAY_OF_MONTH, 1);
        }

        return Math.min(from.getTimeInMillis(), to.getTimeInMillis());
    }

    private void scheduleAlarm(Context context, Long next) {
        Intent intent = new Intent(context, SwitchQuietModeReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, next, pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, next, pendingIntent);
        }
    }

    private void cancelAlarm(Context context) {
        Intent intent = new Intent(context, SwitchQuietModeReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }
}
