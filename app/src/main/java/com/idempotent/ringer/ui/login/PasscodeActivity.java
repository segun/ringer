package com.idempotent.ringer.ui.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.idempotent.ringer.MainActivity;
import com.idempotent.ringer.R;
import com.idempotent.ringer.service.ApiService;
import com.idempotent.ringer.service.RetrofitClient;
import com.idempotent.ringer.ui.data.LoginRequest;
import com.idempotent.ringer.ui.data.UserResponse;
import com.idempotent.ringer.utils.ChargingStatusHelper;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PasscodeActivity extends AppCompatActivity {

    private TextView tvTitle, tvPasscode;
    private Button btn1, btn2, btn3, btn4, btn5, btn6, btn7, btn8, btn9, btn0, btnDelete, btnNext;
    private final StringBuilder passcodeBuilder = new StringBuilder();
    private SharedPreferences sharedPreferences;
    private boolean isRegistered;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passcode);

        tvTitle = findViewById(R.id.tvTitle);
        tvPasscode = findViewById(R.id.tvPasscode);
        btn1 = findViewById(R.id.btn1);
        btn2 = findViewById(R.id.btn2);
        btn3 = findViewById(R.id.btn3);
        btn4 = findViewById(R.id.btn4);
        btn5 = findViewById(R.id.btn5);
        btn6 = findViewById(R.id.btn6);
        btn7 = findViewById(R.id.btn7);
        btn8 = findViewById(R.id.btn8);
        btn9 = findViewById(R.id.btn9);
        btn0 = findViewById(R.id.btn0);
        btnDelete = findViewById(R.id.btnDelete);
        btnNext = findViewById(R.id.btnNext);

        sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        isRegistered = sharedPreferences.getBoolean("isRegistered", false);

        // Set title based on registration or login
        tvTitle.setText(isRegistered ? "Enter Login Passcode" : "Set Your Passcode");

        // Set click listeners for numeric buttons
        View.OnClickListener numericButtonClickListener = v -> {
            if (passcodeBuilder.length() < 6) {
                Button button = (Button) v;
                passcodeBuilder.append(button.getText().toString());
                updatePasscodeDisplay();
            }
        };

        btn1.setOnClickListener(numericButtonClickListener);
        btn2.setOnClickListener(numericButtonClickListener);
        btn3.setOnClickListener(numericButtonClickListener);
        btn4.setOnClickListener(numericButtonClickListener);
        btn5.setOnClickListener(numericButtonClickListener);
        btn6.setOnClickListener(numericButtonClickListener);
        btn7.setOnClickListener(numericButtonClickListener);
        btn8.setOnClickListener(numericButtonClickListener);
        btn9.setOnClickListener(numericButtonClickListener);
        btn0.setOnClickListener(numericButtonClickListener);

        // Delete button
        btnDelete.setOnClickListener(v -> {
            if (passcodeBuilder.length() > 0) {
                passcodeBuilder.deleteCharAt(passcodeBuilder.length() - 1);
                updatePasscodeDisplay();
            }
        });

        // Next button
        btnNext.setOnClickListener(v -> {
            if (passcodeBuilder.length() == 6) {
                String passcode = passcodeBuilder.toString();
                String emailOrPhone = getIntent().getStringExtra("emailOrPhone");
                String userLocation = getIntent().getStringExtra("userLocation");
                boolean manualLocation = getIntent().getBooleanExtra("manualLocation", false);

                SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                boolean isRegistered = sharedPreferences.getBoolean("isRegistered", false);

                if (isRegistered) {
                    // If registered, attempt login
                    loginUser(emailOrPhone, passcode, userLocation, manualLocation);
                } else {
                    // If not registered, proceed to ConfirmPasscodeActivity
                    Intent intent = new Intent(PasscodeActivity.this, ConfirmPasscodeActivity.class);
                    intent.putExtra("emailOrPhone", emailOrPhone);
                    intent.putExtra("userLocation", userLocation);
                    intent.putExtra("manualLocation", manualLocation);
                    intent.putExtra("passcode", passcode);
                    startActivity(intent);
                }
            } else {
                Toast.makeText(this, "Please enter a 6-digit passcode", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updatePasscodeDisplay() {
        StringBuilder displayBuilder = new StringBuilder();
        for (int i = 0; i < passcodeBuilder.length(); i++) {
            displayBuilder.append("*");
        }
        for (int i = passcodeBuilder.length(); i < 6; i++) {
            displayBuilder.append("-");
        }
        tvPasscode.setText(displayBuilder.toString());
    }

    private void loginUser(String emailOrPhone, String passcode, String userLocation, boolean manualLocation) {
        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
        Call<UserResponse> call = apiService.login(new LoginRequest(emailOrPhone, passcode, userLocation, manualLocation));
        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                progressBar.setVisibility(View.GONE);
                Object responseBody = response.body();
                boolean isSuccess = response.isSuccessful();
                if (isSuccess && responseBody != null) {
                    Toast.makeText(PasscodeActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(PasscodeActivity.this, MainActivity.class));
                    SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("userId", response.body().getUser().getId());  // Save user ID
                    editor.putString("userLocation", userLocation);
                    editor.putBoolean("manualLocation", manualLocation);
                    editor.apply();

                    // Send charging status after successful login
                    ChargingStatusHelper.sendChargingStatusToServer(response.body().getUser().getId(), false, userLocation, manualLocation);
                    finish();
                } else {
                    Toast.makeText(PasscodeActivity.this, "Invalid passcode. Try again.", Toast.LENGTH_SHORT).show();
                    passcodeBuilder.setLength(0);
                    updatePasscodeDisplay();
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(PasscodeActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
