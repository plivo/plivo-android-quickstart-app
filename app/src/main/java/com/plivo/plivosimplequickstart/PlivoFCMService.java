package com.plivo.plivosimplequickstart;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;

public class PlivoFCMService extends FirebaseMessagingService {
    private static final String TAG = "PlivoFCMService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Log.d(TAG, "****onMessageReceived");
        if (remoteMessage.getData() != null) {
            String deviceToken = Utils.getDeviceToken();
            HashMap<String, String> pushMap = new HashMap<>(remoteMessage.getData());

            String username = Utils.USERNAME;
            String password = Utils.PASSWORD;

            boolean isLoginWithTokenGenerator = Pref.newInstance(getApplicationContext()).getBoolean(Constants.IS_LOGIN_WITH_USERNAME);

            if (Pref.newInstance(getApplicationContext()).getBoolean(Constants.IS_LOGIN_WITH_TOKEN)) {
                ((App) getApplication()).backend().loginForIncomingWithJwt(deviceToken, Pref.newInstance(getApplicationContext()).getString(Constants.JWT_ACCESS_TOKEN), pushMap);
                Log.d(TAG, "onMessageReceived | loginForIncomingWithJwt ");
            } else if (isLoginWithTokenGenerator){
                //Do nothing just go to MainActivity & handle things there
            }else {
                ((App) getApplication()).backend().loginForIncomingWithUsername(username, password, deviceToken, "",  pushMap);
                Log.d(TAG, "onMessageReceived | loginForIncomingWithUsername");
            }

            Log.d(TAG, "PlivoFCMService | onMessageReceived | start MainActivity");
            startActivity(new Intent(this, MainActivity.class)
                    .putExtra(Constants.LAUNCH_ACTION, true)
                    .putExtra(Constants.JWT_ACCESS_TOKEN_GENERATOR, isLoginWithTokenGenerator)
                    .putExtra(Constants.PAYLOAD,pushMap)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            );

            super.onMessageReceived(remoteMessage);
        }
    }

}
