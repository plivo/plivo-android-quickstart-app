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

public class LoginWithTokenOrUsernameDialog extends DialogFragment {
    private static final String TAG = "LoginWithTokenDialog";


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.login_with_jwt, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView t = view.findViewById(R.id.etTokenUserName);
        t.setText("eyJhbGciOiJIUzI1NiIsImN0eSI6InBsaXZvO3Y9MSIsInR5cCI6IkpXVCJ9.eyJhcHAiOiIiLCJleHAiOjE2NTU3MTE2OTQsImlzcyI6Ik1BRENIQU5EUkVTSDAyVEFOSzA2IiwibmJmIjoxNjU1NzExNDc3LCJwZXIiOnsidm9pY2UiOnsiaW5jb21pbmdfYWxsb3ciOnRydWUsIm91dGdvaW5nX2FsbG93Ijp0cnVlfX0sInN1YiI6ImFiaGkxMTMzMyJ9.dTg0_VrnFh6kCK_fSIZbgqsd-7OCqdHrUuW5YDQwY2A");
        view.findViewById(R.id.loginWithJWT).setOnClickListener(view1 -> loginWithJWTtoken(t.getText().toString()));
    }

    private void loginWithJWTtoken(String token) {
        Log.d(TAG, "loginWithJWTtoken: " + token);
        if(token.isEmpty()){
            Toast.makeText(getContext(),"Enter token",Toast.LENGTH_SHORT).show();
            return;
        }
        Log.d(TAG, "loginWithJWTtoken: "+checkIfToken(token));
        if(checkIfToken(token)) {
            Pref.newInstance(getContext()).setString(Constants.JWT_ACCESS_TOKEN, token);
            Pref.newInstance(getContext()).setBoolean(Constants.IS_LOGIN_WITH_TOKEN, true);
            dismiss();
            Intent intent = new Intent(getContext(), MainActivity.class);
            startActivity(intent);
        }else{
            Pref.newInstance(getContext()).setBoolean(Constants.IS_LOGIN_WITH_USERNAME, true);
            Pref.newInstance(getContext()).setString(Constants.LOGIN_USERNAME, token);
            dismiss();
            Intent intent = new Intent(getContext(), MainActivity.class);
            startActivity(intent);
        }
    }

    private boolean checkIfToken(String token) {
        return !isAlphaNumeric(token);
    }
   boolean isAlphaNumeric(String value){ return value !=null && !value.isEmpty() && value.matches("[a-zA-Z0-9]+");}

}
