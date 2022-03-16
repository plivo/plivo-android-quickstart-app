package com.plivo.plivosimplequickstart;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NotificationCompat;
import androidx.core.content.PermissionChecker;

import com.google.firebase.iid.FirebaseInstanceId;
import com.plivo.endpoint.Incoming;
import com.plivo.endpoint.Outgoing;
import com.plivo.plivosimplequickstart.PlivoBackEnd.STATE;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import static com.plivo.plivosimplequickstart.Utils.HH_MM_SS;
import static com.plivo.plivosimplequickstart.Utils.MM_SS;
import static com.plivo.plivosimplequickstart.Utils.startVibrating;
import static com.plivo.plivosimplequickstart.Utils.stopVibrating;

public class MainActivity extends AppCompatActivity implements PlivoBackEnd.BackendListener {
    private static final int PERMISSIONS_REQUEST_CODE = 21;
    private static final String TAG = MainActivity.class.getName();

    private Timer callTimer;

    private int tick;

    private ActionBar actionBar;

    private boolean isSpeakerOn = false, isHoldOn = false, isMuteOn = false;

    private Object callData;

    static String username = null;
    static String password = null;
    Outgoing outgoing;
    boolean isKeyboardOpen = false;
    String keypadData = "";
    boolean isBackPressed = false;

