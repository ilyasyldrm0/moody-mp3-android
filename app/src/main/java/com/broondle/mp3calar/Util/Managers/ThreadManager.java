package com.broondle.mp3calar.Util.Managers;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

public class ThreadManager {

    private static ThreadManager instance;
    private final HandlerThread handlerThread;
    private final Handler backgroundHandler;
    private final Handler mainThreadHandler;

    private ThreadManager(){
        handlerThread = new HandlerThread("AsyncManagerBackgroundThread");
        handlerThread.start();
        backgroundHandler = new Handler(handlerThread.getLooper());
        mainThreadHandler = new Handler(Looper.getMainLooper());
    }

    public static synchronized ThreadManager shared(){
        if(instance == null) {
            instance = new ThreadManager();
        }
        return instance;
    }

    public void runInBackground(final Runnable task){
        backgroundHandler.post(task);
    }

    public void runOnMainThread(final Runnable task){
        mainThreadHandler.post(task);
    }

    // İsteğe bağlı: Nesne bellekten silindiğinde iş parçacığı temizlemesi yapmak
    @Override
    protected void finalize() throws Throwable {
        handlerThread.quitSafely(); // veya quit() fakat quitSafely() daha tercih edilir.
        super.finalize();
    }
}
