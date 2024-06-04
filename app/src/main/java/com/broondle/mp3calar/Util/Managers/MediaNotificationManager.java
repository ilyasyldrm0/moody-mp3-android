package com.broondle.mp3calar.Util.Managers;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.broondle.mp3calar.Model.AudioModel;
import com.broondle.mp3calar.R;
import com.broondle.mp3calar.Receivers.NotificationActionReceiver;

/**
 * Keeps track of a notification and updates it automatically for a given MediaSession. This is
 * required so that the music service don't get killed during playback.
 */
public class MediaNotificationManager {

    public static final String CHANNEL_ID = "CHANNEL15", ACTION_PREVIOUS = "actionprevious", ACTION_PLAY = "actionplay", ACTION_NEXT = "actionnext";

    public static Notification notification;

    @SuppressLint({"LaunchActivityFromNotification", "MissingPermission"})
    public static void createNotification(Context context, AudioModel selectedAudio, int position, int playbutton, int size) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return;
        }
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        MediaSessionCompat mediaSessionCompat = new MediaSessionCompat(context, "mediaSession");

        PendingIntent pendingIntentPrev;
        int drw_previous = android.R.drawable.ic_media_previous;
        if (position == 0) {
            pendingIntentPrev = null;
        } else {
            Intent intentPrevious = new Intent(context, NotificationActionReceiver.class)
                    .setAction(ACTION_PREVIOUS);
            pendingIntentPrev = PendingIntent.getBroadcast(context, 0, intentPrevious, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        }

        Intent intentPlay = new Intent(context, NotificationActionReceiver.class);
        intentPlay.setAction(ACTION_PLAY);
        PendingIntent pendingIntentPlay = PendingIntent.getBroadcast(context, 0, intentPlay, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        PendingIntent pendingIntentNext;
        int drw_next = android.R.drawable.ic_media_next;
        if (position == size) {
            pendingIntentNext = null;
        } else {
            Intent intentPrevious = new Intent(context, NotificationActionReceiver.class)
                    .setAction(ACTION_NEXT);
            pendingIntentNext = PendingIntent.getBroadcast(context, 0, intentPrevious, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        }

        /*

        Bitmap icon = BitmapFactory.decodeResource(context.getResources(),R.drawable.logo);
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(selectedAudio.getPath());
        byte[] data = mediaMetadataRetriever.getEmbeddedPicture();
        if(data != null){
            icon = BitmapFactory.decodeByteArray(data,0, data.length);
        }

        Aşağıdaki koda .setlargeicon ekleyip bunu gerçekleştirebilirsin ama hata veriyor diye şuanlık kaldırdım.
         */

        Bitmap icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.logo);
        notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle("Moody")
                .setContentText(selectedAudio.getName())
                .setLargeIcon(icon)
                .setOnlyAlertOnce(true)
                .addAction(drw_previous, "Previous", pendingIntentPrev)
                .addAction(playbutton, "Play", pendingIntentPlay)
                .addAction(drw_next, "Next", pendingIntentNext)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSessionCompat.getSessionToken())
                        .setShowActionsInCompactView(0, 1, 2)
                )
                .setShowWhen(false)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
        notificationManagerCompat.notify(1, notification);

    }

}
