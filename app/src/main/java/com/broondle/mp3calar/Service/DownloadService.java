package com.broondle.mp3calar.Service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.os.Binder;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.broondle.mp3calar.BuildConfig;
import com.broondle.mp3calar.Constants;
import com.broondle.mp3calar.Util.Managers.MediaPlayerManager;
import com.broondle.mp3calar.Util.Network.ExecutorRunner;
import com.broondle.mp3calar.Model.YoutubeDataModel;
import com.broondle.mp3calar.Util.Network.ApiManager;
import com.broondle.mp3calar.Util.Managers.JsonManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public class DownloadService extends Service {

    private final IBinder binder = new DownloadBinder();
    private final Context context = this;

    boolean isDownloading = false;
    YoutubeDataModel downloadingModel;
    int percentage = 0;
    ExecutorRunner executorRunner = new ExecutorRunner();

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(action.equals(Constants.ACTION_DOWNLOAD)){
                try{
                    double totalSize = Double.parseDouble(intent.getExtras().getString(Constants.TOTAL_SIZE));
                    double downloadedSize = Double.parseDouble(intent.getExtras().getString(Constants.DOWNLOADED_SIZE));

                    percentage = (int) ((downloadedSize/totalSize)*100);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
    };

    ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onLost(@NonNull Network network) {
            super.onLost(network);
            Toast.makeText(context, "Error, check your internet connection!", Toast.LENGTH_SHORT).show();
            stopSelf();
        }
    };


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(!intent.hasExtra("youtubeDataModel")){
            Toast.makeText(context, "Unexpected Error!", Toast.LENGTH_SHORT).show();
            Log.e("DownloadService","youtubeDataModel is NULL!");
            stopSelf();
        }

        registerReceiver(broadcastReceiver,new IntentFilter(Constants.ACTION_DOWNLOAD));
        String utubeDataModelJson = intent.getStringExtra("youtubeDataModel");
        downloadingModel = JsonManager.shared().jsonTouTubeDataModel(utubeDataModelJson);

        String url = BuildConfig.HOST_ADRESS+"/watch?v="+ downloadingModel.getVideoID() + "&token="+ BuildConfig.API_TOKEN_KEY;



        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkRequest networkRequest = new NetworkRequest.Builder().build();
            connectivityManager.registerNetworkCallback(networkRequest, networkCallback);
        }

        downloadAudio(url);


        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("Servis ","Durduruldu!");

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            connectivityManager.unregisterNetworkCallback(networkCallback);
        }

        unregisterReceiver(broadcastReceiver);

        if(isDownloading){
            deleteTempFile();
        }
    }

    private void deleteTempFile(){
        File file = new File(Constants.MUSIC_FOLDER_PATH,downloadingModel.getTitle()+".mp3");
        if(file.exists()){
            boolean isdeleted = file.delete();
            if(isdeleted)
                Log.e("File download","Cancelled downloading, temp file deleted.");
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public int getProgressOfDownload() {
        return percentage;
    }

    public YoutubeDataModel getDownloadingModel() {return downloadingModel;}

    private void downloadAudio(String url){
        if(!TextUtils.isEmpty(url)){
            //indirme işlemini yap
            Log.e("URL",url);
            Toast.makeText(context, "Download started! (in Library)", Toast.LENGTH_SHORT).show();
            isDownloading = true;

            executorRunner.execute(new ApiManager.CallableDownloadMp3Api(downloadingModel, url, downloadingModel.getTitle()+".mp3"
                    , Constants.MUSIC_FOLDER_PATH,this), new ExecutorRunner.Callback<Boolean>() {
                @Override
                public void onComplete(Boolean result) {
                    if(result){
                        Toast.makeText(context, "Download Completed!", Toast.LENGTH_SHORT).show();
                        saveThumbnail();
                    }else{
                        Toast.makeText(context, "Download failed or cancelled!", Toast.LENGTH_SHORT).show();
                        deleteTempFile();
                    }
                    MediaPlayerManager.shared(context).getService().refresh_after_download();
                    isDownloading = false;
                    stopSelf();
                }

                @Override
                public void onError(Exception e) {
                    Log.e("Servis err ",e.getLocalizedMessage());
                    isDownloading = false;
                    stopSelf();
                }
            });
        }else{
            Log.e("URL ","BOŞ");
            Toast.makeText(context, "Cant find URL!", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveThumbnail(){
        executorRunner.execute(new ApiManager.CallableThumbAPI(downloadingModel.getThumbnail()), new ExecutorRunner.Callback<Bitmap>() {
            @Override
            public void onComplete(Bitmap result) {
                if (result != null){
                    try (FileOutputStream out = new FileOutputStream(getFilesDir() +"/"+downloadingModel.getTitle()+".png")) {
                        result.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
                        Log.e("Thumbnail Path",getFilesDir() +"/"+downloadingModel.getTitle()+".png");
                        // PNG is a lossless format, the compression factor (100) is ignored
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                }
            }

            @Override
            public void onError(Exception e) {
                Log.e("Thumb API Err",e.getLocalizedMessage());
            }
        });
    }


    public class DownloadBinder extends Binder {
        public DownloadService getService() {
            return DownloadService.this;
        }
    }

}
