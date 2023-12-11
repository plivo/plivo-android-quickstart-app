package com.plivo.plivosimplequickstart;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Vibrator;
import android.text.TextUtils;

import com.plivo.endpoint.CallAndMediaMetrics;
import com.plivo.endpoint.Incoming;

import java.util.HashMap;
import java.util.List;

public class Utils {
    /*
     You can define Username & password inside local.properties like below
     plivo.username="Your endpoint username"
     plivo.password="Your endpoint password"
    */
    // endpoint username & password
    static final String USERNAME = BuildConfig.Username;
    static final String PASSWORD = BuildConfig.Password;
    static final String AUTH_ID = BuildConfig.AuthID;
    static final String AUTH_TOKEN = BuildConfig.AuthToken;

    static final String PREFERENCE_KEY = "Plivo-Pref";

    static final String HH_MM_SS = "%02d:%02d:%02d";
    static final String MM_SS = "%02d:%02d";

    private static SharedPreferences mSharedPreferences;
    private static Context context;
    private static PlivoBackEnd.BackendListener listener;
    private static Incoming incoming;
    private static boolean isLoggedIn = false;
    private static boolean isLoggedInTokenGen = false;

    private static Vibrator vibrator;

    public static HashMap<String, Object> options = new HashMap<String, Object>() {{
//        put("enableTracking", Tracking.NONE);
        put("enableQualityTracking", CallAndMediaMetrics.ALL);
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

    static boolean isLoggedInWithTokenGenerator() {
        return isLoggedInTokenGen;
    }

    static void setLoginWithTokenGenerator(boolean status) {
        isLoggedInTokenGen = status;
    }

    static Incoming getIncoming() {
        return incoming;
    }

    static void setIncoming(Incoming data) {
        incoming = data;
    }

    static String from(String fromContact, String fromSip) {
        String from = TextUtils.isEmpty(fromContact) ?
                TextUtils.isEmpty(fromSip) ? "" : fromSip :
                fromContact;
        return from.contains("\"") ?
                from.substring(from.indexOf("\"") + 1, from.lastIndexOf("\"")) :
                from;

    }

    static String to(String toSip) {
        return TextUtils.isEmpty(toSip) ? "" :
                toSip.substring(toSip.indexOf(":") + 1, toSip.indexOf("@"));
    }

    static void startVibrating(Context context) {
        if (vibrator == null)
            vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(new long[]{1000, 1000, 1000, 1000, 1000}, 3);
    }

    static void stopVibrating() {
        if (vibrator != null)
            vibrator.cancel();
    }

    public static boolean isAppIsInBackground(Context context) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(context.getPackageName())) {
                            isInBackground = false;
                        }
                    }
                }
            }
        } else {
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;

            if (componentInfo.getPackageName().equals(context.getPackageName())) {
                isInBackground = false;
            }
        }

        return isInBackground;
    }
}
