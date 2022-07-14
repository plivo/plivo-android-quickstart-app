package com.plivo.plivosimplequickstart;

import android.content.Context;
import android.util.Log;

import com.plivo.endpoint.AccessTokenListener;
import com.plivo.endpoint.Endpoint;
import com.plivo.endpoint.EventListener;
import com.plivo.endpoint.FeedbackCallback;
import com.plivo.endpoint.Incoming;
import com.plivo.endpoint.Outgoing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class PlivoBackEnd implements EventListener, AccessTokenListener {
    private static final String TAG = PlivoBackEnd.class.getSimpleName();

    public void submitFeedback(float rating) {
        endpoint.submitCallQualityFeedback(endpoint.getLastCallUUID(), (int) rating, new ArrayList<>(
                Arrays.asList("audio_lag")), "", false, new FeedbackCallback() {
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

        //Initiate SDK with Options, "enableTracking" (To get network related information)
        HashMap options = new HashMap();
        options.put("maxAverageBitrate", 48000);
        endpoint = Endpoint.newInstance(context, log, this, options);
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
        return endpoint.loginWithJwtToken(JWTToken,token);
    }


    public void loginWithJwtToken(String JWTToken) {
        Log.d("@@Incoming", "Endpoint loginWithJwtToken");
        endpoint.loginWithJwtToken(JWTToken);
    }


    public String getJWTUserName() {
        return endpoint.getSub_auth_ID();
    }

    public void registerListener(Context context) {
        endpoint.registerNetworkChangeReceiver(context);
    }
    public void unregisterListener(Context context) {
        endpoint.unregisterNetworkChangeReceiver(context);
    }

    public boolean loginWithAccessTokenGenerator() {
        Log.d(TAG, "loginWithAccessTokenGenerator: ");
        return  endpoint.loginWithAccessTokenGenerator(this);
    }

    public void logout() {
        endpoint.logout();
    }

    public void relayIncomingPushData(HashMap<String, String> incomingData) {
        if (incomingData != null && !incomingData.isEmpty()) {
            endpoint.relayVoipPushNotification(incomingData);
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
        Log.d(TAG, "onPermissionDenied: "+message);
        listener.onPermissionDenied(message);
    }

    @Override
    public void getAccessToken() {
        Log.d(TAG, "onTokenExpired: ");
        listener.getAccessToken();
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
