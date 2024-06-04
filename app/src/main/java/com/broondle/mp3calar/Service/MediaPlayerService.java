package com.broondle.mp3calar.Service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.broondle.mp3calar.Model.AudioModel;
import com.broondle.mp3calar.R;
import com.broondle.mp3calar.Util.Managers.MediaNotificationManager;
import com.broondle.mp3calar.Util.Managers.MediaStoreManager;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.OptionalInt;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.IntStream;

public class MediaPlayerService extends Service {

    public static final MediaPlayer mediaPlayer = new MediaPlayer();

    private final IBinder binder = new MediaPlayerBinder();
    private List<AudioModel> audioModels;
    private AudioModel selectedAudioModel;

    private NotificationManager notificationManager;
    private int selectedIndex = 0, resumeButtonDrwNot, pauseButtonDrwNot, finishCounterSec = 0;




    /*
    Mediaplayer ve selectedaudio alınabilir. Ancak mediaplayer'ı activity veya benzeri bir contextteyken komut verme. Tüm action'lar servis üzerinden olacak(Broadcast ile).
     */

//for notification
    BroadcastReceiver broadcastReceiverMedia = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getExtras().getString("actionnameNot");
            switch (action){
                case MediaNotificationManager.ACTION_PREVIOUS:
                    on_media_state_changed(MediaAction.PREVIOUS);
                    Log.e("Previous",":Previous!!1");
                    break;
                case MediaNotificationManager.ACTION_PLAY:
                    on_media_state_changed(MediaAction.RESUMEORPAUSE);
                    break;
                case MediaNotificationManager.ACTION_NEXT:
                    on_media_state_changed(MediaAction.NEXT);
                    Log.e("Next",":next!!1");
                    break;
            }
        }
    };





    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("MediaPlayerService","Started!");

        refreshAudioList();

        resumeButtonDrwNot = android.R.drawable.ic_media_play;
        pauseButtonDrwNot = android.R.drawable.ic_media_pause;

        notificationManager = getSystemService(NotificationManager.class);

        registerReceiver(broadcastReceiverMedia,new IntentFilter("TRACKS_TRACKS"));

        //SETUP NOTIFICATION
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(MediaNotificationManager.CHANNEL_ID,"Moody",
                    NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        Log.e("MediaPlayerService","Done!");
        return START_STICKY;
    }

    private File checkThumb;
    public void playSong(Integer selectedIndex,String selectedName){

        try {

            if(mediaPlayer.isPlaying()){
                mediaPlayer.stop();
            }
            mediaPlayer.reset();

            if(selectedIndex != null){
                this.selectedIndex = selectedIndex;
            }
            else{
                // selectedName kullanılarak doğru AudioModel'i bulun
                OptionalInt indexOpt = IntStream.range(0, audioModels.size())
                        .filter(i -> audioModels.get(i).getName().equals(selectedName))
                        .findFirst();

                if (indexOpt.isPresent()) {
                    // İsim eşleşmesi bulunursa, bulunan indexi kullan
                    this.selectedIndex = indexOpt.getAsInt();
                } else {
                    // Eşleşme bulunamazsa, bir hata göster ve fonksiyondan çık
                    Toast.makeText(this, "Song with provided name not found!", Toast.LENGTH_SHORT).show();
                    throw new Exception();
                }
            }

            selectedAudioModel = audioModels.get(this.selectedIndex);


            mediaPlayer.setDataSource(selectedAudioModel.getPath());
            mediaPlayer.prepareAsync();

            checkThumb = new File(getFilesDir() +"/"+selectedAudioModel.getName()+".png");

            mediaPlayer.setOnPreparedListener(mediaPlayer -> {
                Log.e("Mediaplayer ","Prepared!");
                mediaPlayer.start();
                MediaNotificationManager.createNotification(this,selectedAudioModel,this.selectedIndex, pauseButtonDrwNot, audioModels.size()-1);

            });

            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                    Toast.makeText(getApplicationContext(), "Error while playing song!", Toast.LENGTH_SHORT).show();
                    return false;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error while playing song!", Toast.LENGTH_SHORT).show();
        }
    }

    public WeakReference<MediaPlayer> get_wkMediaPlayer(){ return new WeakReference<>(mediaPlayer); }

    public AudioModel get_selectedmodel(){
        return selectedAudioModel;
    }

    public void get_set_thumb(ImageView thumbImage){
        if(checkThumb.exists())
            thumbImage.setImageBitmap(BitmapFactory.decodeFile(checkThumb.getAbsolutePath()));
        else
            thumbImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.audiodefaulticon));
    }

    private void refreshAudioList(){
        MediaStoreManager.shared().getAudioListLocal(this, new Consumer<List<AudioModel>>() {
            @Override
            public void accept(List<AudioModel> audioModelList) {
                audioModels = audioModelList;
            }
        });
    }

    public void refresh_after_download(){
        MediaStoreManager.shared().getAudioListLocal(this, new Consumer<List<AudioModel>>() {
            @Override
            public void accept(List<AudioModel> audioModelList) {
                int previousIndex = selectedIndex;
                audioModels = audioModelList;

                if(audioModels.size()>0){
                    String previous_index_name = audioModelList.get(previousIndex).getName();
                    OptionalInt indexOpt = IntStream.range(0, audioModels.size())
                            .filter(i -> audioModels.get(i).getName().equals(previous_index_name))
                            .findFirst();
                    selectedIndex = indexOpt.getAsInt();
                }else{
                    selectedIndex = 0;
                }


            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaPlayer.stop();

        cancelNotifications();

        unregisterReceiver(broadcastReceiverMedia);

        Toast.makeText(this, "Some error occured!", Toast.LENGTH_SHORT).show();
    }

    public void cancelNotifications(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            notificationManager.cancelAll();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public enum MediaAction {
        RESUMEORPAUSE,
        JUSTSTOP,
        PREVIOUS,
        NEXT,
        FINISH
    }

    public void on_media_state_changed(MediaAction mediaAction){
        switch (mediaAction){
            case RESUMEORPAUSE:
                Log.e("ServisMedia","ResumeOrPause called!");
                if(mediaPlayer.isPlaying()){
                    mediaPlayer.pause();
                    MediaNotificationManager.createNotification(this,selectedAudioModel,selectedIndex, resumeButtonDrwNot, audioModels.size()-1);
                }else if(!mediaPlayer.isPlaying()){
                    mediaPlayer.start();
                    MediaNotificationManager.createNotification(this,selectedAudioModel,selectedIndex, pauseButtonDrwNot, audioModels.size()-1);
                }
                break;

            case JUSTSTOP:
                Log.e("ServisMedia","JustStop called!");
                if(mediaPlayer.isPlaying())
                    mediaPlayer.stop();
                cancelNotifications();

                break;
            case PREVIOUS:
                Log.e("ServisMedia","Previous called!");
                if(selectedIndex > 0){
                    playSong(selectedIndex-1,null);
                    MediaNotificationManager.createNotification(this,selectedAudioModel,selectedIndex, pauseButtonDrwNot, audioModels.size()-1);
                }
                else{
                    Toast.makeText(this, "Beginning of playlist!", Toast.LENGTH_SHORT).show();

                }

                break;
            case NEXT:
                Log.e("ServisMedia","NEXT called!");
                if(audioModels.size() > selectedIndex+1) {
                    playSong(selectedIndex + 1,null);
                    MediaNotificationManager.createNotification(this, selectedAudioModel, selectedIndex, pauseButtonDrwNot, audioModels.size() - 1);
                }
                else{
                    Toast.makeText(this, "End of playlist!", Toast.LENGTH_SHORT).show();
                }
                break;
            case FINISH:
                Log.e("ServisMedia","Finish called!");
                if(finishCounterSec == 0){

                    new CountDownTimer(10000,10000){
                        @Override
                        public void onTick(long l) {
                            on_media_state_changed(MediaAction.NEXT);
                            finishCounterSec = 1;
                        }

                        @Override
                        public void onFinish() {
                            finishCounterSec = 0;
                        }
                    }.start();

                }
                break;
        }
    }


    public class MediaPlayerBinder extends Binder {
        public MediaPlayerService getService() {
            return MediaPlayerService.this;
        }
    }

}
