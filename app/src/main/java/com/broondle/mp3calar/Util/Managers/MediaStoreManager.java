package com.broondle.mp3calar.Util.Managers;

import android.content.Context;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.broondle.mp3calar.Model.AudioModel;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class MediaStoreManager {

    private static MediaStoreManager mediaStoreManager;

    public static synchronized MediaStoreManager shared(){

        if (mediaStoreManager == null)
            mediaStoreManager = new MediaStoreManager();

        return mediaStoreManager;
    }

    private MediaStoreManager(){}

    public void queryMediaStore(Context context, String[] projection, Consumer<Cursor> completion){
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";


        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                MediaStore.Audio.Media.DATE_ADDED);

        if(cursor != null){
            completion.accept(cursor);
            cursor.close();
        }
    }

    public void scanMediaStore(Context context, Runnable runnable){
        MediaScannerConnection.scanFile(context,
                new String[] {Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getAbsolutePath()}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("MediaStore", "Finished scanning " + path);
                        if (runnable != null){
                            runnable.run();
                        }
                    }
                });
    }



    private final List<AudioModel> audioModelList = new ArrayList<>();


    public void getAudioListLocal(Context context, Consumer<List<AudioModel>> consumer){

        MediaStoreManager.shared().scanMediaStore(context, new Runnable() {
            @Override
            public void run() {
                audioModelList.clear();

                String[] projections = {MediaStore.Audio.Media.DATA,MediaStore.Audio.Media.TITLE,
                        MediaStore.Audio.Media.ALBUM,MediaStore.Audio.Media.ARTIST,MediaStore.Audio.Media.DATE_ADDED,
                        MediaStore.Audio.Media.DURATION,MediaStore.Audio.Media._ID};

                MediaStoreManager.shared().queryMediaStore(context,projections, cursor -> {
                    while (cursor.moveToNext()) {

                        String audioPath = cursor.getString(0);
                        String name = cursor.getString(1);
                        String album = cursor.getString(2);
                        String artist = cursor.getString(3);
                        long dateAdded = cursor.getLong(4);
                        long duration = cursor.getLong(5);
                        int id = cursor.getInt(6);
                        String videoID = String.valueOf(id);
                        String thumb_url = name+".png";
                        String lenghtSeconds = String.valueOf(duration);
                        audioModelList.add(new AudioModel(audioPath, name, album, artist, videoID, dateAdded, thumb_url, lenghtSeconds));
                    }
                });
                consumer.accept(audioModelList);
            }
        });
    }


}
