package com.plivo.plivosimplequickstart;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

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
        LoginWithTokenOrUsernameDialog dialogFragment = new LoginWithTokenOrUsernameDialog();
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