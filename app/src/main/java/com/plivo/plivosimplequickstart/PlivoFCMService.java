package com.plivo.plivosimplequickstart;

import static com.plivo.plivosimplequickstart.Utils.startVibrating;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.plivo.endpoint.Incoming;

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
                ((App) getApplication()).backend().loginForIncomingWithJwt(deviceToken, Pref.newInstance(getApplicationContext()).getString(Constants.JWT_ACCESS_TOKEN),"", pushMap);
                Log.d(TAG, "onMessageReceived | loginForIncomingWithJwt ");
            } else if (isLoginWithTokenGenerator) {
//                Utils.setLoginWithTokenGenerator(true);
                ((App) getApplication()).backend().loginWithAccessTokenGenerator(pushMap);
                //Do nothing just go to MainActivity & handle things there
            } else {
                ((App) getApplication()).backend().loginForIncomingWithUsername(username, password, deviceToken, "", pushMap);
                Log.d(TAG, "onMessageReceived | loginForIncomingWithUsername");
            }

            notificationDialog(pushMap.get("callerID"), isLoginWithTokenGenerator, pushMap);

            super.onMessageReceived(remoteMessage);
        }
    }

    private void notificationDialog(String title, boolean isLoginWithTokenGenerator, HashMap pushMap) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String NOTIFICATION_CHANNEL_ID = Constants.NOTIFICATION_CHANNEL;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            @SuppressLint("WrongConstant") NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "My Notifications", NotificationManager.IMPORTANCE_MAX);
            // Configure the notification channel.
            notificationChannel.setDescription(Constants.NOTIFICATION_DESCRIPTION);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        Intent answerIntent = new Intent(this, MainActivity.class);
        Bundle bundle = new Bundle();
        bundle.putBoolean(Constants.LAUNCH_ACTION, true);
        bundle.putBoolean(Constants.JWT_ACCESS_TOKEN_GENERATOR, isLoginWithTokenGenerator);
        bundle.putSerializable(Constants.PAYLOAD, pushMap);
        answerIntent.putExtras(bundle);
        answerIntent.setAction(Constants.ANSWER_ACTION);
        answerIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent AcceptIntent = PendingIntent.getActivity(this, 0, answerIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent rejectIntent = new Intent(this, MainActivity.class)
                .putExtra(Constants.LAUNCH_ACTION, true)
                .putExtra(Constants.JWT_ACCESS_TOKEN_GENERATOR, isLoginWithTokenGenerator)
                .putExtra(Constants.PAYLOAD, pushMap)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ;
        rejectIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_SINGLE_TOP);
        rejectIntent.setAction(Constants.REJECT_ACTION);
        PendingIntent RejectIntent = PendingIntent.getActivity(this, 0, rejectIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        notificationBuilder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE)
                .setCategory(Notification.CATEGORY_CALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker(Constants.NOTIFICATION_CHANNEL)
                .setPriority(Notification.PRIORITY_MAX)
                .setContentTitle(Constants.NOTIFICATION_DESCRIPTION)
                .setContentText(title)
                .addAction(android.R.drawable.ic_menu_delete, getString(R.string.reject), RejectIntent)
                .addAction(android.R.drawable.ic_menu_call, getString(R.string.answer), AcceptIntent)
                .setOngoing(true)
                .setVibrate(new long[]{0, 100, 500, 100, 500, 100, 500, 100, 500, 100, 500})
                .setContentInfo(Constants.NOTIFICATION_DESCRIPTION);
        notificationManager.notify(0, notificationBuilder.build());
        startVibrating(this);
    }

}
