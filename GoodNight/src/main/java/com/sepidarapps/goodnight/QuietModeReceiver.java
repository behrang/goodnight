package com.sepidarapps.goodnight;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

public class QuietModeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean quiet = preferences.getBoolean("quiet", false);

        if (quiet) {
            showNotification(context);
            setSilentModeState(context, true);
            setPhoneInterceptorState(context, true);
            setRingerModeChangedReceiverState(context, true);
        } else {
            setRingerModeChangedReceiverState(context, false);
            setPhoneInterceptorState(context, false);
            setSilentModeState(context, false);
            hideNotification(context);
        }
    }

    private void showNotification(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent mainActivityIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, mainActivityIntent, 0);
        //noinspection ConstantConditions
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_notification)
                .setLargeIcon(((BitmapDrawable) context.getResources().getDrawable(R.drawable.ic_launcher)).getBitmap())
                .setContentTitle(context.getString(R.string.notification_title))
                .setContentText(context.getString(R.string.notification_text))
                .setTicker(context.getString(R.string.notification_text))
                .setOngoing(true)
                .setContentIntent(pendingIntent);
        notificationManager.notify(0, builder.build());
    }

    private void hideNotification(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(0);
    }

    private void setSilentModeState(Context context, boolean enabled) {
        int flag = enabled ? AudioManager.RINGER_MODE_SILENT : AudioManager.RINGER_MODE_NORMAL;
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setRingerMode(flag);
    }

    private void setRingerModeChangedReceiverState(Context context, boolean enabled) {
        int flag = enabled ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
        ComponentName ringerModeChangedReceiver = new ComponentName(context, RingerModeChangedReceiver.class);
        PackageManager packageManager = context.getPackageManager();
        if (packageManager != null) {
            packageManager.setComponentEnabledSetting(ringerModeChangedReceiver, flag, PackageManager.DONT_KILL_APP);
        }
    }

    private void setPhoneInterceptorState(Context context, boolean enabled) {
        int flag = enabled ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
        ComponentName phoneReceiver = new ComponentName(context, PhoneReceiver.class);
        PackageManager packageManager = context.getPackageManager();
        if (packageManager != null) {
            packageManager.setComponentEnabledSetting(phoneReceiver, flag, PackageManager.DONT_KILL_APP);
        }
    }
}
