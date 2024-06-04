package com.broondle.mp3calar.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
//bildirimden gelen receiver yönlendirmesi
public class NotificationActionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        context.sendBroadcast(new Intent("TRACKS_TRACKS")
                .putExtra("actionnameNot",intent.getAction()));
    }

}
