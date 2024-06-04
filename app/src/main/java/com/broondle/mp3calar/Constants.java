package com.broondle.mp3calar;

import android.content.Intent;
import android.os.Environment;
import android.provider.MediaStore;

public class Constants {


    public static String adPrefLoc = "checkForAdOpeningLong" , noAdsCheckUserPref = "noAdsActive";
    public static String RAPID_API_KEY = "0252421ca0mshb8ff5ba6f8e6ecbp11e693jsn690c13ab97ad";

    public static String unityGameID = "4895179",uInterstitialUnitID = "Interstitial_Android",
            Admob_AD_ID = "ca-app-pub-1954225139551009/1330304403"
            ,playPref = "playPrefs",audioModelsPref="audioModelsPref",downloadingListPref="downloadingListPref"

            ,ADS_LIMIT_PREF="unityAdsShowTimeLimitPref",LOCAL_ADS_SHOWN_PREF="localAdsShownPref";
    public static int defaultReklamGecisSure = 300000;

    public static String ACTION_DOWNLOAD = "action_download", DOWNLOADED_SIZE = "downloadsize",
    TOTAL_SIZE = "totalSize",YOUTUBE_DATA_BROADCAST = "youtubeDataModelSend"
            , MUSIC_FOLDER_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getAbsolutePath();

}
