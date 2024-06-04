package com.broondle.mp3calar.Util.Managers;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.broondle.mp3calar.Service.MediaPlayerService;

public class MediaPlayerManager {
    private Context context;
    private static MediaPlayerManager instance;
    private MediaPlayerService mediaPlayerService;
    private boolean isBound = false;

    // ServiceConnection nesnesi
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            MediaPlayerService.MediaPlayerBinder binder = (MediaPlayerService.MediaPlayerBinder) service;
            mediaPlayerService = binder.getService();
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            isBound = false;
        }
    };

    // Private constructor to prevent instantiation
    private MediaPlayerManager(Context context) {
        this.context = context.getApplicationContext();
        bindToMediaPlayerService();
    }

    // Public method to get the singleton instance
    public static synchronized MediaPlayerManager shared(Context context) {
        if (instance == null) {
            instance = new MediaPlayerManager(context);
        }
        return instance;
    }

    private void bindToMediaPlayerService() {
        if (!isServiceRunning()) {
            Intent serviceIntent = new Intent(context, MediaPlayerService.class);
            context.startService(serviceIntent);
        }

        Handler checker = new Handler();
        checker.postDelayed(new Runnable() {
            @Override
            public void run() {
                //servisin başlamaması durumunda oluşabilecek bind sorunları için bu kod parçası handler ile kontrol ediyor
                if(isServiceRunning() && !isBound){
                    Log.e("MediaServis","Started and binded(probably)!");
                    Intent intent = new Intent(context, MediaPlayerService.class);
                    context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
                }else{
                    checker.postDelayed(this,150);
                }
            }
        },100);
    }

    public boolean isServiceRunning() {
        // GenelManager içindeki 'isMyServiceRunning' metodunun tanımını buraya ekleyin
        // Örneğin:
        return GenelManager.shared().isMyServiceRunning(context, MediaPlayerService.class);
    }

    public MediaPlayerService getService(){
        return mediaPlayerService;
    }
}
