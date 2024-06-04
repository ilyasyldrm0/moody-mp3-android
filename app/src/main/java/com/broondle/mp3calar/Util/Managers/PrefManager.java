package com.broondle.mp3calar.Util.Managers;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefManager {
    private static PrefManager instance;

    public static synchronized PrefManager shared(){
        if(instance == null)
            instance = new PrefManager();
        return instance;
    }

    private PrefManager(){}

    private SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(context.getPackageName() + "_preferences",Context.MODE_PRIVATE);
    }

    public void setStringPref(Context context,String key,String deger){

        getPrefs(context).edit().putString(key,deger).apply();

    }

    public String getStringPref(Context context,String key){

        return getPrefs(context).getString(key,"");

    }

    public void setIntPref(Context context,String key,int deger){

        getPrefs(context).edit().putInt(key,deger).apply();

    }

    public int getIntPref(Context context,String key){

        return getPrefs(context).getInt(key, -1);

    }

    public void setLongPref(Context context,String key,Long deger){

        getPrefs(context).edit().putLong(key,deger).apply();

    }

    public Long getLongPref(Context context,String key){

        return getPrefs(context).getLong(key, -1);

    }

    public void setBoolPref(Context context,String key,boolean deger){

        getPrefs(context).edit().putBoolean(key,deger).apply();

    }

    public boolean getBoolPref(Context context, String key){

        return getPrefs(context).getBoolean(key, false);

    }

}
