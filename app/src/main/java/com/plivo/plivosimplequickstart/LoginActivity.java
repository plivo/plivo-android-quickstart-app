package com.plivo.plivosimplequickstart;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.plivo.plivosimplequickstart.PlivoBackEnd;

import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {
    EditText eusername;
    EditText epassword;
    Button eLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        eusername = findViewById(R.id.etUsername);
        epassword = findViewById(R.id.etPassword);
        System.out.println(eusername);
        eLogin = (Button) findViewById(R.id.btLogin);
        System.out.println(eLogin);

        eLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String username = eusername.getText().toString();
                String password = epassword.getText().toString();
                MainActivity.username= username;
                MainActivity.password= password;

                if(username.isEmpty() || password.isEmpty()){
                    Toast.makeText(LoginActivity.this,"Please enter the username or password",Toast.LENGTH_LONG).show();
                } else{
                    login(username,password);
                    Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                    startActivity(intent);
                    }
            }
        });
    }

    private void login(String userName, String passWord){
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(this, instanceIdResult ->
                ((App) getApplication()).backend().login(instanceIdResult.getToken(),userName,passWord));
    }
}