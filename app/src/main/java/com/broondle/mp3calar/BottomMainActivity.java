package com.broondle.mp3calar;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.broondle.mp3calar.Application.Abstracts.ActivityAbstract;
import com.broondle.mp3calar.Model.AudioModel;
import com.broondle.mp3calar.Service.MediaPlayerService;
import com.broondle.mp3calar.Util.Managers.GenelManager;
import com.broondle.mp3calar.Util.Managers.MediaPlayerManager;
import com.broondle.mp3calar.Util.Managers.MediaStoreManager;
import com.broondle.mp3calar.Util.Managers.PrefManager;
import com.broondle.mp3calar.Util.Managers.ViewManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.broondle.mp3calar.databinding.ActivityBottomMainBinding;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.revenuecat.purchases.CustomerInfo;
import com.revenuecat.purchases.EntitlementInfo;
import com.revenuecat.purchases.Purchases;
import com.revenuecat.purchases.PurchasesConfiguration;
import com.revenuecat.purchases.PurchasesError;
import com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback;

import io.sentry.android.core.SentryAndroid;

public class BottomMainActivity extends ActivityAbstract {

    private static final int WRITE_READ_PERM_CODE = 1513,NOTIFICATION_CODE = 2566, MEDIA_CODE=5163;

    boolean isUserPaid = false, isStopCalled = false;

    String packageName;
    PowerManager pm;

    GenelManager genelUtil;

    View bottomMediaView;
    ImageView bMedia_image, bMedia_resume_button_image;
    TextView  bMedia_song_title_text, bMedia_song_album_text;

    MediaPlayerManager mediaPlayerManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        genelUtil = GenelManager.shared();

        packageName = getPackageName();
        pm = (PowerManager) getSystemService(POWER_SERVICE);

        ActivityBottomMainBinding binding = ActivityBottomMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        genelUtil.firebaseAppcheckInit(this);

