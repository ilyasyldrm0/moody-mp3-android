package com.broondle.mp3calar.Util.Managers;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.broondle.mp3calar.R;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.util.Arrays;

public class AlertManager {

    private static AlertManager instance;

    public static synchronized AlertManager shared(){
        if(instance == null)
            instance = new AlertManager();
        return instance;
    }

    private AlertManager(){}

    public AlertDialog createAlertProgress(Context context){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(View.inflate(context, R.layout.progressdialog,null));
        builder.setCancelable(false);

        return builder.create();
    }
    public AlertDialog createAlertForAds(Context context, Runnable showAdsFunc, Runnable buySubFunc){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = View.inflate(context, R.layout.alertdialogview,null);
        Button buttonShowAds = view.findViewById(R.id.buttonShowAds);
        Button buttonBuySub = view.findViewById(R.id.buttonBuySub);
        TextView metin = view.findViewById(R.id.message_ads);


        String metinText = metin.getText().toString();
        String[] split_for_price = metinText.split("-XX-");
        FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        int ads_limit_seconds = (int) mFirebaseRemoteConfig.getLong("ads_show_limit_seconds")/60;

        metinText = split_for_price[0]+String.valueOf(ads_limit_seconds)+split_for_price[1];

        metin.setText(metinText);


        buttonShowAds.setOnClickListener(button1 ->{
            showAdsFunc.run();
        });

        buttonBuySub.setOnClickListener(button2 -> {
            buySubFunc.run();
        });

        builder.setView(view);
        builder.setCancelable(true);

        return builder.create();
    }

    public AlertDialog createAlert(Context context, String title, String message, String ok, String negative, boolean cancelable,
                            Runnable okFunc, Runnable negativeFunc){
        AlertDialog.Builder builder = new AlertDialog.Builder(context.getApplicationContext());
        builder.setCancelable(cancelable);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(ok, (dialogInterface,i) -> { okFunc.run(); });

        if (negative != null){
            builder.setNegativeButton(negative, (dialogInterface, i) -> { negativeFunc.run(); });
        }

        return builder.create();
    }
}
