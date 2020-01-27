package com.plivo.plivosimplequickstart;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RatingBar;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.PermissionChecker;

import com.google.firebase.iid.FirebaseInstanceId;
import com.plivo.endpoint.Incoming;
import com.plivo.endpoint.Outgoing;
import com.plivo.plivosimplequickstart.PlivoBackEnd.STATE;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import static com.plivo.plivosimplequickstart.Utils.HH_MM_SS;
import static com.plivo.plivosimplequickstart.Utils.MM_SS;

public class MainActivity extends AppCompatActivity implements PlivoBackEnd.BackendListener {
    private static final int PERMISSIONS_REQUEST_CODE = 21;

    private AlertDialog alertDialog;

    private Timer callTimer;

    private int tick;

    private ActionBar actionBar;

    private boolean isSpeakerOn=false, isHoldOn=false, isMuteOn=false;

    private Object callData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (PermissionChecker.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                init();
            } else {
                requestPermissions(new String[] { Manifest.permission.RECORD_AUDIO }, PERMISSIONS_REQUEST_CODE);
            }
        } else {
            init();
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

    private void init() {
        registerBackendListener();
        loginWithToken();
    }

    private void registerBackendListener() {
        ((App) getApplication()).backend().setListener(this);
    }

    private void loginWithToken() {
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(this, instanceIdResult ->
                ((App) getApplication()).backend().login(instanceIdResult.getToken()));
    }

    private void logout() {
        ((App) getApplication()).backend().logout();
    }

    /**
     * Display & Handle Outgoing Calls
     * @param state
     * @param outgoing
     */
    private void showOutCallUI(STATE state, Outgoing outgoing) {

        String title = state.name();

        if (state == STATE.IDLE) {
            EditText phoneNumberText = (EditText) findViewById(R.id.call_text);
            String phoneNum = phoneNumberText.getText().toString();
            setContentView(R.layout.call);
            TextView callerName = (TextView) findViewById(R.id.caller_name);
            TextView callerState = (TextView) findViewById(R.id.caller_state);
            callerName.setText(phoneNum);
            callerState.setText(title);
            makeCall(phoneNum);
        }
        if (state == STATE.RINGING) {
            TextView callerState = (TextView) findViewById(R.id.caller_state);
            callerState.setText("Ringing...");
        }
        if(state == STATE.ANSWERED) {
            startTimer();
        }
    }

    /**
     * Display & Handle Incoming Calls
     * @param state
     * @param incoming
     */
    private void showInCallUI(STATE state, Incoming incoming) {
        if (alertDialog != null) alertDialog.dismiss();

        String title = state.name() + " " + (incoming != null ? Utils.from(incoming.getFromContact(), incoming.getFromSip()) : "");

        switch (state) {
            case ANSWERED:
                alertDialog = new AlertDialog.Builder(this)
                        .setTitle(title)
                        .setView(R.layout.dialog_outgoing_content_view)
                        .setCancelable(false)
                        .setNeutralButton(R.string.end_call, (dialog, which) -> {
                            cancelTimer();
                            incoming.hangup();
                        })
                        .show();
                startTimer();
                break;

            case RINGING:
                alertDialog = new AlertDialog.Builder(this)
                        .setTitle(title)
                        .setView(R.layout.dialog_outgoing_content_view)
                        .setCancelable(false)
                        .setNegativeButton(R.string.reject, (dialog, which) -> incoming.reject())
                        .setPositiveButton(R.string.answer, (dialog, which) -> {
                            incoming.answer();
                            updateUI(STATE.ANSWERED, incoming);
                        })
                        .show();
                break;
        }

        if (alertDialog != null) {
            // DTMF handle
            AppCompatEditText editBox = alertDialog.findViewById(R.id.edit_number);
            if (editBox != null) {
                editBox.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                    @Override
                    public void afterTextChanged(Editable s) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (state != STATE.ANSWERED || incoming == null) return;

                        if (!TextUtils.isEmpty(s) && before < count) {
                            incoming.sendDigits(Character.toString(s.charAt(s.length() - 1)));
                        }
                    }
                });
            }

            // stop timer
            alertDialog.setOnDismissListener(dialog -> {
                if (state == STATE.ANSWERED) cancelTimer();
            });
        }
    }

    private void startTimer() {
        cancelTimer();

        callTimer = new Timer(false);
        callTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    int hours = (int) TimeUnit.SECONDS.toHours(tick);
                    int minutes = (int) TimeUnit.SECONDS.toMinutes(tick-=TimeUnit.HOURS.toSeconds(hours));
                    int seconds = (int) (tick-TimeUnit.MINUTES.toSeconds(minutes));
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
        Outgoing outgoing = ((App) getApplication()).backend().getOutgoing();
        if (outgoing != null) {
            outgoing.call(phoneNum);

        }
    }
    public void showRatingWindow(){
        RatingBar ratingBar;
        TextView star;
        setContentView(R.layout.rating);
        ratingBar = (RatingBar) findViewById(R.id.star);
        star = (TextView) findViewById(R.id.star_count);
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                float ratedValue;
                ratedValue = ratingBar.getRating();
                star.setText("Your Rating : " + (int)ratedValue + "/5");
                LinearLayout one = (LinearLayout) findViewById(R.id.LinearLayout);

                if (ratedValue==5) {
                    EditText comments = (EditText) findViewById(R.id.comments);
                    comments.getText().clear();
                    one.setVisibility(View.GONE);
                }
                else{
                    one.setVisibility(View.VISIBLE);
                }
            }
        });
    }
    public void submitCallQualityFeedback(){
        Boolean addLog= ((CheckBox) findViewById(R.id.add_log)).isChecked();
        ArrayList <String> issueList = new ArrayList<String>();
        if (((CheckBox) findViewById(R.id.audio_lag)).isChecked()){
            issueList.add( (((CheckBox) findViewById(R.id.audio_lag)).getText()).toString());
        }
        if (((CheckBox) findViewById(R.id.broken_audio)).isChecked()){
            issueList.add( (((CheckBox) findViewById(R.id.broken_audio)).getText()).toString());
        }
        if (((CheckBox) findViewById(R.id.call_dropped)).isChecked()){
            issueList.add( (((CheckBox) findViewById(R.id.call_dropped)).getText()).toString());
        }
        if (((CheckBox) findViewById(R.id.high_connect_time)).isChecked()){
            issueList.add( (((CheckBox) findViewById(R.id.high_connect_time)).getText()).toString());
        }
        if (((CheckBox) findViewById(R.id.low_audio_level)).isChecked()){
            issueList.add( (((CheckBox) findViewById(R.id.low_audio_level)).getText()).toString());
        }
        if (((CheckBox) findViewById(R.id.callerid_issues)).isChecked()){
            issueList.add( (((CheckBox) findViewById(R.id.callerid_issues)).getText()).toString());
        }
        if (((CheckBox) findViewById(R.id.echo)).isChecked()){
            issueList.add( (((CheckBox) findViewById(R.id.echo)).getText()).toString());
        }
        RatingBar ratingBar = (RatingBar) findViewById(R.id.star);
        Integer ratedValue = (int) (ratingBar.getRating());
        String comments = ((EditText) findViewById(R.id.comments)).getText().toString();
        if(ratedValue==5) {
            issueList.clear();
            addLog=false;
        }
        if (ratedValue==0){
            alertDialog = new AlertDialog.Builder(this)
                    .setTitle("Star rating can't be empty")
                    .setCancelable(true)
                    .setNeutralButton("Ok", (dialog, which) -> {
                        showRatingWindow();
                    })
                    .show();
        }
        else if (ratedValue<5 && issueList.size()==0){
            alertDialog = new AlertDialog.Builder(this)
                    .setTitle("Atleast one issue is mandatory for feedback")
                    .setCancelable(true)
                    .setNeutralButton("Ok", (dialog, which) -> {
                        showRatingWindow();
                    })
                    .show();

        }
        else {
            ((App) getApplication()).backend().submitCallQualityFeedback(ratedValue, addLog, comments, issueList);
            setContentView(R.layout.activity_main);
            updateUI(STATE.IDLE, null);
        }

    }

    public void onClickBtnMakeCall(View view) {
        EditText phoneNumberText = (EditText) findViewById(R.id.call_text);
        String phoneNumber = phoneNumberText.getText().toString();
        if (phoneNumber.matches("")) {
            Toast.makeText(this, "Enter sip uri or phone number", Toast.LENGTH_SHORT).show();
            return;
        }
        hideSupportActionBar(view);
        showOutCallUI(STATE.IDLE, null);
    }

    public void onClickBtnEndCall(View view) {
        unHideSupportActionBar(view);
        endCall();
        setContentView(R.layout.activity_main);
        updateUI(STATE.IDLE, null);
    }

    public void endCall() {
        if (callData != null) {
            if (callData instanceof Outgoing) {
                ((Outgoing) callData).hangup();
            } else {
                ((Incoming) callData).hangup();
            }
        }
    }

    public void onClickBtnSpeaker(View view) {
        AudioManager audioManager =(AudioManager)getSystemService(Context.AUDIO_SERVICE);
        ImageButton btn = (ImageButton) findViewById(R.id.speaker);
        if(isSpeakerOn) {
            isSpeakerOn=false;
            btn.setImageResource(R.drawable.speaker);
            audioManager.setMode(AudioManager.MODE_IN_CALL);
            audioManager.setMode(AudioManager.MODE_NORMAL);
        }
        else {
            isSpeakerOn=true;
            btn.setImageResource(R.drawable.speaker_selected);
            audioManager.setMode(AudioManager.MODE_NORMAL);
            audioManager.setMode(AudioManager.MODE_IN_CALL);
        }
        audioManager.setSpeakerphoneOn(isSpeakerOn);
    }

    public void onClickBtnHold(View view) {
        ImageButton btn = (ImageButton) findViewById(R.id.hold);
        if(isHoldOn) {
            isHoldOn=false;
            btn.setImageResource(R.drawable.hold);
            unHoldCall();
        }
        else {
            isHoldOn=true;
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
        if(isMuteOn) {
            isMuteOn=false;
            btn.setImageResource(R.drawable.mute);
            unMuteCall();
        }
        else {
            isMuteOn=true;
            btn.setImageResource(R.drawable.mute_selected);
            muteCall();
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

    public void onClickSkip(View view){
        setContentView(R.layout.activity_main);
        updateUI(STATE.IDLE, null);
    }

    public void hideSupportActionBar(View view) {
        actionBar.hide();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
    }

    public void unHideSupportActionBar(View view) {
        actionBar.show();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
    }

    private void updateUI(PlivoBackEnd.STATE state, Object data) {
        callData = data;
        if(state.equals(STATE.REJECTED) || state.equals(STATE.HANGUP) || state.equals(STATE.INVALID)){
            if (data != null) {
                if (data instanceof Outgoing) {
                    // handle outgoing
                    showOutCallUI(state, (Outgoing) data);
                } else {
                    // handle incoming
                    showInCallUI(state, (Incoming) data);
                }
            }
            showRatingWindow();
        }
        else {

            if(findViewById(R.id.call_btn) ==null ||  findViewById(R.id.logged_in_as) == null || findViewById(R.id.logging_in_label)==null){
                if (data != null) {
                    if (data instanceof Outgoing) {
                        // handle outgoing
                        showOutCallUI(state, (Outgoing) data);
                    } else {
                        // handle incoming
                        showInCallUI(state, (Incoming) data);
                    }
                }
            }else {
                ((AppCompatTextView) findViewById(R.id.logging_in_label)).setText("Logged in as:");
                ((AppCompatTextView) findViewById(R.id.logged_in_as)).setText(Utils.USERNAME);
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

    }

    @Override
    public void onIncomingCall(Incoming data, PlivoBackEnd.STATE callState) {
        runOnUiThread(() -> updateUI(callState, data));
    }

    @Override
    public void onOutgoingCall(Outgoing data, PlivoBackEnd.STATE callState) {
        runOnUiThread(() -> updateUI(callState, data));
    }

    @Override
    public void onIncomingDigit(String digit) {
        runOnUiThread(() -> Toast.makeText(this, String.format(getString(R.string.dtmf_received), digit), Toast.LENGTH_SHORT).show());
    }

    @Override
    public void mediaMetrics(HashMap messageTemplate){

    }
}
