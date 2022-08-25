package com.plivo.plivosimplequickstart;

import static com.plivo.plivosimplequickstart.Utils.HH_MM_SS;
import static com.plivo.plivosimplequickstart.Utils.MM_SS;
import static com.plivo.plivosimplequickstart.Utils.USERNAME;
import static com.plivo.plivosimplequickstart.Utils.startVibrating;
import static com.plivo.plivosimplequickstart.Utils.stopVibrating;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NotificationCompat;
import androidx.core.content.PermissionChecker;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.auth0.android.jwt.JWT;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.iid.FirebaseInstanceId;
import com.plivo.endpoint.Incoming;
import com.plivo.endpoint.Outgoing;
import com.plivo.plivosimplequickstart.PlivoBackEnd.STATE;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

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
    boolean isLoginFirstTime = false;

    public static boolean isInstantiated = false;
    private BroadcastReceiver callIncomingReceiver;
    ConstraintLayout constraintLayout;
    ProgressBar progressBar;
    ConstraintLayout parentPanel;
    private ProgressDialog progressDialog;
    boolean activeCall = false;
    private boolean isLoginForIncomingWithTokenGenerator = false;
    private HashMap<String, String> payload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");
        isInstantiated = true;
        setContentView(R.layout.activity_main);
        progressDialog = new ProgressDialog(this);
        ((App) getApplication()).backend().registerListener(this);
        actionBar = getSupportActionBar();

        username = Pref.newInstance(MainActivity.this).getString(Constants.USERNAME);
        password = Pref.newInstance(MainActivity.this).getString(Constants.PASSWORD);

        constraintLayout = findViewById(R.id.cl_main);
        progressBar = findViewById(R.id.progress_bar);
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

    public JWT getDecodedJwt(String jwt) {
        return new JWT(jwt);
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed Called");
        if (isBackPressed) {
            return;
        }
        if (outgoing == null && Utils.getIncoming() == null) {
            super.onBackPressed();
        }

    }

    private void showFeedbackDialog(){
        SubmitFeedbackDialog dialogFragment = new SubmitFeedbackDialog();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog2");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        dialogFragment.show(ft, "feedback");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d("MainActivity", "onNewIntent");
        String action = intent.getAction();
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Constants.ANSWER_ACTION.equals(action)) {
            Log.d("@@Incoming", "onNewIntent | ANSWER_ACTION");
            answerCall();
            notificationManager.cancel(Constants.NOTIFICATION_ID);
            Utils.stopVibrating();
        } else if (Constants.REJECT_ACTION.equals(action)) {
            rejectCall();
            notificationManager.cancel(Constants.NOTIFICATION_ID);
            Utils.stopVibrating();
        }else{
            Log.d("MainActivity", "onNewIntent | loginWithToken");
            loginWithToken(intent);
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
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        ((App) getApplication()).backend().unregisterListener(this);
        super.onDestroy();

    }

    @Override
    protected void onPause() {
        progressDialog.dismiss();
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
        boolean isLoginForIncoming = getIntent().getBooleanExtra(Constants.LAUNCH_ACTION, false);
        boolean tokenGenerator = getIntent().getBooleanExtra(Constants.JWT_ACCESS_TOKEN_GENERATOR, false);
        payload = (HashMap<String, String>) getIntent().getSerializableExtra(Constants.PAYLOAD);
        Log.d("****@@Incoming", "isLoginForIncoming " + isLoginForIncoming);
        Log.d("****@@Incoming", "loginWithToken " + Utils.getLoggedinStatus());

        isLoginForIncomingWithTokenGenerator = tokenGenerator;
        if ((Utils.getLoggedinStatus() || isLoginForIncoming) && !tokenGenerator) {
            updateUI(STATE.IDLE, null);
            callData = Utils.getIncoming();
            if (callData != null) {
                Log.d("****@@Incoming", "loginWithToken | callData not null");
                showInCallUI(STATE.RINGING, Utils.getIncoming());
            }
        } else if (Pref.newInstance(MainActivity.this).getBoolean(Constants.IS_LOGIN_WITH_TOKEN)) {
            Log.d(TAG, "****loginWithToken: login with token");
            String token = Pref.newInstance(MainActivity.this).getString(Constants.JWT_ACCESS_TOKEN);
            FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(this, instanceIdResult ->
                    ((App) getApplication()).backend().loginWithJwtToken(instanceIdResult.getToken(), token));
        } else if (Pref.newInstance(MainActivity.this).getBoolean(Constants.IS_LOGIN_WITH_USERNAME)) {
            Log.d(TAG, "****loginWithToken: login with accessToken generator");
            String token = Pref.newInstance(MainActivity.this).getString(Constants.LOGIN_USERNAME);
            ((App) getApplication()).backend().loginWithAccessTokenGenerator();
        } else {
            Log.d("****@@Incoming", "loginWithToken | is not logged in");
            FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(this, instanceIdResult ->
                    ((App) getApplication()).backend().login(instanceIdResult.getToken(), username, password));
        }
    }

    public void loginWithToken(Intent intent) {
        boolean isLoginForIncoming = intent.getBooleanExtra(Constants.LAUNCH_ACTION, false);
        boolean tokenGenerator = intent.getBooleanExtra(Constants.JWT_ACCESS_TOKEN_GENERATOR, false);
        payload = (HashMap<String, String>) intent.getSerializableExtra(Constants.PAYLOAD);
//        Log.d("****@@Incoming", "isLoginForIncoming " + isLoginForIncoming);
        Log.d("****@@Incoming", "loginWithToken " + Utils.getLoggedinStatus());

        isLoginForIncomingWithTokenGenerator = tokenGenerator;
        if ((Utils.getLoggedinStatus()) && !tokenGenerator) {
            updateUI(STATE.IDLE, null);
            callData = Utils.getIncoming();
            if (callData != null) {
                Log.d("****@@Incoming", "loginWithToken | callData not null");
                showInCallUI(STATE.RINGING, Utils.getIncoming());
            }
        } else if (Pref.newInstance(MainActivity.this).getBoolean(Constants.IS_LOGIN_WITH_TOKEN)) {
            Log.d(TAG, "****loginWithToken: login with token");
            String token = Pref.newInstance(MainActivity.this).getString(Constants.JWT_ACCESS_TOKEN);
            HashMap<String, String> finalPayload = payload;
            FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(this, instanceIdResult ->
                    ((App) getApplication()).backend().loginForIncomingWithJwt(instanceIdResult.getToken(), token, finalPayload));
        } else if (Pref.newInstance(MainActivity.this).getBoolean(Constants.IS_LOGIN_WITH_USERNAME)) {
            Log.d(TAG, "****loginWithToken: login with accessToken generator");
            String token = Pref.newInstance(MainActivity.this).getString(Constants.LOGIN_USERNAME);
            ((App) getApplication()).backend().loginWithAccessTokenGenerator();
        } else {
            Log.d("****@@Incoming", "loginWithToken | is not logged in");
            HashMap<String, String> finalPayload1 = payload;
            FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(this, instanceIdResult ->
                    ((App) getApplication()).backend().loginForIncomingWithUsername(username, password,instanceIdResult.getToken(),"", finalPayload1));
        }
    }

    public void onClickLogout(View view) {
        logout();
    }

    private void logout() {
        Log.d(TAG, "logout: ");
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
        Log.d(TAG, "showOutCallUI: " + state);
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
                activeCall = true;
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
        Log.d(TAG, "****showInCallUI: " + state);

        String title = (incoming != null ? Utils.from(incoming.getFromContact(), incoming.getFromSip()) : "");

        String callerId = com.plivo.endpoint.Utils.getEndpointFromUri(title);

        switch (state) {
            case ANSWERED:
                progressBar.setVisibility(View.GONE);
                setContentView(R.layout.call);
                TextView callerName = (TextView) findViewById(R.id.caller_name);
                callerName.setText(callerId);
                ((ImageButton) findViewById(R.id.keypad)).setVisibility(View.GONE);
                startTimer();
                break;

            case RINGING:
                Log.d(TAG, "****showInCallUI: ringingF");
                notificationDialog(title, incoming);
                break;
            case HANGUP:
                cancelTimer();
                removeNotification(Constants.NOTIFICATION_ID);
                setContentView(R.layout.activity_main);
                progressBar.setVisibility(View.GONE);
                constraintLayout.setVisibility(View.VISIBLE);
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
            Log.d("@@Incoming", "answerCall");
            constraintLayout = (ConstraintLayout) findViewById(R.id.cl_main);
            progressBar = (ProgressBar) findViewById(R.id.progress_bar);

            constraintLayout.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            Utils.getIncoming().answer();
//            updateUI(STATE.ANSWERED, Utils.getIncoming());
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
        Log.d(TAG, "****updateUI: " + state);
        callData = data;
        if (state.equals(STATE.REJECTED) || state.equals(STATE.HANGUP) || state.equals(STATE.INVALID)) {
            Log.d(TAG, "****updateUI: REJECTED,HANGUP,INVALID");
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
                Log.d(TAG, "updateUI: ");
                ((AppCompatTextView) findViewById(R.id.logging_in_label)).setText(Constants.LOGGED_IN_LABEL);
                ((AppCompatTextView) findViewById(R.id.logged_in_as)).setText(username);
                ((Button) findViewById(R.id.btlogout)).setText(Constants.LOG_OUT);
                findViewById(R.id.btlogout).setOnClickListener(view -> {
                    logout();
                });
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

        Log.d(TAG, "****onLogin: " + success);
        runOnUiThread(() -> {
            if (success) {
                if (!isLoginFirstTime) isLoginFirstTime = true;
                if (Pref.newInstance(MainActivity.this).getBoolean(Constants.IS_LOGIN_WITH_TOKEN) || Pref.newInstance(MainActivity.this).getBoolean(Constants.IS_LOGIN_WITH_USERNAME)) {
                    username = ((App) getApplication()).backend().getJWTUserName();
                    Log.d(TAG, "onLogin:  " + username);
                    Pref.newInstance(MainActivity.this).setString(USERNAME, username);
                    ((AppCompatTextView) findViewById(R.id.logged_in_as)).setText(username);
                }
                updateUI(STATE.IDLE, null);
            } else {
//                Toast.makeText(this, R.string.login_failed, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onLoginFailed(String message) {
        Log.d(TAG, "****onLoginFailed: ");
        runOnUiThread(() -> {
            setContentView(R.layout.activity_main);
            parentPanel = findViewById(R.id.parentPanel);
            Snackbar snackbar = Snackbar.make(parentPanel, message, Snackbar.LENGTH_LONG);
            snackbar.show();
        });

    }

    @Override
    public void onLogout() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
//        Utils.setLoggedinStatus(false);
//        startActivity(new Intent(this, LoginActivity.class));
//        finish();
    }

    @Override
    public void onIncomingCall(Incoming data, PlivoBackEnd.STATE callState) {
        Log.d("****Incoming", "onIncomingCall");
        runOnUiThread(() -> {
            if (data != null) {
                Log.d("****TAG", "incoming data is not null");
            }
            if (outgoing != null) {
                outgoing = null;
            }
            if (callState == STATE.HANGUP) {
                showFeedbackDialog();
            }
            updateUI(callState, data);
        });
    }

    @Override
    public void onOutgoingCall(Outgoing data, PlivoBackEnd.STATE callState) {
        runOnUiThread(() -> {
            if (callState == STATE.HANGUP) {
                endCallRituals();
                showFeedbackDialog();
            }
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

    @Override
    public void onPermissionDenied(String message) {
        setContentView(R.layout.activity_main);
        parentPanel = findViewById(R.id.parentPanel);
        Snackbar snackbar = Snackbar.make(parentPanel, message, Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    @Override
    public void getAccessToken() {
        Log.d(TAG, "getAccessToken: ");
        this.runOnUiThread(() -> {
            setContentView(R.layout.activity_main);
            generateToken();
        });
    }


    public void generateToken() {
        Pref.newInstance(getApplicationContext()).setBoolean(Constants.IS_LOGIN_WITH_USERNAME, true);
        new Thread(() -> {
            String sub;
            sub = Pref.newInstance(getApplicationContext()).getString(Constants.LOGIN_USERNAME);
            if (sub.isEmpty()) sub = "plivoUser";
            long nbf = System.currentTimeMillis() / 1000;
            long exp = nbf + 240;
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("application/json");
            Log.d(TAG, "generateToken: sub: " + sub);
            Log.d(TAG, "generateToken: nbf: " + nbf);
            RequestBody body = RequestBody.create(mediaType, "{\n    \"iss\": \"MAY2RJNZKZNJMWOTG4NT\",\n    \"sub\": \"" + sub + "\",\n    \"per\": {\n        \"voice\": {\n            \"incoming_allow\": true,\n            \"outgoing_allow\": true\n        }\n    },\n    \"exp\": " + exp + "\n}\n");
            Request request = new Request.Builder()
                    .url("https://api.plivo.com/v1/Account/MAY2RJNZKZNJMWOTG4NT/JWT/Token")
                    .method("POST", body)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Basic TUFZMlJKTlpLWk5KTVdPVEc0TlQ6WWpJM1pXVmpPV0poTW1Kak5USXhNakJtTkdJeVlUUmtZVGd3TUdSaA==")
                    .build();
            try {
                runOnUiThread(() -> {
                    progressDialog.setMessage("generating..");
                    progressDialog.show();
                });
                Response response = client.newCall(request).execute();
                progressDialog.dismiss();
                String responseData = response.body().string();
                Log.d(TAG, "run: generateToken " + responseData);

                if (response.code() == 200) {
                    JSONObject jsonResponse = new JSONObject(responseData);
                    String token = jsonResponse.getString("token");
                    Log.d(TAG, "run: generateToken jwtToken" + token);
                    Pref.newInstance(getApplicationContext()).setString(Constants.JWT_ACCESS_TOKEN, token);
                    runOnUiThread(() -> {
                        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(this, instanceIdResult -> {
                            Log.d(TAG, "generateToken: device-token" + instanceIdResult.getToken());
                            if (isLoginForIncomingWithTokenGenerator && payload != null) {
                                ((App) getApplication()).backend().loginForIncomingWithJwt(instanceIdResult.getToken(), token, payload);
                            } else {
                                ((App) getApplication()).backend().loginWithJwtToken(instanceIdResult.getToken(), token);
                            }
                        });
                        progressDialog.dismiss();
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, response.message(), Toast.LENGTH_LONG).show());
                    progressDialog.dismiss();
                }
            } catch (Exception e) {
                Log.d(TAG, "run: generateToken " + e);
                e.printStackTrace();
            }
        }).start();

    }

}
