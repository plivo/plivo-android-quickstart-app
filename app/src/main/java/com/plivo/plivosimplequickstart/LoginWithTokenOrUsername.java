package com.plivo.plivosimplequickstart;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.iid.FirebaseInstanceId;
import com.plivo.endpoint.Incoming;
import com.plivo.endpoint.Outgoing;

import java.util.HashMap;
import java.util.concurrent.Executor;

public class LoginWithTokenOrUsername extends DialogFragment implements PlivoBackEnd.BackendListener{
    private static final String TAG = "LoginWithTokenOrUsernam";


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.login_with_jwt, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView t = view.findViewById(R.id.etTokenUserName);
        t.setText("eyJhbGciOiJIUzI1NiIsImN0eSI6InBsaXZvO3Y9MSIsInR5cCI6IkpXVCJ9.eyJhcHAiOiIiLCJleHAiOjE2NTM0NzkxODcsImlzcyI6Ik1BRENIQU5EUkVTSDAyVEFOSzA2IiwibmJmIjoxNjUzMzkyNzg3LCJwZXIiOnsidm9pY2UiOnsiaW5jb21pbmdfYWxsb3ciOnRydWUsIm91dGdvaW5nX2FsbG93Ijp0cnVlfX0sInN1YiI6ImFiaGlzaGVrMzMyNTQ1NDUzNTQzNDMifQ.py-KY_TOdZpUYy7pteFo6ZMWOBwrrRUF7y3JwBX5P5A");
        view.findViewById(R.id.loginWithJWT).setOnClickListener(view1 -> loginWithJWTtoken(t.getText().toString()));
    }

    private void loginWithJWTtoken(String token) {
        Log.d(TAG, "loginWithJWTtoken: "+token);
        if(!token.isEmpty()) {
            Pref.newInstance(getContext()).setString(Constants.JWT_ACCESS_TOKEN, token);
            FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(getActivity(), instanceIdResult ->
                    ((App) getContext().getApplicationContext()).backend().loginWithJwtToken(instanceIdResult.getToken(),token));

        }else{
            Log.d(TAG, "loginWithJWTtoken: Enter token");
        }


    }

    @Override
    public void onLogin(boolean success) {
        Log.d(TAG, "onLogin: ");
        dismiss();
        Intent intent = new Intent(getContext(), MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onLogout() {

    }

    @Override
    public void onIncomingCall(Incoming data, PlivoBackEnd.STATE callState) {

    }

    @Override
    public void onOutgoingCall(Outgoing data, PlivoBackEnd.STATE callState) {

    }

    @Override
    public void onIncomingDigit(String digit) {

    }

    @Override
    public void mediaMetrics(HashMap messageTemplate) {

    }
}
