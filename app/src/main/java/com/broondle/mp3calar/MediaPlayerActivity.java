package com.broondle.mp3calar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.broondle.mp3calar.Service.MediaPlayerService;
import com.broondle.mp3calar.Model.AudioModel;
import com.broondle.mp3calar.Util.Managers.MediaPlayerManager;

import java.lang.ref.WeakReference;

public class MediaPlayerActivity extends AppCompatActivity {
    ImageView thumbImage;
    ImageView previousButton,startStopButton,nextButton;
    AudioModel selectedAudioModel;
    SeekBar seekBar;

    WeakReference<MediaPlayer> mediaPlayerWeakRef;
    int resumeButtonDrw, pauseButtonDrw,previousButtonDrw,nextButtonDrw;
    private boolean isResumeDrawable;


    TextView sayacStart,sayacFinish,sarkiAdText;

    Intent mediaPlayerIntent;
    MediaPlayerManager mediaPlayerManager;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_player);
        init();

    }

    private void init(){
        mediaPlayerIntent = new Intent(this,MediaPlayerService.class);

        mediaPlayerManager = MediaPlayerManager.shared(getApplicationContext());

        mediaPlayerWeakRef = mediaPlayerManager.getService().get_wkMediaPlayer();
        selectedAudioModel = mediaPlayerManager.getService().get_selectedmodel();

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O){
            resumeButtonDrw = android.R.drawable.ic_media_play;
            pauseButtonDrw = android.R.drawable.ic_media_pause;
            previousButtonDrw = android.R.drawable.ic_media_previous;
            nextButtonDrw = android.R.drawable.ic_media_next;
        }else{
            resumeButtonDrw = R.drawable.resume_button;
            pauseButtonDrw = R.drawable.pause_button;
            previousButtonDrw = R.drawable.previous_button;
            nextButtonDrw = R.drawable.next_button;
        }
        sarkiAdText = findViewById(R.id.songNameText);
        sarkiAdText.setSelected(true);
        sayacStart = findViewById(R.id.sayacStart);
        sayacFinish = findViewById(R.id.sayacFinish);
        seekBar = findViewById(R.id.seekBar);

        thumbImage = findViewById(R.id.thumbImage);

        previousButton = findViewById(R.id.previousButton);
        startStopButton = findViewById(R.id.startStopButton);
        nextButton = findViewById(R.id.nextButton);
        startStopButton.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), pauseButtonDrw));
        isResumeDrawable = false;
        previousButton.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),previousButtonDrw));
        nextButton.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),nextButtonDrw));
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progres, boolean fromUser) {

                if(fromUser){
                    mediaPlayerWeakRef.get().seekTo(progres);

                }
                // kalan süre
                String dakika = String.valueOf((mediaPlayerWeakRef.get().getDuration()-mediaPlayerWeakRef.get().getCurrentPosition())/1000/60);
                if(Integer.parseInt(dakika) < 10){
                    dakika = "0" + dakika;
                }

                int saniyeInt = ((mediaPlayerWeakRef.get().getDuration()-mediaPlayerWeakRef.get().getCurrentPosition())/1000)%60;
                String saniye = String.valueOf(saniyeInt);

                //bittimi kontrol
                if(((mediaPlayerWeakRef.get().getDuration()-mediaPlayerWeakRef.get().getCurrentPosition())/1000) == 0){
                    mediaPlayerManager.getService().on_media_state_changed(MediaPlayerService.MediaAction.FINISH);
                }

                if(Integer.parseInt(saniye) < 10){
                    saniye = "0"+saniye;
                }
                String dakikaveSaniye = dakika+"."+saniye;
                sayacFinish.setText(dakikaveSaniye);

                //ilerlemiş süre
                String dakika2 = String.valueOf((mediaPlayerWeakRef.get().getCurrentPosition()/1000)/60);
                if(Integer.parseInt(dakika2) < 10){
                    dakika2 = "0"+dakika2;
                }

                String saniye2 = String.valueOf((mediaPlayerWeakRef.get().getCurrentPosition()/1000)%60);
                if(Integer.parseInt(saniye2) < 10){
                    saniye2 = "0"+saniye2;
                }
                String dakikaveSaniye2 = dakika2+"."+saniye2;
                sayacStart.setText(dakikaveSaniye2);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        playerHandler();

        setButtonClickeds();


    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void setButtonClickeds(){

        startStopButton.setOnClickListener(view -> {
            mediaPlayerManager.getService().on_media_state_changed(MediaPlayerService.MediaAction.RESUMEORPAUSE);
        });
        nextButton.setOnClickListener(view -> {
            mediaPlayerManager.getService().on_media_state_changed(MediaPlayerService.MediaAction.NEXT);
            selectedAudioModel = mediaPlayerManager.getService().get_selectedmodel();
        });
        previousButton.setOnClickListener(view -> {
            mediaPlayerManager.getService().on_media_state_changed(MediaPlayerService.MediaAction.PREVIOUS);
            selectedAudioModel = mediaPlayerManager.getService().get_selectedmodel();
        });

    }


    private void playerHandler(){
        Handler progressHandler = new Handler();

        Runnable runnableProgress = new Runnable() {
            @Override
            public void run() {
                seekBar.setProgress(mediaPlayerWeakRef.get().getCurrentPosition());

                if(seekBar.getMax() != mediaPlayerWeakRef.get().getDuration())
                    seekBar.setMax(mediaPlayerWeakRef.get().getDuration());

                selectedAudioModel = mediaPlayerManager.getService().get_selectedmodel();
                if(!selectedAudioModel.getName().equals(sarkiAdText.getText().toString()))
                    sarkiAdText.setText(selectedAudioModel.getName());

                if(mediaPlayerWeakRef.get().isPlaying() && isResumeDrawable){
                    startStopButton.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), pauseButtonDrw));
                    isResumeDrawable = false;
                }


                if(!mediaPlayerWeakRef.get().isPlaying() && !isResumeDrawable){
                    startStopButton.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), resumeButtonDrw));
                    isResumeDrawable = true;
                }


                progressHandler.postDelayed(this,1000);
            }
        };
        progressHandler.post(runnableProgress);
    }
}