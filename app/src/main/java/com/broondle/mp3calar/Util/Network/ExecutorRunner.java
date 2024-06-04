package com.broondle.mp3calar.Util.Network;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ExecutorRunner {
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    public interface Callback<R> {
        void onComplete(R result);
        void onError(Exception e);
    }

    public <R> void execute(Callable<R> callable, Callback<R> callback) {
        // Executor's execute method to execute the task asynchronously
        executor.execute(() -> {
            final R result;
            try {
                result = callable.call();

                handler.post(() -> callback.onComplete(result));
            } catch (Exception e) {
                e.printStackTrace();
                handler.post(() -> callback.onError(e));
            }

        });
    }


}
