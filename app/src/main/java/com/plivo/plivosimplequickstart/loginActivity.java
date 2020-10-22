package com.plivo.plivosimplequickstart;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.plivo.endpoint.Incoming;
import com.plivo.endpoint.Outgoing;

import java.util.HashMap;

public class loginActivity extends AppCompatActivity implements PlivoBackEnd.BackendListener{

    /* Define the UI elements */
    private EditText username;
    private EditText password;
    private Button login;

    boolean isValid = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ((App) getApplication()).backend().setListener(this);
        Utils.setBackendListener(this);

        String userName = "";
        String userPassword = "";

        /* Bind the XML views to Java Code Elements */
        username = findViewById(R.id.etName);
        password = findViewById(R.id.etPassword);
        login = findViewById(R.id.btnLogin);


        /* Describe the logic when the login button is clicked */
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /* Obtain user inputs */
                String userName = username.getText().toString();
                String userPassword = password.getText().toString();

                /* Check if the user inputs are empty */
                if(userName.isEmpty() || userPassword.isEmpty())
                {
                    /* Display a message toast to user to enter the details */
                    Toast.makeText(loginActivity.this, "Please enter name and password!", Toast.LENGTH_LONG).show();

                }else {
                    loginWithToken(userName,userPassword);

                }
            }
        });
    }

    private void loginWithToken(String username, String password) {
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(this, instanceIdResult ->
                    ((App) getApplication()).backend().login(instanceIdResult.getToken(),username,password));
    }

    @Override
    public void onLogin(boolean success){
        runOnUiThread(() -> {
            if (success) {
                startActivity(new Intent(loginActivity.this, MainActivity.class));
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
    }

    @Override
    public void onOutgoingCall(Outgoing data, PlivoBackEnd.STATE callState) {
    }

    @Override
    public void onIncomingDigit(String digit) {
    }

    @Override
    public void mediaMetrics(HashMap messageTemplate){

    }
}
