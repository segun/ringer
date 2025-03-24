package com.idempotent.ringer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.idempotent.ringer.R;
import com.idempotent.ringer.ui.login.LoginActivity;
import com.idempotent.ringer.ui.login.PasscodeActivity;

public class LandingActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_IS_REGISTERED = "isRegistered";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);

        MaterialButton btnLogin = findViewById(R.id.btn_login);
        MaterialButton btnRegister = findViewById(R.id.btn_register);

        btnLogin.setOnClickListener(v -> {
            setUserRegistered(true);
            navigateToPasscodeActivity();
        });

        btnRegister.setOnClickListener(v -> {
            setUserRegistered(false);
            navigateToPasscodeActivity();
        });
    }

    private void setUserRegistered(boolean isRegistered) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (isRegistered) {
            editor.putBoolean(KEY_IS_REGISTERED, true);
        } else {
            editor.remove(KEY_IS_REGISTERED);
        }

        editor.apply();
    }

    private void navigateToPasscodeActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
