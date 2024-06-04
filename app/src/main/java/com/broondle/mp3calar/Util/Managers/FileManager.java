package com.broondle.mp3calar.Util.Managers;

import android.app.RecoverableSecurityException;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;
import androidx.activity.result.IntentSenderRequest;
import com.broondle.mp3calar.Adapters.listAdapter;
import com.broondle.mp3calar.BuildConfig;
import com.broondle.mp3calar.Constants;
import com.broondle.mp3calar.Model.YoutubeDataModel;
import com.broondle.mp3calar.Receivers.DownloadReceiver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class FileManager {

    private static FileManager instance;

    public static synchronized FileManager shared(){
        if(instance == null)
            instance = new FileManager();

        return instance;
    }

    private FileManager(){}


    public boolean downloadFile(YoutubeDataModel youtubeDataModel, String dwnload_file_path, String fileName,
                                String pathToSave, Context context) {
        int totalSize = 0;
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(dwnload_file_path)
                .build();

        try (Response response = client.newCall(request).execute()) {

            if (!response.isSuccessful()) {
                Log.e("Download", "HTTP Error: " + response.code());
                return false;
            }

            // Create a new file to save the downloaded data
            File dir = new File(pathToSave);
            if (!dir.exists()) dir.mkdirs();

            File file = new File(dir, fileName);

            try (InputStream inputStream = response.body().byteStream();
                 FileOutputStream fileOutput = new FileOutputStream(file)) {
                MediaPlayerManager.shared(context).getService().refresh_after_download();

                totalSize = (int) response.body().contentLength();

                Intent broadcast = new Intent(context, DownloadReceiver.class);
                broadcast.setAction(Constants.ACTION_DOWNLOAD);
                broadcast.putExtra(Constants.TOTAL_SIZE, String.valueOf(totalSize));

                byte[] buffer = new byte[65536]; // 64KB
                int bytesRead;
                int downloadedSize = 0;

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    fileOutput.write(buffer, 0, bytesRead);
                    downloadedSize += bytesRead;
                    Log.e("Downloading", "Progress - *" + downloadedSize + "* Total - *" + totalSize + "*");

                    broadcast.putExtra(Constants.DOWNLOADED_SIZE, String.valueOf(downloadedSize));
                    context.sendBroadcast(broadcast);
                    if (!GenelManager.shared().isDownloadServiceRunning(context)) {
                        break;
                    }
                }

                if (downloadedSize == totalSize) {
                    //download success
                    Log.e("Download", "Successful! & " + file.getAbsolutePath());

                    // Metadata operations as in your original code...

                    return true;
                } else {
                    Log.e("Download Failed", "Can't get video! ERROR!");
                    return false;
                }

            } catch (IOException e) {
                if (BuildConfig.DEBUG) e.printStackTrace();
                return false;
            }
        } catch (IOException e) {
            if (BuildConfig.DEBUG) e.printStackTrace();
            return false;
        }
    }

    public File lastFileForDelete;

    public boolean deleteActionAudioFile(Context context, File file, listAdapter.OnDeleteListener deleteListener){
        lastFileForDelete = file;

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            try {

                ContentResolver contentResolver = context.getContentResolver();
                Uri mediaContentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

                String[] projection = new String[]{MediaStore.Audio.Media._ID};
                String selection = MediaStore.Audio.Media.DISPLAY_NAME + "=?";
                String[] selectionArgs = new String[]{file.getName()};

                Cursor cursor = contentResolver.query(mediaContentUri, projection, selection, selectionArgs, null);
                if (cursor != null && cursor.moveToFirst()) {
                    int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
                    long id = cursor.getLong(idColumn);
                    Uri fileUri = ContentUris.withAppendedId(mediaContentUri, id);
                    cursor.close();

                    try {
                        // Silme işlemi
                        int deletedRows = contentResolver.delete(fileUri, null, null);

                        if (deletedRows > 0) {
                            deleteListener.requestDeletePermission(null);
                            MediaPlayerManager.shared(context).getService().refresh_after_download();
                            return true;
                        } else {
                            return false;
                        }

                    } catch (SecurityException securityException) {
                        RecoverableSecurityException recoverableSecurityException = (RecoverableSecurityException) securityException;
                        IntentSenderRequest senderRequest = new IntentSenderRequest.Builder(recoverableSecurityException.getUserAction()
                                .getActionIntent().getIntentSender()).build();
                        deleteListener.requestDeletePermission(senderRequest);

                        return false;
                    }

                } else {
                    if (cursor != null) {
                        cursor.close();
                    }
                    Log.e("DeleteAudio", "Audio file not found.");
                    return false;
                }
            } catch (Exception e) {
                Log.e("Error", e.getLocalizedMessage());
                e.printStackTrace();
                return false;
            }
        }else{
            return file.delete();
        }
    }

    public boolean deleteActionThumbFile(){

        return false;
    }



}