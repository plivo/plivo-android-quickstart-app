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
import android.os.Vibrator;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.plivo.endpoint.Incoming;

import java.util.HashMap;

public class PlivoFCMService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        if (remoteMessage.getData() != null) {
            String deviceToken = Utils.getDeviceToken();
            HashMap<String, String> pushMap = new HashMap<>(remoteMessage.getData());

            String username = Utils.USERNAME;
            String password = Utils.PASSWORD;
            if (((App) getApplication()).backend().loginForIncoming(deviceToken,Pref.newInstance(getApplicationContext()).getBoolean(Constants.IS_LOGIN_WITH_TOKEN))) {
                Log.d("@@Incoming", "onMessageReceived | relayIncomingPushData");
                ((App) getApplication()).backend().relayIncomingPushData(pushMap);
            }

            Log.d("@@Incoming", "PlivoFCMService | onMessageReceived | start MainActivity");
            startActivity(new Intent(this, MainActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            );

            if (Utils.getBackendListener() == null) {
                Log.d("@@Incoming", "PlivoFCMService | onMessageReceived | getBackendListener null");
                notificationDialog();
            }
        }
    }


    public void notificationDialog() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String NOTIFICATION_CHANNEL_ID = Constants.NOTIFICATION_CHANNEL;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            @SuppressLint("WrongConstant") NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "My Notifications", NotificationManager.IMPORTANCE_MAX);
            // Configure the notification channel.
            notificationChannel.setDescription(Constants.NOTIFICATION_DESCRIPTION);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 100, 500, 100, 500, 100, 500, 100, 500, 100, 500});
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        Intent launchIntent = new Intent(this, MainActivity.class);
        launchIntent.setAction(Constants.LAUNCH_ACTION);
        PendingIntent LaunchIntent = PendingIntent.getActivity(this, 0, launchIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        notificationBuilder.setAutoCancel(true)
                .setCategory(Notification.CATEGORY_CALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker(Constants.NOTIFICATION_CHANNEL)
                .setPriority(Notification.PRIORITY_MAX)
                .setContentTitle(Constants.NOTIFICATION_DESCRIPTION)
                .addAction(android.R.drawable.ic_menu_call, getString(R.string.launch), LaunchIntent)
                .setOngoing(true)
                .setVibrate(new long[]{0, 100, 500, 100, 500, 100, 500, 100, 500, 100, 500})
                .setContentInfo(Constants.NOTIFICATION_DESCRIPTION);
        notificationManager.notify(0, notificationBuilder.build());

        Utils.startVibrating(this);
    }
}
