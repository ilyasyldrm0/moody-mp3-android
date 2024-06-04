package com.broondle.mp3calar.Util.Managers;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.broondle.mp3calar.BottomMainActivity;
import com.broondle.mp3calar.BuildConfig;
import com.broondle.mp3calar.Constants;
import com.broondle.mp3calar.R;
import com.broondle.mp3calar.Service.MediaPlayerService;
import com.broondle.mp3calar.ui.SettingsFragment;
import com.unity3d.ads.IUnityAdsInitializationListener;
import com.unity3d.ads.IUnityAdsLoadListener;
import com.unity3d.ads.IUnityAdsShowListener;
import com.unity3d.ads.UnityAds;

import java.lang.ref.WeakReference;

public class AdManager {

    private static AdManager instance;

    public static synchronized AdManager shared(){
        if(instance == null)
            instance = new AdManager();

        return instance;
    }


    private AdManager(){}


    private WeakReference<Activity> context;
    private AlertDialog alertDialog;
    private AlertDialog progressDialog;

    public synchronized void checkAds(Activity context,Runnable canDownload){
        this.context = new WeakReference<>(context);
        Log.e("UnityAds","Admanager Check!");
        if(ads_must_beshowed(context.getApplicationContext())){
            Log.e("UnityAds","Alert Dialog asked!");

            alertDialog = AlertManager.shared().createAlertForAds(context,
                    () ->{//Show_Ads
                        initUnity();

                        progressDialog = AlertManager.shared().createAlertProgress(context);
                        alertDialog.dismiss();
                        progressDialog.show();

                        Toast.makeText(context, "Ads loading...", Toast.LENGTH_SHORT).show();
                    },
                    () -> {//Buy_Sub
                        alertDialog.dismiss();
                        SettingsFragment myFragment = new SettingsFragment();
                        FragmentManager fragmentManager = ((BottomMainActivity)context).getSupportFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.container, myFragment);
                        fragmentTransaction.addToBackStack(null);
                        fragmentTransaction.commit();

                    });
            alertDialog.show();
        }else{
            canDownload.run();
        }

    }

    private void initUnity(){
        if(!UnityAds.isInitialized()){
            Log.e("UnityAds ", "is not initialized. / Trying to init.");
            UnityAds.initialize(context.get(), Constants.unityGameID, BuildConfig.DEBUG, new IUnityAdsInitializationListener() {
                @Override
                public void onInitializationComplete() {
                    Log.e("UnityAds ","init is successfully!");
                    showUnityAds();
                }

                @Override
                public void onInitializationFailed(UnityAds.UnityAdsInitializationError unityAdsInitializationError, String s) {
                    Log.e("UnityAds ", "init failed again.");
                    alertDialog.dismiss();
                    Toast.makeText(context.get(), "Unexpected error, cant load ads!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void showUnityAds(){
        UnityAds.load(Constants.uInterstitialUnitID, new IUnityAdsLoadListener() {
            @Override
            public void onUnityAdsAdLoaded(String s) {
                UnityAds.show(context.get(), Constants.uInterstitialUnitID, new IUnityAdsShowListener() {
                    @Override
                    public void onUnityAdsShowFailure(String s, UnityAds.UnityAdsShowError unityAdsShowError, String s1) {
                        alertDialog.dismiss();
                    }

                    @Override
                    public void onUnityAdsShowStart(String s) {
                        Log.e("Unity Ads ","Intersititial ad showed successfully!");
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onUnityAdsShowClick(String s) {

                    }

                    @Override
                    public void onUnityAdsShowComplete(String s, UnityAds.UnityAdsShowCompletionState unityAdsShowCompletionState) {
                        //resetle
                        resetAdShownPref();
                        Log.e("test","Unity reklam gösterimi tamamen tamamlandı !");

                    }
                });

            }

            @Override
            public void onUnityAdsFailedToLoad(String s, UnityAds.UnityAdsLoadError unityAdsLoadError, String s1) {
                Log.e("Unity Ads ","Intersititial ad failed to load!");
                alertDialog.dismiss();
            }
        });
    }

    private void resetAdShownPref(){
        PrefManager.shared().setLongPref(context.get(),Constants.LOCAL_ADS_SHOWN_PREF,System.currentTimeMillis());
    }


    public boolean ads_must_beshowed(Context context){
        MediaPlayerManager mpManager = MediaPlayerManager.shared(context);

        if(!TextUtils.isEmpty(PrefManager.shared().getStringPref(context,Constants.noAdsCheckUserPref))){
            //aboneyse
            if(PrefManager.shared().getStringPref(context,Constants.noAdsCheckUserPref).equals("true")){
                return false;
            }
        }

        long sonGosterimSure = PrefManager.shared().getLongPref(context,Constants.LOCAL_ADS_SHOWN_PREF)/1000;
        long nowSeconds = System.currentTimeMillis()/1000;

        if(sonGosterimSure > 0){
            //süre var kontrol et
            long gecenSure = nowSeconds-sonGosterimSure;

            int reklamSureLimit = Constants.defaultReklamGecisSure;
            if (PrefManager.shared().getIntPref(context,Constants.ADS_LIMIT_PREF) != -1){
                reklamSureLimit = PrefManager.shared().getIntPref(context,Constants.ADS_LIMIT_PREF);
            }

            if(gecenSure >= reklamSureLimit){
                //vakit doldu gösterim yap
                if(mpManager.isServiceRunning()){
                    if(mpManager.getService().get_wkMediaPlayer().get().isPlaying()){
                        mpManager.getService().on_media_state_changed(MediaPlayerService.MediaAction.RESUMEORPAUSE);
                    }
                }

                return !BuildConfig.DEBUG;
            }else{
                return false;
            }

        }else{
            //süre yok reklam göster
            if(mpManager.isServiceRunning()){
                if(mpManager.getService().get_wkMediaPlayer().get().isPlaying()){
                    mpManager.getService().on_media_state_changed(MediaPlayerService.MediaAction.RESUMEORPAUSE);
                }
            }


            return !BuildConfig.DEBUG;
        }
    }

}
