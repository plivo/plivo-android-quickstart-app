package com.plivo.plivosimplequickstart;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class LogInWithPasswordDialog extends DialogFragment {
    EditText eUsername;
    EditText ePassword;
    Button bLogin;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_login, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        eUsername = view.findViewById(R.id.etUsername);
        ePassword = view.findViewById(R.id.etPassword);
        bLogin = view.findViewById(R.id.btLogin);
        eUsername.setText(Utils.USERNAME);
        ePassword.setText(Utils.PASSWORD);

        bLogin.setOnClickListener(view1 -> login());
    }

    private void login() {
        String username1 = eUsername.getText().toString();
        String password2 = ePassword.getText().toString();

        if (username1.isEmpty() || password2.isEmpty()) {
            Toast.makeText(getContext(), "Please enter the username or password", Toast.LENGTH_LONG).show();
        } else {
            Pref.newInstance(getContext()).setString(Constants.USERNAME, username1);
            Pref.newInstance(getContext()).setString(Constants.PASSWORD, password2);
//                    login(username, password);
            dismiss();
            Intent intent = new Intent(getContext(), MainActivity.class);
            startActivity(intent);
        }
    }
}
