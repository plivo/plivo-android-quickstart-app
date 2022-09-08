package com.plivo.plivosimplequickstart;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.plivo.endpoint.AccessTokenListener;
import com.plivo.endpoint.Endpoint;
import com.plivo.endpoint.EventListener;
import com.plivo.endpoint.FeedbackCallback;
import com.plivo.endpoint.Incoming;
import com.plivo.endpoint.Outgoing;
import com.plivo.endpoint.slf4j.helpers.Util;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PlivoBackEnd implements EventListener {
    private static final String TAG = PlivoBackEnd.class.getSimpleName();
    private TokenGenerator tokenGenerator;

    public void submitFeedback(String callUUID, float rating, ArrayList<String> issue, String comment, boolean sendLogs) {
        endpoint.submitCallQualityFeedback(callUUID, (int) rating, issue, comment, sendLogs, new FeedbackCallback() {
            @Override
            public void onFailure(int statusCode) {
                Log.d("@@Feedback", "onFailure: ");
            }

            @Override
            public void onSuccess(String response) {
                Log.d("@@Feedback", "onSuccess: ");
            }

            @Override
            public void onValidationFail(String message) {
                Log.d("@@Feedback", "onValidationFail: ");
            }
        });
    }


    enum STATE {IDLE, PROGRESS, RINGING, ANSWERED, HANGUP, REJECTED, INVALID;}

    private Endpoint endpoint;
    private BackendListener listener;
    private Context context;

    static PlivoBackEnd newInstance() {
        return new PlivoBackEnd();
    }

    public void init(boolean log) {
//        endpoint = Endpoint.newInstance(context,log, this);
        Log.d("****PlivoBackEnd", "Init");
        //Initiate SDK with Options, "enableTracking" (To get network related information)
        HashMap options = Utils.options;
        options.put("maxAverageBitrate", 48000);
        endpoint = Endpoint.newInstance(context, log, this);
        tokenGenerator = new TokenGenerator(context);
    }

    public void setListener(BackendListener listener) {
        this.listener = listener;
    }

    public boolean login(String newToken, String username, String password) {
        Log.d("@@Incoming", "Endpoint login");
        Utils.setDeviceToken(newToken);
        return endpoint.login(username, password, newToken);
    }

    public boolean loginWithJwtToken(String token, String JWTToken) {
        Log.d("@@Incoming", "Endpoint loginWithJwtToken");
        Utils.setDeviceToken(token);
        return endpoint.loginWithJwtToken(JWTToken, token);
    }

    public boolean loginForIncomingWithJwt(String token, String JWTToken, String certificateId, HashMap<String, String> incomingData) {
        Log.d("@@Incoming", "Endpoint loginForIncomingWithJwt");
        Utils.setDeviceToken(token);
        return endpoint.loginForIncomingWithJwt(JWTToken, token, certificateId, incomingData);
    }

    public void loginWithJwtToken(String JWTToken) {
        Log.d("@@Incoming", "Endpoint loginWithJwtToken");
        endpoint.loginWithJwtToken(JWTToken);
    }


    public String getJWTUserName() {
        return endpoint.getSub_auth_ID();
    }

    public String getCallUUID() {
        return endpoint.getLastCallUUID();
    }

    public boolean loginWithAccessTokenGenerator() {
        Log.d(TAG, "loginWithAccessTokenGenerator: ");
        tokenGenerator.loginForIncoming(null, "");
        return endpoint.loginWithAccessTokenGenerator(tokenGenerator.getListener());
    }

    public boolean loginWithAccessTokenGenerator(HashMap map) {
        Log.d(TAG, "loginWithAccessTokenGenerator with map ");
        tokenGenerator.loginForIncoming(map, "");
        return endpoint.loginWithAccessTokenGenerator(tokenGenerator.getListener());
    }

    public void logout() {
        endpoint.logout();
    }

    public void loginForIncomingWithUsername(String username, String password, String deviceToken, String certificateId, HashMap<String, String> incomingData) {
        if (incomingData != null && !incomingData.isEmpty()) {
            endpoint.loginForIncomingWithUsername(username, password, deviceToken, certificateId, incomingData);
        }
    }

    public Outgoing getOutgoing() {
        return endpoint.createOutgoingCall();
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public boolean getRegistered() {
        return endpoint.getRegistered();
    }

    // Plivo SDK callbacks
    @Override
    public void onLogin() {
        Log.d(TAG, Constants.LOGIN_SUCCESS);
        Utils.setLoggedinStatus(true);
        Pref.newInstance(getContext()).setBoolean(Constants.LOG_IN, true);
        if (listener != null) listener.onLogin(true);
    }

    @Override
    public void onLogout() {
        Log.d(TAG, Constants.LOGOUT_SUCCESS);
        Pref.newInstance(context.getApplicationContext()).setBoolean(Constants.IS_LOGIN_WITH_TOKEN, false);
        Pref.newInstance(context.getApplicationContext()).setBoolean(Constants.IS_LOGIN_WITH_USERNAME, false);
        Pref.newInstance(context.getApplicationContext()).setBoolean(Constants.LOG_IN, false);
        Utils.setLoggedinStatus(false);
        listener.onLogout();
//        if (listener != null) listener. cf();
    }

    @Override
    public void onLoginFailed() {
        Log.e(TAG, Constants.LOGIN_FAILED);
        if (listener != null) listener.onLogin(false);
    }

    @Override
    public void onLoginFailed(String message) {
        Log.e(TAG, Constants.LOGIN_FAILED + message);
        if (listener != null) listener.onLoginFailed(message);
    }

    @Override
    public void onIncomingDigitNotification(String s) {
        if (listener != null) listener.onIncomingDigit(s);
    }

    @Override
    public void onIncomingCall(Incoming incoming) {
        Log.d(TAG, Constants.INCOMING_CALL_RINGING);
        Log.d(TAG, "****onIncomingCall");
        Utils.setIncoming(incoming);
        if (listener != null) listener.onIncomingCall(incoming, STATE.RINGING);
    }

    @Override
    public void onIncomingCallHangup(Incoming incoming) {
        Log.d(TAG, Constants.INCOMING_CALL_HANGUP);
        if (listener != null) listener.onIncomingCall(incoming, STATE.HANGUP);
    }

    @Override
    public void onIncomingCallInvalid(Incoming incoming) {

    }

    @Override
    public void onIncomingCallRejected(Incoming incoming) {
        Log.d(TAG, Constants.INCOMING_CALL_REJECTED);
        if (listener != null) listener.onIncomingCall(incoming, STATE.REJECTED);
    }

    @Override
    public void onOutgoingCall(Outgoing outgoing) {
        Log.d(TAG, Constants.OUTGOING_CALL);
        if (listener != null) listener.onOutgoingCall(outgoing, STATE.PROGRESS);
    }

    @Override
    public void onOutgoingCallRinging(Outgoing outgoing) {
        Log.d(TAG, Constants.OUTGOING_CALL_RINGING);
        if (listener != null) listener.onOutgoingCall(outgoing, STATE.RINGING);
    }

    @Override
    public void onOutgoingCallAnswered(Outgoing outgoing) {
        Log.d(TAG, Constants.OUTGOING_CALL_ANSWERED);
        if (listener != null) listener.onOutgoingCall(outgoing, STATE.ANSWERED);
    }

    @Override
    public void onOutgoingCallRejected(Outgoing outgoing) {
        Log.d(TAG, Constants.OUTGOING_CALL_REJECTED);
        if (listener != null) listener.onOutgoingCall(outgoing, STATE.REJECTED);
    }


    public void onIncomingCallConnected(Incoming incoming) {
        Log.d(TAG, Constants.INCOMING_CALL_CONNECTED);
        if (listener != null) listener.onIncomingCall(incoming, STATE.ANSWERED);
    }

    @Override
    public void onOutgoingCallHangup(Outgoing outgoing) {
        Log.d(TAG, Constants.OUTGOING_CALL_HANGUP);
        if (listener != null) listener.onOutgoingCall(outgoing, STATE.HANGUP);
    }

    @Override
    public void onOutgoingCallInvalid(Outgoing outgoing) {
        Log.d(TAG, Constants.OUTGOING_CALL_INVALID);
        if (listener != null) listener.onOutgoingCall(outgoing, STATE.INVALID);
    }

    @Override
    public void mediaMetrics(HashMap messageTemplate) {
        Log.d(TAG, Constants.MEDIAMETRICS);
        Log.i(TAG, messageTemplate.toString());
        if (listener != null) listener.mediaMetrics(messageTemplate);
    }


    @Override
    public void onPermissionDenied(String message) {
        Log.d(TAG, "onPermissionDenied: " + message);
        listener.onPermissionDenied(message);
    }


    // Your own custom listener
    public interface BackendListener {
        void onLogin(boolean success);

        void onLoginFailed(String message);

        void onLogout();

        void onIncomingCall(Incoming data, STATE callState);

        void onOutgoingCall(Outgoing data, STATE callState);

        void onIncomingDigit(String digit);

        void mediaMetrics(HashMap messageTemplate);

        void onPermissionDenied(String message);

        void getAccessToken();
    }
}