        setupRevenuaCat();

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_bottom_main);
        NavigationUI.setupWithNavController(binding.navView, navController);
        navView.setItemIconTintList(null);
        setRemoteConfig();

        askPerm();

        MediaStoreManager.shared().scanMediaStore(this,null);

        if(!BuildConfig.DEBUG)
            SentryAndroid.init(this, options -> {
                options.setDsn(BuildConfig.SENTRY_DNS_KEY);
                options.setDebug(BuildConfig.DEBUG);
                options.setSampleRate(0.5);
            });

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w("TAG", "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();

                        Log.w("Token new",token);
                    }
                });


        mediaPlayerManager = MediaPlayerManager.shared(getApplicationContext());
        //Bottom Media
        bottomMediaView = findViewById(R.id.bottomMediaView);
        bottomMediaView.setVisibility(View.GONE);
        bMedia_image = findViewById(R.id.bMedia_image);
        bMedia_song_title_text = findViewById(R.id.bMedia_title);
        bMedia_song_album_text = findViewById(R.id.bMedia_album);
        bMedia_resume_button_image = findViewById(R.id.startstopImageView);

        //bottomMediaView.setVisibility(View.GONE);
        bottomMediaView.setOnTouchListener(new View.OnTouchListener() {
            private float initialX;
            private float initialTouchX;

            private boolean isClick;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                int threshold = 35;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:

                        initialX = v.getTranslationX();
                        initialTouchX = event.getRawX();
                        isClick = true;
                        return true;


                    case MotionEvent.ACTION_MOVE:


                        float deltaX = event.getRawX() - initialTouchX;
                        if (event.getRawX() > initialTouchX){
                            v.setTranslationX(initialX + deltaX);
                        }

                        if(Math.abs(deltaX) > 12){
                            isClick = false;
                        }

                        return true;
                    case MotionEvent.ACTION_UP:
                        Log.e("actiontest","up");
                        if(isClick){
                            if(ViewManager.shared().isTouchInsideView(bMedia_resume_button_image,event)){
                                bMedia_resume_button_image.performClick();

                            }else{
                                Intent intent = new Intent(BottomMainActivity.this,MediaPlayerActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        }else if (getPercentageOfScreen(v) >= threshold) {
                            if(mediaPlayerManager.getService().get_wkMediaPlayer().get().isPlaying()){
                                mediaPlayerManager.getService().on_media_state_changed(MediaPlayerService.MediaAction.JUSTSTOP);
                                isStopCalled = true;
                            }

                            v.animate()
                                    .translationX(v.getWidth())
                                    .alpha(0.0f)
                                    .setDuration(300)
                                    .setListener(new AnimatorListenerAdapter() {
                                        @Override
                                        public void onAnimationEnd(Animator animation) {
                                            super.onAnimationEnd(animation);
                                            //KAYDIRARAK KAPATILIRSA
                                            v.setVisibility(View.GONE);
                                            v.animate().translationX(0).start();
                                        }
                                    })
                                    .start();
                        } else {
                            v.animate().translationX(0).alpha(1.0f).start();
                        }
                        break;
                }
                return false;
            }

            private float getPercentageOfScreen(View v) {
                return Math.abs(v.getTranslationX()) / ((float) v.getResources().getDisplayMetrics().widthPixels) * 100;
            }
        });
        bottomMediaView.setClipToOutline(true);

        bottom_player_handler();

    }


    private void setupRevenuaCat(){
        try{

            Purchases.setDebugLogsEnabled(true);
            PurchasesConfiguration conf = new PurchasesConfiguration.Builder(this,"goog_ubCHjwXmNFtTBKKFZpTpsJSQQBP").build();
            Purchases.configure(conf);

            Purchases.getSharedInstance().getCustomerInfo(new ReceiveCustomerInfoCallback() {
                @Override
                public void onReceived(@NonNull CustomerInfo customerInfo) {
                    EntitlementInfo sku = customerInfo.getEntitlements().get("noadsstandart");
                    if (sku != null)
                        if(sku.isActive()){
                            //start new activity for sub
                            Log.e("Subscription "," isActive!");
                            if(TextUtils.isEmpty(PrefManager.shared().getStringPref(getApplicationContext(),Constants.noAdsCheckUserPref)) ||
                                    TextUtils.equals(PrefManager.shared().getStringPref(getApplicationContext(),Constants.noAdsCheckUserPref),"false")){
                                Toast.makeText(BottomMainActivity.this, "Subcription detected, RESTART APP for features!", Toast.LENGTH_SHORT).show();
                                PrefManager.shared().setStringPref(getApplicationContext(),Constants.noAdsCheckUserPref,"true");
                            }
                        }else{
                            Log.e("RevenuaCat ","There is not active sub.");
                            PrefManager.shared().setStringPref(getApplicationContext(),Constants.noAdsCheckUserPref,"false");
                        }
                }

                @Override
                public void onError(@NonNull PurchasesError purchasesError) {
                    Toast.makeText(getApplicationContext(), "Cant get subscription info!", Toast.LENGTH_SHORT).show();
                    Log.e("RevenuaCat error ",purchasesError.getMessage());
                }
            });

            isUserPaid = genelUtil.isUserPaid(getApplicationContext());

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Cant get subscription info!", Toast.LENGTH_SHORT).show();
            Log.e("RevenuaCat error ",e.getMessage());
        }
    }

    PackageInfo pInfo;

    private void setRemoteConfig(){

        try {
            pInfo = getApplicationContext().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        //firebase remote config
        FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(1)
                .build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        mFirebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_values);
        mFirebaseRemoteConfig.fetchAndActivate()
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()){
                        boolean updated = task.getResult();
                        Log.d("Remote Config: ","Config params updated: "+ updated);
                        int onlineVersion = Integer.parseInt(mFirebaseRemoteConfig.getString("force_current_version"));
                        boolean updateRequired = mFirebaseRemoteConfig.getBoolean("force_update_required");
                        String onlineVersionName = mFirebaseRemoteConfig.getString("force_gereken_isim");
                        boolean bakimVarmi = mFirebaseRemoteConfig.getBoolean("force_bakim_ekrani");
                        String urlForUpdate = mFirebaseRemoteConfig.getString("force_update_url");
                        int ads_limit_seconds = (int) mFirebaseRemoteConfig.getLong("ads_show_limit_seconds");

                        PrefManager.shared().setIntPref(this,Constants.ADS_LIMIT_PREF,ads_limit_seconds);
                        if(bakimVarmi){
                            genelUtil.bakimvarDialog(BottomMainActivity.this);
                        }else{
                            if(updateRequired && onlineVersion > pInfo.versionCode){
                                genelUtil.playstoreUpdate(BottomMainActivity.this,onlineVersionName,urlForUpdate);
                            }else if(onlineVersion > pInfo.versionCode){
                                Toast.makeText(BottomMainActivity.this, "New update available.", Toast.LENGTH_SHORT).show();
                            }
                        }


                    }
                });

    }

    //permission methods
    private boolean isPerm(int number){

        switch (number){
            case 0:
                return pm.isIgnoringBatteryOptimizations(packageName);

            case 1:

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    return true;

                return GenelManager.shared().isPermGranted(this,Manifest.permission.READ_EXTERNAL_STORAGE) && GenelManager.shared().isPermGranted(this,Manifest.permission.WRITE_EXTERNAL_STORAGE);

            case 2:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    return GenelManager.shared().isPermGranted(this,Manifest.permission.POST_NOTIFICATIONS);
                }

            case 3:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    return GenelManager.shared().isPermGranted(this,Manifest.permission.READ_MEDIA_AUDIO);
                }

            default:
                return true;
        }

    }

    @SuppressLint("BatteryLife")
    private void askPerm(){

        if(!isPerm(0)){
            Intent batteryIntent = new Intent();
            batteryIntent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            batteryIntent.setData(Uri.parse("package:" + packageName));
            activityResultLauncherOptimization.launch(batteryIntent);
            return;
        }

        if(!isPerm(1)){
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE}, WRITE_READ_PERM_CODE);
            return;
        }

        if(!isPerm(2) && Build.VERSION.SDK_INT >= 33){
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_CODE);
            return;
        }

        if(!isPerm(3) && Build.VERSION.SDK_INT >= 33){
            requestPermissions(new String[]{Manifest.permission.READ_MEDIA_AUDIO}, MEDIA_CODE);
        }

    }


    String warning_perm = "Your refusal to allow may prevent you from using some features.";


    //-onresult of requests
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == WRITE_READ_PERM_CODE){
            if(isPerm(1)){
                askPerm();
            }else{
                Toast.makeText(BottomMainActivity.this, warning_perm, Toast.LENGTH_SHORT).show();
                //System.exit(0);
            }
        }

        if(NOTIFICATION_CODE == requestCode){
            if(isPerm(2)){
                askPerm();
            }else{
                Toast.makeText(this, warning_perm, Toast.LENGTH_SHORT).show();
                //System.exit(0);
            }
        }
    }
    private final ActivityResultLauncher<Intent> activityResultLauncherOptimization = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    Log.d("Batter Optimization", "onActivityResult: ");
                    Log.d("Result Code ","*"+result.getResultCode());

                    if (isPerm(0)) {
                        askPerm();
                    }else{
                        Toast.makeText(BottomMainActivity.this, warning_perm, Toast.LENGTH_SHORT).show();
                        //System.exit(0);
                    }

                }
            }
    );




    //BOTTOM PLAYER VİEW CONTROLLERS
    AudioModel selectedAudioModel;
    boolean isResumeDrawable = false;
    Handler progressHandler = new Handler();

    private void bottom_player_handler(){

        int resumeButtonDrw = android.R.drawable.ic_media_play,
        pauseButtonDrw = android.R.drawable.ic_media_pause;

        bMedia_resume_button_image.setOnClickListener(view -> {
            if(mediaPlayerManager.isServiceRunning()){
                if(isResumeDrawable){
                    bMedia_resume_button_image.setImageDrawable(ContextCompat.getDrawable(this,pauseButtonDrw));
                }else{
                    bMedia_resume_button_image.setImageDrawable(ContextCompat.getDrawable(this,resumeButtonDrw));
                }
                mediaPlayerManager.getService().on_media_state_changed(MediaPlayerService.MediaAction.RESUMEORPAUSE);
            }
        });

        Runnable runnableProgress = new Runnable() {
            @Override
            public void run() {
                try {
                    if(mediaPlayerManager.getService().get_wkMediaPlayer().get().isPlaying()){
                        if(bottomMediaView.getVisibility() == View.VISIBLE){

                            selectedAudioModel = MediaPlayerManager.shared(getApplicationContext()).getService().get_selectedmodel();
                            if(!selectedAudioModel.getName().equals(bMedia_song_title_text.getText().toString()))
                                bMedia_song_title_text.setText(selectedAudioModel.getName());

                            if(isResumeDrawable){
                                bMedia_resume_button_image.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), pauseButtonDrw));
                                isResumeDrawable = false;
                            }

                        }else{
                            //servis açık ama view yok getir
                            isStopCalled = false;
                            bottomMediaView.setTranslationY(bottomMediaView.getRootView().getHeight());
                            bottomMediaView.setAlpha(0f);

                            // View'ı görünür yap
                            bottomMediaView.setVisibility(View.VISIBLE);

                            // Animasyonu başlat
                            bottomMediaView.animate()
                                    .translationY(0)
                                    .alpha(1f)
                                    .setDuration(400)
                                    .setListener(new AnimatorListenerAdapter() {
                                        @Override
                                        public void onAnimationEnd(Animator animation) {
                                            super.onAnimationEnd(animation);
                                        }
                                    });

                        }

                    }else{
                        if(!isResumeDrawable){
                            bMedia_resume_button_image.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), resumeButtonDrw));
                            isResumeDrawable = true;
                        }

                        if(bottomMediaView.getVisibility() == View.VISIBLE && isStopCalled){
                            isStopCalled = false;
                            bottomMediaView.animate()
                                    .translationX(bottomMediaView.getWidth())
                                    .alpha(0.0f)
                                    .setDuration(300)
                                    .setListener(new AnimatorListenerAdapter() {
                                        @Override
                                        public void onAnimationEnd(Animator animation) {
                                            super.onAnimationEnd(animation);
                                            //KAYDIRARAK KAPATILIRSA
                                            bottomMediaView.setVisibility(View.GONE);
                                            bottomMediaView.animate().translationX(0).start();
                                        }
                                    })
                                    .start();
                        }
                    }
                } catch (Exception ignored) {}

                progressHandler.postDelayed(this,1000);
            }
        };
        progressHandler.post(runnableProgress);
    }

}

