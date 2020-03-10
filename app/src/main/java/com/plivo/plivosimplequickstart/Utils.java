package com.plivo.plivosimplequickstart;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import com.plivo.endpoint.Incoming;

import java.util.HashMap;

public class Utils {
    // endpoint username & password
    static final String USERNAME = "username";
    static final String PASSWORD = "password";

    static final String HH_MM_SS = "%02d:%02d:%02d";
    static final String MM_SS = "%02d:%02d";

    private static SharedPreferences mSharedPreferences;
    private static Context context;
    private static PlivoBackEnd.BackendListener listener;
    private static Incoming incoming;
    private static boolean isLoggedIn = false;

    public static HashMap<String, Object> options = new HashMap<String, Object>()
    {{
        put("enableTracking",true);
    }};

    static String getDeviceToken() {
        context = (Context) options.get("sharedContext");
        mSharedPreferences = context.getSharedPreferences("plivo_refs", Context.MODE_PRIVATE);
        return mSharedPreferences.getString("token", "");
    }

    static void setDeviceToken(String newDeviceToken) {
        context = (Context) options.get("sharedContext");
        mSharedPreferences = context.getSharedPreferences("plivo_refs", Context.MODE_PRIVATE);
        mSharedPreferences.edit().putString("token", newDeviceToken).apply();
    }

    static PlivoBackEnd.BackendListener getBackendListener() {
        return (PlivoBackEnd.BackendListener) options.get("listener");
    }

    static void setBackendListener(PlivoBackEnd.BackendListener backendListener) {
        listener = backendListener;
        options.put("listener", listener);
    }

    static boolean getLoggedinStatus() {
        return isLoggedIn;
    }

    static void setLoggedinStatus(boolean status) {
        isLoggedIn = status;
    }

    static Incoming getIncoming() {
        return incoming;
    }

    static void setIncoming(Incoming data) {
        incoming = data;
    }

    static String from(String fromContact, String fromSip) {
        String from = TextUtils.isEmpty(fromContact)?
                TextUtils.isEmpty(fromSip)? "" : fromSip:
                fromContact;
        return from.contains("\"") ?
                from.substring(from.indexOf("\"")+1, from.lastIndexOf("\"")):
                from;

    }

    static String to(String toSip) {
        return TextUtils.isEmpty(toSip) ? "" :
                toSip.substring(toSip.indexOf(":")+1, toSip.indexOf("@"));
    }
}
