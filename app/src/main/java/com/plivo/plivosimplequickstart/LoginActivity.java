package com.plivo.plivosimplequickstart;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signin);
        findViewById(R.id.loginWithUsername).setOnClickListener(view -> loginWithUserNamePassword());
        findViewById(R.id.loginwithTokenUsername).setOnClickListener(view -> loginWithTokenOrUserName());
    }

    private void loginWithTokenOrUserName() {
        LoginWithTokenOrUsername dialogFragment = new LoginWithTokenOrUsername();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog2");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        dialogFragment.show(ft, "dialog2");
    }

    private void loginWithUserNamePassword() {
        LogInWithPasswordDialog dialogFragment = new LogInWithPasswordDialog();
        Bundle bundle = new Bundle();
        bundle.putBoolean("UserNamePassword", true);
        dialogFragment.setArguments(bundle);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        dialogFragment.show(ft, "dialog");
    }

    private void login(String userName, String passWord) {
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(this, instanceIdResult ->
                ((App) getApplication()).backend().login(instanceIdResult.getToken(), userName, passWord));
    }
}