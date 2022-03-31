package com.plivo.plivosimplequickstart;

import android.content.Context;
import android.content.SharedPreferences;

public class Pref {
    private static Pref pref;
    private SharedPreferences sharedPref;

    Pref(Context context) {
        sharedPref = context.getSharedPreferences(Utils.PREFERENCE_KEY, Context.MODE_PRIVATE);
    }

    public static Pref newInstance(Context context) {
        if (pref == null) {
            pref = new Pref(context);
        }
        return pref;
    }


    public void setBoolean(String key, boolean value) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public boolean getBoolean(String key) {
        return sharedPref.getBoolean(key, false);
    }

    public void setString(String key, String value) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public String getString(String key) {
        return sharedPref.getString(key, "");
    }


    public void clear() {
        setBoolean(Constants.LOG_IN,false);
        sharedPref.edit().clear().apply();
    }
}
