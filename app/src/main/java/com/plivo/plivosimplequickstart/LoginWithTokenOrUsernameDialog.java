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
        t.setText("");
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