    public static boolean isInstantiated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isInstantiated = true;
        setContentView(R.layout.activity_main);
        Log.d("@@Incoming", "onCretae");
        actionBar = getSupportActionBar();
//        actionBar.setDisplayHomeAsUpEnabled(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (PermissionChecker.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                init();
            } else {
                requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_REQUEST_CODE);
            }
        } else {
            init();
        }

    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed Called");
        if (isBackPressed) {
            return;
        }
        if (outgoing == null && Utils.getIncoming() == null) {
            isBackPressed = true;
            logout();
            isBackPressed = false;
            super.onBackPressed();
        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String action = intent.getAction();
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Constants.ANSWER_ACTION.equals(action)) {
            answerCall();
            notificationManager.cancel(Constants.NOTIFICATION_ID);
            Utils.stopVibrating();
        } else if (Constants.REJECT_ACTION.equals(action)) {
            rejectCall();
            notificationManager.cancel(Constants.NOTIFICATION_ID);
            Utils.stopVibrating();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                if (grantResults != null && grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    init();
                }
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void init() {
        Log.d("@@Incoming", "init");
        registerBackendListener();
        loginWithToken();
    }

    private void registerBackendListener() {
        Log.d("@@Incoming", "registerBackendListener");
        ((App) getApplication()).backend().setListener(this);
        Utils.setBackendListener(this);
    }

    private void loginWithToken() {
        Log.d("@@Incoming", "loginWithToken");
        if (Pref.newInstance(this).getBoolean(Constants.LOG_IN)) {
            updateUI(STATE.IDLE, null);
            callData = Utils.getIncoming();
            if (callData != null) {
                Log.d("@@Incoming", "loginWithToken | callData not null");
                showInCallUI(STATE.RINGING, Utils.getIncoming());
            } else {
                boolean isIncomingCallRinging = getIntent().getBooleanExtra(Constants.INCOMING_CALL_RINGING, false);
                HashMap<String, String> pushMap = (HashMap<String, String>) getIntent().getSerializableExtra(Constants.MAP);
                Log.d("@@Incoming", " flag = " + isIncomingCallRinging);

                if (pushMap != null)
                    Log.d("@@Incoming", " pushMap = " + pushMap.size());


                Log.d("@@Incoming", "loginWithToken | callData null");
                FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(this, instanceIdResult -> {
                    if (isIncomingCallRinging) {
                        if (((App) getApplication()).backend().loginForIncoming(instanceIdResult.getToken(), Utils.USERNAME, Utils.PASSWORD)) {
                            Log.d("@@Incoming", "PlivoFCMService | onMessageReceived | login success");
                            ((App) getApplication()).backend().relayIncomingPushData(pushMap);
                        }
                    } else {
                        ((App) getApplication()).backend().login(instanceIdResult.getToken(), Utils.USERNAME, Utils.PASSWORD);
                    }
                });
            }
        } else {
            /*FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(this, instanceIdResult ->
                    ((App) getApplication()).backend().login(instanceIdResult.getToken(), Utils.USERNAME, Utils.PASSWORD));*/
        }
    }

    public void onClickLogout(View view) {
        logout();
    }

    private void logout() {
        ((App) getApplication()).backend().logout();
    }

    /**
     * Display & Handle Outgoing Calls
     *
     * @param state
     * @param outgoing
     */
    private void showOutCallUI(STATE state, Outgoing outgoing) {

        String title = state.name();
        TextView callerState;
        switch (state) {
            case IDLE:
                EditText phoneNumberText = (EditText) findViewById(R.id.call_text);
                String phoneNum = phoneNumberText.getText().toString();
                setContentView(R.layout.call);
                ((ImageButton) findViewById(R.id.speaker)).setVisibility(View.GONE);
                ((ImageButton) findViewById(R.id.mute)).setVisibility(View.GONE);
                ((ImageButton) findViewById(R.id.hold)).setVisibility(View.GONE);
                TextView callerName = (TextView) findViewById(R.id.caller_name);
                callerState = (TextView) findViewById(R.id.caller_state);
                callerName.setText(phoneNum);
                callerState.setText(title);
                makeCall(phoneNum);
                break;
            case RINGING:
                callerState = (TextView) findViewById(R.id.caller_state);
                callerState.setText(Constants.RINGING_LABEL);
                ((ImageButton) findViewById(R.id.speaker)).setVisibility(View.VISIBLE);
                ((ImageButton) findViewById(R.id.mute)).setVisibility(View.VISIBLE);
                ((ImageButton) findViewById(R.id.hold)).setVisibility(View.VISIBLE);
                break;
            case ANSWERED:
                startTimer();
                ((ImageButton) findViewById(R.id.keypad)).setVisibility(View.VISIBLE);
                ((TextView) findViewById(R.id.dial_numbers)).setText("");
                break;
            case HANGUP:
            case REJECTED:
            case INVALID:
                cancelTimer();
                this.outgoing = null;
                setContentView(R.layout.activity_main);
                updateUI(STATE.IDLE, null);
                break;
        }
    }

    /**
     * Display & Handle Incoming Calls
     *
     * @param state
     * @param incoming
     */
    private void showInCallUI(STATE state, Incoming incoming) {

        String title = (incoming != null ? Utils.from(incoming.getFromContact(), incoming.getFromSip()) : "");

        String callerId = com.plivo.endpoint.Utils.getEndpointFromUri(title);

        switch (state) {
            case ANSWERED:
                EditText phoneNumberText = (EditText) findViewById(R.id.call_text);
                String phoneNum = phoneNumberText.getText().toString();
                setContentView(R.layout.call);
                TextView callerName = (TextView) findViewById(R.id.caller_name);
                callerName.setText(callerId);
                ((ImageButton) findViewById(R.id.keypad)).setVisibility(View.GONE);
                startTimer();
                break;

            case RINGING:
                boolean isIncomingCallRinging = getIntent().getBooleanExtra(Constants.INCOMING_CALL_RINGING, false);
                if (!isIncomingCallRinging)
                    notificationDialog(title, incoming);
                break;
            case HANGUP:
                cancelTimer();
                removeNotification(Constants.NOTIFICATION_ID);
                setContentView(R.layout.activity_main);
                updateUI(STATE.IDLE, null);
                Utils.setIncoming(null);
                break;
            case REJECTED:
                removeNotification(Constants.NOTIFICATION_ID);
                Utils.setIncoming(null);
                break;
        }
    }

    private void notificationDialog(String title, Incoming incoming) {
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
        answerIntent.setAction(Constants.ANSWER_ACTION);
        PendingIntent AcceptIntent = PendingIntent.getActivity(this, 0, answerIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);
        Intent rejectIntent = new Intent(this, MainActivity.class);
        rejectIntent.setAction(Constants.REJECT_ACTION);
        PendingIntent RejectIntent = PendingIntent.getActivity(this, 0, rejectIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);

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

    private void removeNotification(int id) {
        stopVibrating();
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(id);
    }

    private void startTimer() {
        cancelTimer();

        callTimer = new Timer(false);
        callTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    int hours = (int) TimeUnit.SECONDS.toHours(tick);
                    int minutes = (int) TimeUnit.SECONDS.toMinutes(tick -= TimeUnit.HOURS.toSeconds(hours));
                    int seconds = (int) (tick - TimeUnit.MINUTES.toSeconds(minutes));
                    String text = hours > 0 ? String.format(HH_MM_SS, hours, minutes, seconds) : String.format(MM_SS, minutes, seconds);
                    TextView timerTextView = (TextView) findViewById(R.id.caller_state);
                    if (timerTextView != null) {
                        timerTextView.setVisibility(View.VISIBLE);
                        timerTextView.setText(text);
                        tick++;
                    }
                });
            }
        }, 100, TimeUnit.SECONDS.toMillis(1));
    }

    private void cancelTimer() {
        if (callTimer != null) callTimer.cancel();
        tick = 0;
    }

    private void makeCall(String phoneNum) {
        outgoing = ((App) getApplication()).backend().getOutgoing();
        if (outgoing != null) {
            Map<String, String> headers = new HashMap<String, String>();
            headers.put("X-PH-Header1", "Value1");
            headers.put("X-PH-Header2", "Value2");
            if (!outgoing.call(phoneNum, headers)) {
                updateUI(STATE.INVALID, outgoing);
            }
        }
    }

    public void onClickBtnMakeCall(View view) {
        EditText phoneNumberText = (EditText) findViewById(R.id.call_text);
        String phoneNumber = phoneNumberText.getText().toString();
        if (phoneNumber.matches("")) {
            Toast.makeText(this, Constants.OUTGOING_CALL_DIAL_HINT, Toast.LENGTH_SHORT).show();
            return;
        }
        showOutCallUI(STATE.IDLE, null);
    }

    public void onClickBtnEndCall(View view) {
        endCall();
    }

    private void endCallRituals() {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        cancelTimer();
        isSpeakerOn = false;
        isHoldOn = false;
        isMuteOn = false;
        isKeyboardOpen = false;
        keypadData = "";
        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        audioManager.setSpeakerphoneOn(isSpeakerOn);
        setContentView(R.layout.activity_main);
        updateUI(STATE.IDLE, null);
    }

    public void endCall() {
        if (outgoing != null) {
            outgoing.hangup();
        } else if (Utils.getIncoming() != null) {
            Utils.getIncoming().hangup();
        }
    }

    public void answerCall() {
        Log.d("TAG", "answerCall: inside answer");

        if (Utils.getIncoming() != null) {
            Utils.getIncoming().answer();
            updateUI(STATE.ANSWERED, Utils.getIncoming());
        } else {
            Log.d("TAG", "answerCall: inside answer, call data is null");
        }
    }

    public void rejectCall() {
        if (Utils.getIncoming() != null) {
            Utils.getIncoming().reject();
        } else {
            Log.d("TAG", "rejectCall: call data is null");
        }
    }

    public void onClickBtnSpeaker(View view) {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        ImageButton btn = (ImageButton) findViewById(R.id.speaker);
        if (isSpeakerOn) {
            isSpeakerOn = false;
            btn.setImageResource(R.drawable.speaker);
            audioManager.setMode(AudioManager.MODE_IN_CALL);
            audioManager.setMode(AudioManager.MODE_NORMAL);
        } else {
            isSpeakerOn = true;
            btn.setImageResource(R.drawable.speaker_selected);
            audioManager.setMode(AudioManager.MODE_NORMAL);
            audioManager.setMode(AudioManager.MODE_IN_CALL);
        }
        audioManager.setSpeakerphoneOn(isSpeakerOn);
    }

    public void onClickBtnHold(View view) {
        ImageButton btn = (ImageButton) findViewById(R.id.hold);
        if (isHoldOn) {
            isHoldOn = false;
            btn.setImageResource(R.drawable.hold);
            unHoldCall();
        } else {
            isHoldOn = true;
            btn.setImageResource(R.drawable.hold_selected);
            holdCall();
        }
    }

    public void holdCall() {
        if (callData != null) {
            if (callData instanceof Outgoing) {
                ((Outgoing) callData).hold();
            } else {
                ((Incoming) callData).hold();
            }
        }
    }

    public void unHoldCall() {
        if (callData != null) {
            if (callData instanceof Outgoing) {
                ((Outgoing) callData).unhold();
            } else {
                ((Incoming) callData).unhold();
            }
        }
    }


    public void onClickBtnMute(View view) {
        ImageButton btn = (ImageButton) findViewById(R.id.mute);
        if (isMuteOn) {
            isMuteOn = false;
            btn.setImageResource(R.drawable.mute);
            unMuteCall();
        } else {
            isMuteOn = true;
            btn.setImageResource(R.drawable.mute_selected);
            muteCall();
        }
    }

    public void onClickBtnKeypad(View view) {
        ImageButton btn = (ImageButton) findViewById(R.id.keypad);
        LinearLayout ll_keypad = (LinearLayout) findViewById(R.id.ll_keypad);
        ConstraintLayout cl_call = (ConstraintLayout) findViewById(R.id.cl_call);

        if (isKeyboardOpen) {
            isKeyboardOpen = false;
            ll_keypad.setVisibility(View.GONE);
            cl_call.setVisibility(View.VISIBLE);
            btn.setImageResource(R.drawable.keypad_deactivated);

        } else {
            isKeyboardOpen = true;
            cl_call.setVisibility(View.GONE);
            ll_keypad.setVisibility(View.VISIBLE);
            btn.setImageResource(R.drawable.keypad_activated);

        }
    }

    public void onClickKeypadButton(View v) {
        TextView textView = (TextView) findViewById(R.id.dial_numbers);
        String value = v.getTag().toString().trim();
        keypadData += value;
        textView.setText(keypadData);
        if (outgoing != null) {
            outgoing.sendDigits(value);
        } else if (Utils.getIncoming() != null) {
            Utils.getIncoming().sendDigits(value);
        }

    }

    public void muteCall() {
        if (callData != null) {
            if (callData instanceof Outgoing) {
                ((Outgoing) callData).mute();
            } else {
                ((Incoming) callData).mute();
            }
        }
    }

    public void unMuteCall() {
        if (callData != null) {
            if (callData instanceof Outgoing) {
                ((Outgoing) callData).unmute();
            } else {
                ((Incoming) callData).unmute();
            }
        }
    }

    private void updateUI(PlivoBackEnd.STATE state, Object data) {
        callData = data;
        if (state.equals(STATE.REJECTED) || state.equals(STATE.HANGUP) || state.equals(STATE.INVALID)) {
            if (data != null) {
                if (data instanceof Outgoing) {
                    // handle outgoing
                    showOutCallUI(state, (Outgoing) data);
                } else {
                    // handle incoming
                    showInCallUI(state, (Incoming) data);
                }
            }
        } else {

            if (findViewById(R.id.call_btn) == null || findViewById(R.id.logged_in_as) == null || findViewById(R.id.logging_in_label) == null) {
                if (data != null) {
                    if (data instanceof Outgoing) {
                        // handle outgoing
                        showOutCallUI(state, (Outgoing) data);
                    } else {
                        // handle incoming
                        showInCallUI(state, (Incoming) data);
                    }
                }
            } else {
                ((AppCompatTextView) findViewById(R.id.logging_in_label)).setText(Constants.LOGGED_IN_LABEL);
                ((AppCompatTextView) findViewById(R.id.logged_in_as)).setText(Utils.USERNAME);
                ((Button) findViewById(R.id.btlogout)).setText(Constants.LOG_OUT);
                findViewById(R.id.call_btn).setEnabled(true);

                if (data != null) {
                    if (data instanceof Outgoing) {
                        // handle outgoing
                        showOutCallUI(state, (Outgoing) data);
                    } else {
                        // handle incoming
                        showInCallUI(state, (Incoming) data);
                    }
                }
            }
        }
    }

    @Override
    public void onLogin(boolean success) {
        runOnUiThread(() -> {
            if (success) {
                updateUI(STATE.IDLE, null);
            } else {
                Toast.makeText(this, R.string.login_failed, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onLogout() {
        Utils.setLoggedinStatus(false);
        startActivity(new Intent(this,LoginActivity.class));
        finish();
    }

    @Override
    public void onIncomingCall(Incoming data, PlivoBackEnd.STATE callState) {

        runOnUiThread(() -> {
            if (data != null) {
                Log.d("TAG", "incoming data is not null");
            }
            if (outgoing != null) {
                outgoing = null;
            }
            updateUI(callState, data);
        });
    }

    @Override
    public void onOutgoingCall(Outgoing data, PlivoBackEnd.STATE callState) {
        runOnUiThread(() -> {
            if (callState == STATE.HANGUP)
                endCallRituals();
            updateUI(callState, data);
        });
    }

    @Override
    public void onIncomingDigit(String digit) {
        runOnUiThread(() -> Toast.makeText(this, String.format(getString(R.string.dtmf_received), digit), Toast.LENGTH_SHORT).show());
    }

    @Override
    public void mediaMetrics(HashMap messageTemplate) {

    }
}
