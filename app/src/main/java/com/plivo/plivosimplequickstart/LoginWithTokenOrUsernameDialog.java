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
        t.setText("eyJhbGciOiJIUzI1NiIsImN0eSI6InBsaXZvO3Y9MSIsInR5cCI6IkpXVCJ9.eyJhcHAiOiIiLCJleHAiOjE2NTUxMzMxNzYsImlzcyI6Ik1BRENIQU5EUkVTSDAyVEFOSzA2IiwibmJmIjoxNjU1MDQ2Nzc2LCJwZXIiOnsidm9pY2UiOnsiaW5jb21pbmdfYWxsb3ciOnRydWUsIm91dGdvaW5nX2FsbG93Ijp0cnVlfX0sInN1YiI6InBhbDMzMzMifQ.WJNaaKnIW-SxtbiNIdoMziRwWn-xaoiriqEjsZQM_Fk");
        view.findViewById(R.id.loginWithJWT).setOnClickListener(view1 -> loginWithJWTtoken(t.getText().toString()));
    }

    private void loginWithJWTtoken(String token) {
        Log.d(TAG, "loginWithJWTtoken: " + token);
        if(token.isEmpty()){
            Toast.makeText(getContext(),"Enter token",Toast.LENGTH_SHORT).show();
            return;
        }
        Pref.newInstance(getContext()).setString(Constants.JWT_ACCESS_TOKEN, token);
        Pref.newInstance(getContext()).setBoolean(Constants.IS_LOGIN_WITH_TOKEN, true);
        dismiss();
        Intent intent = new Intent(getContext(), MainActivity.class);
        startActivity(intent);
    }
}
