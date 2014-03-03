package com.sepidarapps.goodnight;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootCompletedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        callSwitchQuietModeReceiver(context);
    }

    private void callSwitchQuietModeReceiver(Context context) {
        Intent intent = new Intent(context, SwitchQuietModeReceiver.class);
        context.sendBroadcast(intent);
    }
}
