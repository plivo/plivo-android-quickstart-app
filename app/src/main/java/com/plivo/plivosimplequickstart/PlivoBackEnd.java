package com.plivo.plivosimplequickstart;

import android.util.Log;
import com.plivo.endpoint.Endpoint;
import com.plivo.endpoint.EventListener;
import com.plivo.endpoint.Incoming;
import com.plivo.endpoint.Outgoing;

import java.util.HashMap;

public class PlivoBackEnd implements EventListener {

    private static final String TAG = PlivoBackEnd.class.getSimpleName();

    enum STATE { IDLE, RINGING, ANSWERED, HANGUP, REJECTED, INVALID }

    private Endpoint endpoint;

    private BackendListener listener;

    static PlivoBackEnd newInstance() {
        return new PlivoBackEnd();
    }

    public void init(boolean log) {
        endpoint = Endpoint.newInstance(log, this);

        //Iniatiate SDK with Options, "enableTracking" and "context"(To get network related information)

//        endpoint = Endpoint.newInstance(log, this,Utils.options);
    }

    public void setListener(BackendListener listener) {
        this.listener = listener;
    }

    public void login(String newToken) {
        endpoint.login(Utils.USERNAME, Utils.PASSWORD, newToken);
        Utils.setDeviceToken(newToken);
    }

    public boolean loginForIncoming(String newToken) {
        return endpoint.login(Utils.USERNAME, Utils.PASSWORD, newToken);
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

    // Plivo SDK callbacks
    @Override
    public void onLogin() {
        Log.d(TAG, Constants.LOGIN_SUCCESS);
        Utils.setLoggedinStatus(true);
        if (listener != null) listener.onLogin(true);
    }

    @Override
    public void onLogout() {
        Log.d(TAG, Constants.LOGOUT_SUCCESS);
        if (listener != null) listener.onLogout();
    }

    @Override
    public void onLoginFailed() {
        Log.e(TAG, Constants.LOGIN_FAILED);
        if (listener != null) listener.onLogin(false);
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
    public void mediaMetrics(HashMap messageTemplate){
        Log.d(TAG, Constants.MEDIAMETRICS);
        Log.i(TAG, messageTemplate.toString());
        if (listener != null ) listener.mediaMetrics(messageTemplate);
    }


    // Your own custom listener
    public interface BackendListener {
        void onLogin(boolean success);
        void onLogout();
        void onIncomingCall(Incoming data, STATE callState);
        void onOutgoingCall(Outgoing data, STATE callState);
        void onIncomingDigit(String digit);
        void mediaMetrics(HashMap messageTemplate);
    }
}
