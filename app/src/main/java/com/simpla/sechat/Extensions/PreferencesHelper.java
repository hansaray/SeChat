package com.simpla.sechat.Extensions;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import com.simpla.sechat.R;

public class PreferencesHelper {

    public static boolean setTheme(Context mContext){
        SharedPreferences settings = mContext.getApplicationContext().getSharedPreferences("SeChat_settings",0);
        boolean darkModeControl = settings.getBoolean("darkMode",false);
        if (!darkModeControl){
            mContext.setTheme(R.style.LightTheme);
        } else {
            mContext.setTheme(R.style.DarkTheme);
        }
        return darkModeControl;
    }

    public void newSave(SharedPreferences.Editor editor,Context mContext){
        int nightModeFlags = mContext.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        switch (nightModeFlags) {
            case Configuration.UI_MODE_NIGHT_YES:
                editor.putBoolean("darkMode",true);
                break;
            case Configuration.UI_MODE_NIGHT_NO:
            case Configuration.UI_MODE_NIGHT_UNDEFINED:
                editor.putBoolean("darkMode",false);
                break;
        }
        editor.apply();
    }

    public void existingSave(Context mContext){
        SharedPreferences settings = mContext.getApplicationContext().getSharedPreferences("SeChat_settings",0);
        SharedPreferences.Editor editor = settings.edit();
        boolean darkMode = settings.getBoolean("darkMode",false);
        if(darkMode){
            editor.putBoolean("darkMode",false);
        }else{
            editor.putBoolean("darkMode",true);
        }
        editor.putBoolean("loggedIn",true);
        editor.apply();
    }

}

