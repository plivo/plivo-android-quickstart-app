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
    EditText eusername;
    EditText epassword;
    Button eLogin;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_login, container, false);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        eusername = view.findViewById(R.id.etUsername);
        epassword = view.findViewById(R.id.etPassword);
        System.out.println(eusername);
        eLogin = (Button) view.findViewById(R.id.btLogin);
        System.out.println(eLogin);
        String username = Utils.USERNAME;
        String password = Utils.PASSWORD;
        eusername.setText(username);
        epassword.setText(password);

        eLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username1 = eusername.getText().toString();
                String password2 = epassword.getText().toString();

                if (username1.isEmpty() || password2.isEmpty()) {
                    Toast.makeText(view.getContext(), "Please enter the username or password", Toast.LENGTH_LONG).show();
                } else {
                    Pref.newInstance(view.getContext()).setString(Constants.USERNAME, username1);
                    Pref.newInstance(view.getContext()).setString(Constants.PASSWORD, password2);
//                    login(username, password);
                    dismiss();
                    Intent intent = new Intent(view.getContext(), MainActivity.class);
                    startActivity(intent);
                }
            }
        });
        
    }
}
