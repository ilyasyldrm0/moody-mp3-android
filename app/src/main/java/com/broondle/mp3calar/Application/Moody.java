package com.broondle.mp3calar.Application;

import android.app.Application;
import android.os.Debug;
import android.util.Log;

import com.broondle.mp3calar.Util.Managers.MediaPlayerManager;

public class Moody extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        MediaPlayerManager.shared(this);
    }
}
