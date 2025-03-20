package com.idempotent.ringer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.idempotent.ringer.ui.login.LoginActivity;

public class MainActivity extends AppCompatActivity {
    private Button stopSoundButton;
    private boolean isSoundPlaying = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        startActivity(new Intent(this, LoginActivity.class));
        finish();

        setContentView(R.layout.activity_main);

        // Start the foreground service
        startChargingToneService();

        // Setup stop sound button
        stopSoundButton = findViewById(R.id.stopSoundButton);
        stopSoundButton.setEnabled(false);
        stopSoundButton.setOnClickListener(v -> {
            // Send broadcast to stop sound in service
            Intent stopIntent = new Intent("STOP_SOUND");
            sendBroadcast(stopIntent);
            isSoundPlaying = false;
            stopSoundButton.setEnabled(false);
        });

        // Register a local broadcast receiver to update button state
        IntentFilter filter = new IntentFilter("SOUND_PLAYING_STATUS");
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                isSoundPlaying = intent.getBooleanExtra("isPlaying", false);
                stopSoundButton.setEnabled(isSoundPlaying);
            }
        }, filter);
    }

    private void startChargingToneService() {
        Intent serviceIntent = new Intent(this, ChargingToneService.class);
        startForegroundService(serviceIntent);
    }
}
