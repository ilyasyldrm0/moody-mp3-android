package com.broondle.mp3calar.Util.Managers;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.text.TextUtils;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.broondle.mp3calar.Constants;
import com.broondle.mp3calar.Service.DownloadService;
import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;

import java.util.List;

public class GenelManager {

    private static GenelManager instance;

    public static synchronized GenelManager shared(){
        if(instance == null)
            instance = new GenelManager();

        return instance;
    }

    private GenelManager(){}

    public double percentage(double screenSize, Double percent){
        return (screenSize/100)*percent;
    }

    public boolean isPermGranted(Context context,String perm){
        return (ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED);
    }


    public void firebaseAppcheckInit(Context context){
        FirebaseApp.initializeApp(context);
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(PlayIntegrityAppCheckProviderFactory.getInstance());
    }

    public void bakimvarDialog(Context contextt){
        AlertDialog.Builder builder = new AlertDialog.Builder(contextt);
        builder.setTitle("Maintenance - Bakım");
        builder.setMessage("ENG: 'We are in maintenance, we are trying to solve some problems. Thank for understanding...'\n\nTR: 'Size daha iyi hizmet sunabilmemiz için çalışıyoruz. Anlayışınız için teşekkür ederiz..'");
        builder.setCancelable(false);
        builder.setPositiveButton("Okey", (dialog, which) -> System.exit(0));
        builder.show();
    }

    public void playstoreUpdate(Context contextt,String versionName,String urlForUpdate){
        AlertDialog.Builder builder = new AlertDialog.Builder(contextt);
        builder.setTitle("Güncelleme Gerekli");
        builder.setMessage("ENG: 'We have update for you! You might want to update cause we are trying to give you better experience.'\n\nTR: 'Size daha iyi hizmet sunabilmemiz için uygulamanızı güncel tutmanız gerekiyor!'\n\nNew Version: "+versionName);
        builder.setCancelable(false);
        builder.setPositiveButton("Güncelle", (dialog, which) -> contextt.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(urlForUpdate))));
        builder.setNegativeButton("Şimdi Değil", (dialogInterface, i) -> System.exit(0));
        builder.show();
    }


    public Boolean isUserPaid(Context context){
        if (!TextUtils.isEmpty(PrefManager.shared().getStringPref(context, Constants.noAdsCheckUserPref))){
            String value = PrefManager.shared().getStringPref(context,Constants.noAdsCheckUserPref);
            // kullanıcı abone değişiklikleri yap
            return value.equals("true");
        }else{
            PrefManager.shared().setStringPref(context,Constants.noAdsCheckUserPref,"false");
        }
        return false;
    }


    public boolean isMyServiceRunning(Context context, Class<?> serviceClass){

            ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceClass.getName().equals(service.service.getClassName())) {
                    return true;
                }
            }
            return false;


    }

    public boolean isDownloadServiceRunning(Context context){
        return isMyServiceRunning(context, DownloadService.class);
    }

    public boolean isAppOnForeground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            return false;
        }
        final String packageName = context.getPackageName();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }

}
