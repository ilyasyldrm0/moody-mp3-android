package com.broondle.mp3calar.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.broondle.mp3calar.Constants;

public class DownloadReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(Constants.ACTION_DOWNLOAD)){
            String extraName1 = Constants.DOWNLOADED_SIZE;
            String extra1 = "0";
            String extraName2 = Constants.TOTAL_SIZE;
            String extra2 = "0";

            if(!TextUtils.isEmpty(intent.getStringExtra(extraName1))){
                extra1 = intent.getStringExtra(extraName1);

            }
            if(!TextUtils.isEmpty(intent.getStringExtra(extraName2))){
                extra2 = intent.getStringExtra(extraName2);
            }

            context.sendBroadcast(new Intent(Constants.ACTION_DOWNLOAD)
                    .putExtra(extraName1,extra1).putExtra(extraName2,extra2));
        }
    }
}
